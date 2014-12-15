;; Twice as slow as maxes using recur
(defn- max-collector
  "Helper function for maxes."
  [f collected new-elt]
  (let [best-elt (first collected)
        best-val (f best-elt)
        new-val (f new-elt)]
    (cond
      (== best-val new-val) (conj collected new-elt)
      (> new-val best-val) [new-elt]
      :else collected)))
(defn reduce-maxes
  "Returns a sequence of elements from coll, each of which has the same maximum
  value of (f element), or (identity element) if f is not provided."
  ([coll] (maxes identity coll))
  ([f coll]
   (if (empty? coll)
     coll
     (reduce (partial max-collector f) [(first coll)] (rest coll)))))


;(defn collect
;  "If args are collections, concats them; if neither is, creates a collection 
;  containing the two args; otherwise conjs the non-coll onto the coll."
;  [x y]
;  (cond
;    (and (coll? x) (coll? y)) (concat x y)
;    (coll? x) (conj x y)
;    (coll? y) (conj y x)
;    :else [x y]))

;; Quick tests show suggest that this may be only slightly slower than the non-lazy version,
;; even though it has to traverse the input twice--and even if you map doall over the result.
;; Doing something like the recur version with lazy-seq embedded in it seems slower than this one.
;(defn lazy-split-elements
;  "Given a collection of pairs, returns a pair of two sequences, one containing
;  the first elements of the pairs, in order, and the other containing the
;  second elements of the pairs, in order.  Note that if the input collection
;  is empty, split-elements returns a pair containing two empty sequences."
;  [pairs]
;  (list (map first pairs) (map second pairs)))

;; Version with recur and lazy split
;(defn lazy-split-elements2
;  "Given a collection of pairs, returns a pair of two sequences, one containing
;  the first elements of the pairs, in order, and the other containing the
;  second elements of the pairs, in order.  Note that if the input collection
;  is empty, split-elements returns a pair containing two empty sequences."
;  [pairs]
;  (loop [prs pairs
;         firsts []
;         seconds []]
;    (if (empty? prs)
;      (list firsts seconds)
;      (let [[fst snd] (first prs)]
;        (recur (rest prs)
;               (cons fst (lazy-seq firsts))
;               (cons snd (lazy-seq seconds)) )))))

