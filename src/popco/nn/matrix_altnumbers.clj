(ns popco.nn.matrix
  (require [clojure.core.matrix :as mx]
           [popco.core.constants :as cn]))
;; vectorz only supports Doubles, and ndarray defaults to Doubles.
;; If I want something else with ndarray, I need to replace some of
;; its functions with other ones.  But for use with vectorz, the
;; built-in core.matrix functions are best.  So there will be two
;; different versions of popco.nn.matrix--the usual one, which just
;; defines my functions as the original core.matrix functions, and
;; the special version designed for using ndarray with other sorts
;; of numbers.

(defn zero-vector
  "Returns a vector of size size, filled with zeros of the same type as +zero+."
  [size]
  (let [zv (mx/zero-vector size)]
    (mx/fill! zv cn/+zero+)
    zv))

(defn zero-matrix
  "Returns a matrix with dimensions rows, cols,, filled with zeros of the same type as +zero+."
  [rows cols]
  (let [zm (mx/zero-matrix rows cols)]
    (mx/fill! zm cn/+zero+)
    zm))
