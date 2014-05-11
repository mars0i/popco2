(ns popco.io.utils
  (:require [clojure.core.matrix :as mx]))

(defn doublify-matrix
  [mat]
  (mx/emap double mat))

(defn doublify-pers-propn-activns
  [pers]
  (assoc pers 
         :propn-activns 
         (doublify-matrix (:propn-activns pers))))

(defn doublify-popn-propn-activns
  [popn]
  (assoc popn
         :members 
         (map doublify-pers-propn-activns (:members popn))))
