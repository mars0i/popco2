(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]
            [clojure.core.matrix :as mx])
  (:import [popco.core.lot Propn]
           [popco.nn.nets PropnNet]))


(declare make-propn-to-extended-family-propn-idxs propn-extended-family-propns make-propn-net)

;; TODO Add the SALIENT node
(defn make-propn-net
  [propnseq]
  (let [dim (count propnseq)
        nncore (nn/make-nn-core propnseq)]
    (nn/map->PropnNet
      (assoc 
        nncore
        :wt-mat (mx/new-matrix dim dim)
        :propn-to-family-propn-idxs (make-propn-to-extended-family-propn-idxs propnseq 
		                                                     (:id-to-idx nncore))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-propn-to-extended-family-propn-idxs
  "Create a map from propn ids to seqs of indexes into the propn vector.
  For propn P, the seq contains its index, the indexes of any propns that
  are its args, propns that are their args, etc.  First arg is a collection
  of propns. Second arg is a map (or function) from propn ids to indexes
  into the propn vector."
  [propns id-to-idx]
    (zipmap 
      (map :id propns) 
      (map #(map id-to-idx %)
           (map propn-extended-family-propns propns))))

(defn propn-extended-family-propns
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
