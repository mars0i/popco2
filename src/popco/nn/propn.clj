(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]
            [clojure.core.matrix :as mx])
  (:import [popco.core.lot Propn]
           [popco.nn.nets PropnNet]))


(declare make-propn-to-extended-descendant-propn-idxs propn-extended-descendant-propns make-propn-to-extended-fams-idxs make-propn-net)

;; TODO Add the SALIENT node
(defn make-propn-net
  [propnseq]
  (let [dim (count propnseq)
        nncore (nn/make-nn-core propnseq)
        pnetmap (assoc 
                  nncore
                  :wt-mat (mx/new-matrix dim dim)
                  :propn-to-descendant-propn-idxs (make-propn-to-extended-descendant-propn-idxs 
                                                    propnseq (:id-to-idx nncore)))]
    (nn/map->PropnNet
      (assoc pnetmap
             :propn-to-extended-fams-idxs (make-propn-to-extended-fams-idxs pnetmap)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO NOT VERIFIED
(defn make-propn-to-extended-fams-idxs
  "Create a map from each propn id to sets of indexes of all propns that bear 
  extended family relations to it, whether upward or downward."
  [pnet]
  (let [idx-to-id (:id-vec pnet)
        idx-fams (vals (:propn-to-descendant-propn-idxs pnet))] ; we already have extended fams as idxs, so use them
    (group-by 
      (comp idx-to-id first)            ; group/hashmap on id of the first idx
      (mapcat ug/rotations idx-fams)))) ; list all rotations of each idx fam in order to put each id first

(defn make-propn-to-extended-descendant-propn-idxs
  "Create a map from propn ids to seqs of indexes into the propn vector.
  For propn P, the seq contains its index, the indexes of any propns that
  are its args, propns that are their args, etc.  First arg is a collection
  of propns. Second arg is a map (or function) from propn ids to indexes
  into the propn vector."
  [propns id-to-idx]
    (zipmap 
      (map :id propns) 
      (map #(map id-to-idx %)
           (map propn-extended-descendant-propns propns))))

(defn propn-extended-descendant-propns
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
