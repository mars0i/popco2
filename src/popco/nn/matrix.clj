(ns popco.nn.matrix
  (require [clojure.core.matrix :as mx]))
;; matrix utility functions


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
