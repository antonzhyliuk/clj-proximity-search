(ns text-matcher.core
  (:require
   [clojure.string :as str]))

(defn tokenize [text]
  (vec (str/split text #" ")))

(defn indexes-left [index distance]
  (range (- index distance)
         index))

(defn indexes-right [index distance]
  (range (inc index)
         (+ index (inc distance))))

;;; Right index for Match structures

(defmulti get-index :Match)

(defmethod get-index :Keyword [kw _direction]
  (:index kw))

(defmethod get-index :Op [op direction]
  (let [[left right] (:operands op)]
    (condp = direction
      :right (get-index right direction)
      :left  (get-index left direction))))

;;; Match by index for Query structures

(defmulti match-index
  (fn [_text _index query]
    (:Query query)))

(defmethod match-index :Keyword [words index kw]
  (when (= (get words index)
           (:word kw))
    {:Match :Keyword
     :word  (:word kw)
     :index index}))

(defmethod match-index :Within [words index operator]
  (let [[left-operand right-operand] (:operands operator)
        distance                     (:distance operator)]
    (when-let [left-match (match-index words index left-operand)]
      (when-let [right-match (some (fn [index]
                                     (match-index words index right-operand))
                                   (indexes-right (get-index left-match :right) distance))]
        {:Match    :Op
         :distance distance
         :operands [left-match right-match]}))))

(defmethod match-index :Near [words index operator]
  (let [[left-operand right-operand] (:operands operator)
        distance                     (:distance operator)]
    (when-let [left-match (match-index words index left-operand)] 
      (when-let [right-match (some (fn [index]
                                     (match-index words index right-operand))
                                   (concat (indexes-right (get-index left-match :right) distance)
                                           (indexes-left (get-index left-match :left) distance)))]
        {:Match    :Op
         :distance distance
         :operands [left-match right-match]}))))

(defn proximity-search [text query]
  (let [words (tokenize text)]
    (some (fn [index]
            (match-index words index query))
          (range (count words)))))
