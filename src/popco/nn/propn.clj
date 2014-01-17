(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]
            [clojure.core.matrix :as mx])
  (:import [popco.core.lot Propn]
           [popco.nn.nets PropnNet]))


(declare make-propn-to-family-propns propn-family-propns make-propn-net)

(defn make-propn-net
  [propnseq]
  (let [dim (count propnseq)
        nncore (nn/make-nn-core propnseq)]
    (nn/map->PropnNet
      (assoc 
        nncore
        :wt-mat (mx/new-matrix dim dim)
        :propn-to-family-propns (make-propn-to-family-propns propnseq (:id-to-idx nncore))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-propn-to-family-propns
  "Create a map from propn ids to seqs of indexes into the propn vector.
  For propn P, the seq contains its index, the indexes of any propns that
  are its args, propns that are their args, etc.  First arg is a collection
  of propns. Second arg is a map (or function) from propn ids to indexes
  into the propn vector."
  [propns id-to-idx]
    (zipmap 
      (map :id propns) 
      (map #(map id-to-idx %)
           (map propn-family-propns propns))))

(defn propn-family-propns
  "List the ids of this propn, propns that are its args, propns that 
  are their args, etc."
  [propn]
  (letfn [(fam-propns [args]
            (for [arg args 
                  :when (propn? arg)] 
              (cons (:id arg) (fam-propns (:args arg)))))]
    (distinct 
      (flatten 
        (cons (:id propn)
              (fam-propns (:args propn)))))))
