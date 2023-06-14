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

(deftest query-test
  (testing "query function should work"
    (is (query sample-text "solar")                         true)
    (is (query sample-text "solar W1 panel")                true)
    (is (query sample-text "solar W2 panel")                true)
    (is (query sample-text "(solar W1 panel) W2 roof")      false)
    (is (query sample-text "car W9 (silicon W3 material)")  true)
    (is (query sample-text "(silicon W2 material) W2 film") false)))