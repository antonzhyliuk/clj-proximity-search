(ns text-matcher.core
  (:require
   [clojure.string :as str]))

(defn tokenize [text]
  (vec (str/split text #" ")))

(defn indexes-after [index distance]
  (range (inc index)
         (+ index (inc distance))))

;;; Right index for Match structures

(defmulti right-index :Match)

(defmethod right-index :Keyword [kw]
  (:index kw))

(defmethod right-index :Op [op]
  (let [[_left right] (:operands op)]
    (right-index right)))

;;; Match by index for Query structures

(defmulti match-by-index
  (fn [_text _index query]
    (:Query query)))

(defmethod match-by-index :Keyword [words index kw]
  (when (= (get words index)
           (get kw :word))
    {:Match :Keyword
     :word  (get kw :word)
     :index index}))

(defmethod match-by-index :Op [words index operator]
  (let [[left-operand right-operand] (:operands operator)
        distance                     (:distance operator)]
    (when-let [left-match (match-by-index words index left-operand)] 
      (when-let [right-match (some (fn [index]
                                     (match-by-index words index right-operand))
                                   (indexes-after (right-index left-match) distance))]
        {:Match :Op
         :distance distance
         :operands [left-match right-match]}))))

(defn proximity-search [text query]
  (let [words (tokenize text)]
    (some (fn [index]
            (match-by-index words index query))
          (range (count words)))))
