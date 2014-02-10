(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]
            [clojure.core.matrix :as mx]
            [clojure.math.combinatorics :as comb])
  (:import [popco.core.lot Propn]
           [popco.nn.nets PropnNet]))


(declare make-propn-to-extended-descendant-propn-idxs propn-extended-descendant-propns make-propn-net)

;; TODO Add the SALIENT node
(defn make-propn-net
  [propnseq]
  (let [dim (count propnseq)
        nncore (nn/make-nn-core propnseq)]
    (nn/map->PropnNet
      (assoc 
        nncore
        :wt-mat (mx/new-matrix dim dim)
        :propn-to-descendant-propn-idxs (make-propn-to-extended-descendant-propn-idxs propnseq 
		                                                     (:id-to-idx nncore))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO I think this little monster defines a map from propn ids to
;; indexes of all propns that bear extended family relations to, whether
;; upward or downward.  Not sure if it does it correctly, or if this is
;; exactly what I need in receive-propn.  Probably needs refactoring anyway.
;; OK: the simple call to 'merge' is wrong.  Also, calling 'permutations' is overkill,
;; since the only reason I'm using it is to get different indexes into the
;; first position--I don't care about the order of the rest of the indexes.
;; Instead I should use clojure.contrib.seq-utils/rotations, if I'm using this
;; algorithm.  When I merge, I need to list as separate subseqs, each of the
;; different sets of indexes associated with the first index.
(defn make-extended-family-propn-idxs
  [pnet]
  (apply merge 
         (map #(hash-map (:id ((:node-vec pnet) (first %))) %) 
              (apply concat 
                     (map comb/permutations 
                          (vals (:propn-to-descendant-propn-idxs pnet)))))))

; (defn yo [pnet] (map #(hash-map (:id ((:node-vec pnet) (first %))) (rest %)) (apply concat (map comb/permutations (vals (:propn-to-descendant-propn-idxs pnet))))))

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
