(ns text-matcher.core)

(defn tokenize [text]
  (clojure.string/split text #" "))

;; (defn indexes-before [index distance]
;;   (range (- index distance) index))

(defn indexes-after [index distance]
  (range (inc index) (+ index (inc distance))))

(defmulti match-by-index
  (fn [_text _index query]
    (:Token query)))

(defmethod match-by-index :Word [text index word]
  (when (= (get text index)
           (get word :word))
    index))

(defmethod match-by-index :Op [text index operator]
  (let [[left-operand
         right-operand]     (:operands operator)
        distance            (:distance operator)]
    (when-let [left-operand-border-index (match-by-index text index left-operand)]
      (some #(match-by-index text % right-operand)
            (indexes-after left-operand-border-index distance)))))

(defn proximity-search [text query]
  (let [words (tokenize text)]
    (some #(match-by-index words % query) (range (count words)))))
