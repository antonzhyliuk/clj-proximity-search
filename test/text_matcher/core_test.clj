(ns text-matcher.core-test
  (:require [clojure.test :refer :all]
            [text-matcher.core :refer :all]))

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

(def query1 ;; solar
  {:Token :Word
   :word  "solar"})

(def query2 ;; solar W1 panel
  {:Token    :Op
   :distance 1
   :operands [{:Token :Word
               :word  "solar"}
              {:Token :Word
               :word  "panel"}]})

(def query3 ;; solar W2 panel
  {:Token    :Op
   :distance 2
   :operands [{:Token :Word
               :word  "solar"}
              {:Token :Word
               :word  "panel"}]})

(def query4 ;; (solar W1 panel) W2 roof"
  {:Token    :Op
   :distance 2
   :operands [{:Token    :Op
               :distance 1
               :operands [{:Token :Word
                           :word  "solar"}
                          {:Token :Word
                           :word  "panel"}]}
              {:Token :Word
               :word  "roof"}]})

(def query5 ;; (solar W1 panel) W1 roof"
  {:Token    :Op
   :distance 1
   :operands [{:Token    :Op
               :distance 1
               :operands [{:Token :Word
                           :word  "solar"}
                          {:Token :Word
                           :word  "panel"}]}
              {:Token :Word
               :word  "roof"}]})

(def query6 ;; car W9 (silicon W3 material)
  {:Token    :Op
   :distance 9
   :operands [{:Token :Word
               :word  "car"}
              {:Token    :Op
               :distance 3
               :operands [{:Token :Word
                           :word  "silicon"}
                          {:Token :Word
                           :word  "material"}]}]})

(def query7 ;; (silicon W2 material) W2 film
  {:Token    :Op
   :distance 2
   :operands [{:Token    :Op
               :distance 2
               :operands [{:Token :Word
                           :word  "silicon"}
                          {:Token :Word
                           :word  "material"}]}
              {:Token :Word
               :word  "film"}]})

(deftest proximity-search-test
  (testing "Proximity search test"
    (is (proximity-search sample-text query1))
    (is (proximity-search sample-text query2))
    (is (proximity-search sample-text query3))
    (is (proximity-search sample-text query4))
    (is (not (proximity-search sample-text query5)))
    (is (proximity-search sample-text query6))
    (is (not (proximity-search sample-text query7)))))


(deftest match-by-index-test
  (let [words (tokenize sample-text)]
    (testing "Should match on word at given index and return word's index"
      (is (= 0 (match-by-index words 0 {:Token :Word :word "The"})))
      (is (= 1 (match-by-index words 1 {:Token :Word :word "invention"})))
      (is (not (match-by-index words 1 {:Token :Word :word "vozhyk"}))))

    (testing "Should match on operator with distance and return index of right operand"
      (is (match-by-index words 0 {:Token :Op :distance 1 :operands [{:Token :Word :word "The"}
                                                                     {:Token :Word :word "invention"}]}))
      (is (= 5 (match-by-index words 2 {:Token :Op :distance 3 :operands [{:Token :Word :word "discloses"}
                                                                          {:Token :Word :word "fiber"}]}))))))
