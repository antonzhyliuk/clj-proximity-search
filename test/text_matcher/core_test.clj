(ns text-matcher.core-test
  (:require [clojure.test :refer [deftest
                                  testing
                                  is]]
            [text-matcher.core :refer [proximity-search
                                       match-by-index
                                       tokenize]]))

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

(def query1
  ;; solar
  {:Query :Keyword
   :word  "solar"})

(def query2
  ;; solar W1 panel
  {:Query    :Op
   :distance 1
   :operands [{:Query :Keyword
               :word  "solar"}
              {:Query :Keyword
               :word  "panel"}]})

(def query3
  ;; solar W2 panel
  {:Query    :Op
   :distance 2
   :operands [{:Query :Keyword
               :word  "solar"}
              {:Query :Keyword
               :word  "panel"}]})

(def query4
  ;; (solar W1 panel) W2 roof"
  {:Query    :Op
   :distance 2
   :operands [{:Query    :Op
               :distance 1
               :operands [{:Query :Keyword
                           :word  "solar"}
                          {:Query :Keyword
                           :word  "panel"}]}
              {:Query :Keyword
               :word  "roof"}]})

(def query5
  ;; (solar W1 panel) W1 roof"
  {:Query    :Op
   :distance 1
   :operands [{:Query    :Op
               :distance 1
               :operands [{:Query :Keyword
                           :word  "solar"}
                          {:Query :Keyword
                           :word  "panel"}]}
              {:Query :Keyword
               :word  "roof"}]})

(def query6
  ;; car W9 (silicon W3 material)
  {:Query    :Op
   :distance 9
   :operands [{:Query :Keyword
               :word  "car"}
              {:Query    :Op
               :distance 3
               :operands [{:Query :Keyword
                           :word  "silicon"}
                          {:Query :Keyword
                           :word  "material"}]}]})

(def query7
  ;; (silicon W2 material) W2 film
  {:Query    :Op
   :distance 2
   :operands [{:Query    :Op
               :distance 2
               :operands [{:Query :Keyword
                           :word  "silicon"}
                          {:Query :Keyword
                           :word  "material"}]}
              {:Query :Keyword
               :word  "film"}]})

(def query8
  ;; crystalline W1 (silicon W1 (film W1 is))
  {:Query    :Op
   :distance 1
   :operands [{:Query :Keyword
               :word  "crystalline"}
              {:Query    :Op
               :distance 1
               :operands [{:Query :Keyword
                           :word  "silicon"}
                          {:Query    :Op
                           :distance 1
                           :operands [{:Query :Keyword
                                       :word  "film"}
                                      {:Query :Keyword
                                       :word  "is"}]}]}]})

(deftest proximity-search-test
  (testing "Proximity search test"
    (is (proximity-search sample-text query1))
    (is (proximity-search sample-text query2))
    (is (proximity-search sample-text query3))
    (is (proximity-search sample-text query4))
    (is (not (proximity-search sample-text query5)))
    (is (proximity-search sample-text query6))
    (is (not (proximity-search sample-text query7)))
    (is (proximity-search sample-text query8))))


(deftest match-by-index-test
  (let [words (tokenize sample-text)]
    (testing "Should match on word at given index and return match struct with index"
      (is (= {:Match :Keyword
              :word  "The"
              :index 0}
             (match-by-index words 0 {:Query :Keyword
                                      :word  "The"})))
      (is (= {:Match :Keyword
              :word  "invention"
              :index 1}
             (match-by-index words 1 {:Query :Keyword
                                      :word  "invention"})))
      (is (not (match-by-index words 1 {:Query :Keyword
                                        :word  "non-existing-word"}))))

    (testing "Should match on operator with distance and return match struct with index"
      (is (match-by-index words 0 {:Query   :Op
                                   :distance 1
                                   :operands [{:Query :Keyword
                                               :word  "The"}
                                              {:Query :Keyword
                                               :word  "invention"}]}))
      (is (= {:Match    :Op
              :distance 3
              :operands [{:Match :Keyword
                          :word  "discloses"
                          :index 2}
                         {:Match :Keyword
                          :word  "fiber"
                          :index 5}]}
             (match-by-index words 2 {:Query    :Op
                                      :distance 3
                                      :operands [{:Query :Keyword
                                                  :word  "discloses"}
                                                 {:Query :Keyword
                                                  :word  "fiber"}]}))))))
