;; Utility functions for use with clojure.core.matrix
(ns utils.matrix
  (require [clojure.core.matrix :as mx]))

;; Wrappers around core.matrix functions that do additional things:

(defn zero-vector
  "Returns a vector of size size, filled with zeros of the same type as zero."
  [size zero]
  (let [zv (mx/zero-vector size)]
    (when (not= zero (mx/mget zv 0 0))
      (mx/fill! zv zero))
    zv))
