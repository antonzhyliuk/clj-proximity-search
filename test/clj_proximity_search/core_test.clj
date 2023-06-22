(ns clj-proximity-search.core-test
  (:require [clojure.test :refer [deftest
                                  testing
                                  is]]
            [clj-proximity-search.core :refer [proximity-search
                                       match-query]]
            [clojure.string :as str]))

(defn tokenize [text]
  (vec (str/split text #" ")))

(def sample-text
  (str
   "The invention discloses a carbon fiber solar panel car roof. Carbon fiber is taken as a raw material to be "
   "subjected to die machining; one or more required carbon fiber car roof models are formed; then a silicon raw "
   "material is subjected to chemical vapor deposition or deposited or sprayed on to a prepared carbon fiber model "
   "base in a physical spraying mode in a reaction chamber, so as to form a crystalline silicon film; finally the "
   "carbon fiber model base with the crystalline silicon film is put into a crystal oven, so as to be subjected to "
   "high-temperature melting crystallization; and a series of battery piece manufacture processes are performed, so "
   "that a required solar panel is obtained, and the finished product of the carbon fiber solar panel car roof is"
   "obtained"))

(def tokens (tokenize sample-text))

(def query1
  ;; solar
  {:Query :Keyword
   :token "solar"})

(def query2
  ;; solar W1 panel
  {:Query    :Op
   :operator :within
   :distance 1
   :operands [{:Query :Keyword
               :token "solar"}
              {:Query :Keyword
               :token "panel"}]})

(def query3
  ;; solar W2 panel
  {:Query    :Op
   :operator :within
   :distance 2
   :operands [{:Query :Keyword
               :token "solar"}
              {:Query :Keyword
               :token "panel"}]})

(def query4
  ;; (solar W1 panel) W2 roof"
  {:Query    :Op
   :operator :within
   :distance 2
   :operands [{:Query    :Op
               :operator :within
               :distance 1
               :operands [{:Query :Keyword
                           :token "solar"}
                          {:Query :Keyword
                           :token "panel"}]}
              {:Query :Keyword
               :token "roof"}]})

(def query5
  ;; (solar W1 panel) W1 roof"
  {:Query    :Op
   :operator :within
   :distance 1
   :operands [{:Query    :Op
               :operator :within
               :distance 1
               :operands [{:Query :Keyword
                           :token "solar"}
                          {:Query :Keyword
                           :token "panel"}]}
              {:Query :Keyword
               :token "roof"}]})

(def query6
  ;; car W9 (silicon W3 material)
  {:Query    :Op
   :operator :within
   :distance 9
   :operands [{:Query :Keyword
               :token "car"}
              {:Query    :Op
               :operator :within
               :distance 3
               :operands [{:Query :Keyword
                           :token "silicon"}
                          {:Query :Keyword
                           :token "material"}]}]})

(def query7
  ;; (silicon W2 material) W2 film
  {:Query    :Op
   :operator :within
   :distance 2
   :operands [{:Query    :Op
               :operator :within
               :distance 2
               :operands [{:Query :Keyword
                           :token "silicon"}
                          {:Query :Keyword
                           :token "material"}]}
              {:Query :Keyword
               :token "film"}]})

(def query8
  ;; crystalline W1 (silicon W1 (film W1 is))
  {:Query    :Op
   :operator :within
   :distance 1
   :operands [{:Query :Keyword
               :token "crystalline"}
              {:Query    :Op
               :operator :within
               :distance 1
               :operands [{:Query :Keyword
                           :token "silicon"}
                          {:Query    :Op
                           :operator :within
                           :distance 1
                           :operands [{:Query :Keyword
                                       :token "film"}
                                      {:Query :Keyword
                                       :token "is"}]}]}]})

(def query9
  ;; (carbon W2 car) N2 required
  {:Query    :Op
   :operator :near
   :distance 2
   :operands [{:Query    :Op
               :operator :within
               :distance 2
               :operands [{:Query :Keyword
                           :token "carbon"}
                          {:Query :Keyword
                           :token "car"}]}
              {:Query :Keyword
               :token "required"}]})

(def query10
  ;; (carbon N3 product) N2 the
  {:Query    :Op
   :operator :near
   :distance 2
   :operands [{:Query    :Op
               :operator :near
               :distance 3
               :operands [{:Query :Keyword
                           :token "carbon"}
                          {:Query :Keyword
                           :token "product"}]}
              {:Query :Keyword
               :token "the"}]})

(deftest proximity-search-test
  (testing "Proximity search test"
    (is (proximity-search tokens query1))
    (is (proximity-search tokens query2))
    (is (proximity-search tokens query3))
    (is (proximity-search tokens query4))
    (is (not (proximity-search tokens query5)))
    (is (proximity-search tokens query6))
    (is (not (proximity-search tokens query7)))
    (is (proximity-search tokens query8))
    (is (proximity-search tokens query9))
    (is (proximity-search tokens query10)))
  (testing "Should not match on empty vec"
    (is (not (proximity-search [] query1)))))


(deftest match-query-test
  (testing "Should match on word at given index and return match struct with index"
    (is (= {:Match :Keyword
            :token "The"
            :index 0}
           (match-query tokens 0 {:Query :Keyword
                                  :token "The"})))
    (is (= {:Match :Keyword
            :token  "invention"
            :index 1}
           (match-query tokens 1 {:Query :Keyword
                                  :token "invention"})))
    (is (not (match-query tokens 1 {:Query :Keyword
                                    :token "non-existing-word"}))))

  (testing "Should match on operator with distance and return match struct with index"
    (is (match-query tokens 0 {:Query    :Op
                               :operator :within
                               :distance 1
                               :operands [{:Query :Keyword
                                           :token "The"}
                                          {:Query :Keyword
                                           :token "invention"}]}))
    (is (= {:Match    :Op
            :distance 3
            :operands [{:Match :Keyword
                        :token "discloses"
                        :index 2}
                       {:Match :Keyword
                        :token "fiber"
                        :index 5}]}
           (match-query tokens 2 {:Query    :Op
                                  :operator :within
                                  :distance 3
                                  :operands [{:Query :Keyword
                                              :token "discloses"}
                                             {:Query :Keyword
                                              :token "fiber"}]})))))
