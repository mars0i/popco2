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


;; vectorz only supports Doubles, and ndarray defaults to Doubles.
;; If I want something else with ndarray, I need to replace some of
;; its functions with other ones.  But for use with vectorz, the
;; built-in core.matrix functions are best.  So there will be two
;; different versions of popco.nn.matrix--the usual one, which just
;; defines my functions as the original core.matrix functions, and
;; the special version designed for using ndarray with other sorts
;; of numbers.
(def zero-vector mx/zero-vector)
(def zero-matrix mx/zero-matrix)


;; vectorz only supports Doubles, and ndarray defaults to Doubles.
;; If I want something else with ndarray, I need to replace some of
;; its functions with other ones.  But for use with vectorz, the
;; built-in core.matrix functions are best.  So there will be two
;; different versions of popco.nn.matrix--the usual one, which just
;; defines my functions as the original core.matrix functions, and
;; the special version designed for using ndarray with other sorts
;; of numbers.
(def zero-vector mx/zero-vector)
(def zero-matrix mx/zero-matrix)
