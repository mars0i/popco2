(ns popco.nn.matrix
  (require [clojure.core.matrix :as mx]))
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
