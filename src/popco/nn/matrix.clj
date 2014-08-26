(ns popco.nn.matrix
  (require [clojure.core.matrix :as mx]))
;; matrix utility functions

(defn square-from-row
  "Make a symmetric matrix by multiplying a row matrix by itself.
  row-mat must be a *matrix* not a vector, i.e. it must be 2D 
  even though only one row."
  [row-mat]
  (mx/mmul (mx/transpose row-mat) row-mat))

(defn square-from-col
  "Make a symmetric matrix by multiplying a column matrix by itself.
  col-mat must be a *matrix* not a vector, i.e. it must be 2D 
  even though only one column."
  [col-mat]
  (mx/mmul col-mat (mx/transpose col-mat)))

(defn non-zero-index-val-pairs
  "Return [indices val] pairs for non-zero vals in matrix m.
  (See docstring for non-zeros.)"
  [m]
  (filter (comp not zero? second) 
          (map vector (mx/index-seq m) (mx/eseq m))))

(defn non-zeros
  "Gets the non-zero indices of an array mapped to the values.
  (By Matt Revelle at https://github.com/mikera/core.matrix/issues/102
  Something like this will probably be incorporated into core.matrix
  with the name non-zero-map.)"
  [m]
  (into {}
        (non-zero-index-val-pairs m)))

(defn non-zero-indices
  [m]
  (map first 
       (non-zero-index-val-pairs m)))

(defn non-zero-vals
  [m]
  (map second
       (non-zero-index-val-pairs m)))

(defn col1
  [m]
  (first (mx/columns m)))

(defn row1
  [m]
  (first (mx/rows m)))

(defn pm-with-breaks
  [m]
  (mx/pm m)
  (println)
  (flush))

;; EH
(defn pm-with-idxs
  [m]
  (let [width (first (mx/shape m))  ; assume vector or square mat
        idx-vec (mx/matrix (range width))]
    (mx/pm idx-vec)
    (mx/pm m)
    (println)
    (flush)))

(defn print-vec-with-labels
  [id-vec m]
  (doseq [i (range (count 
                     (mx/matrix :persistent-vector m)))]
    (println (mx/mget m i) (id-vec i))))
