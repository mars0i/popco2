;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.io.utils
  (:require [clojure.core.matrix :as mx]))

(defn doublify-matrix
  [mat]
  (mx/emap double mat))

(defn doublify-pers-propn-activns
  [pers]
  (let [pnet (:propn-net pers)]
    (assoc pers 
           :propn-net (assoc pnet 
                             :activns (doublify-matrix 
                                        (:activns pnet))))))

(defn doublify-popn-propn-activns
  [popn]
  (assoc popn
         :persons 
         (map doublify-pers-propn-activns (:persons popn))))
