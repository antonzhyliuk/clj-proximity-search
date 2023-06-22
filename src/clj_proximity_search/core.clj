(ns clj-proximity-search.core)

;;; Indexes computation

(defn indexes-left [index distance]
  (range (- index distance)
         index))

(defn indexes-right [index distance]
  (range (inc index)
         (+ index (inc distance))))

;;; Right index for Match structures

(defmulti get-match-index :Match)

(defmethod get-match-index :Keyword [kw _direction]
  (:index kw))

(defmethod get-match-index :Op [op direction]
  (let [{[left right] :operands} op]
    (condp = direction
      :right (get-match-index right direction)
      :left  (get-match-index left direction))))

;;; Match tokens vector with index Query structures

(defmulti match-query
  (fn [_text _index query]
    (:Query query)))

(defmethod match-query :Keyword [tokens index kw]
  (let [token (:token kw)]
    (when (= token (get tokens index))
      {:Match :Keyword
       :token token
       :index index})))

(defmethod match-query :Op [tokens index op]
  (let [{[left-operand right-operand] :operands
         :keys [operator distance]} op]
    (when-let [left-match (match-query tokens index left-operand)]
      (let [possible-right-match-indexes (condp = operator
                                           :within (indexes-right (get-match-index left-match :right) distance)
                                           :near (concat (indexes-right (get-match-index left-match :right) distance)
                                                         (indexes-left (get-match-index left-match :left) distance)))]
        (when-let [right-match (some (fn [index]
                                       (match-query tokens index right-operand))
                                     possible-right-match-indexes)]
          {:Match    :Op
           :distance distance
           :operands [left-match right-match]})))))

;;; Top level function

(defn proximity-search [tokens query] 
  (some (fn [index]
          (match-query tokens index query))
        (range (count tokens))))
