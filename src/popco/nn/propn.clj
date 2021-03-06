;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; propn.clj
;; Functions for creating and working with PropnNets.
;; NOTE: The Proposition record type is defined in popco.nn.nets.
;; Further documentation on PropnNets can be read and maintained in net.clj
;; See lot.clj for documentation on Propns and their components.

(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [utils.general :as ug]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx])
  (:import [popco.core.lot Propn]
           [popco.nn.nets PropnNet]))

(declare make-propn-to-extended-descendant-propn-idxs
         propn-extended-descendant-propns make-propn-to-extended-fams-ids
         make-propn-net new-linger-wt-mat)

;; NOTE: The Proposition record type is defined in popco.nn.nets.

;; non-lazy
;; By default, propn net's link-mat is empty (contains nil).  An external function applied 
;; to the popn can be used to fill it, if desired (e.g. by mapping over ticks).  Otherwise 
;; we use the regular wt-mat as the value of links.  This can occasionally hide what should 
;; be considered a link even though it has weight zero.  See github issue #6 for discussion.
(defn make-propn-net
  "Constructs a proposition netword object with fields specified in doctrings
  of ->Propn-net and make-nn-core."
  [propnseq sem-iffs sem-ifs]
  (let [node-seq (cons {:id :SALIENT} (sort #(compare (:id %1) (:id %2)) propnseq)) ; sort will put everything in alphabetical order
        num-nodes (count node-seq)
        nncore (nn/make-nn-core node-seq)
        id-to-idx (:id-to-idx nncore)
        propn-map (assoc 
                  nncore
                  :mask     (mx/zero-vector num-nodes)
                  :activns  (mx/zero-vector num-nodes)
                  :all-wt-mat (mx/zero-matrix num-nodes num-nodes)
                  :linger-wt-mat (new-linger-wt-mat id-to-idx sem-iffs sem-ifs)
                  :link-mat nil ; by default this is "empty".  it can be filled by an external function applied to the population.
                  :propn-to-descendant-propn-idxs (make-propn-to-extended-descendant-propn-idxs 
                                                    node-seq id-to-idx))]
    (nn/map->PropnNet
      (assoc propn-map
             :propn-to-extended-fams-ids (make-propn-to-extended-fams-ids propn-map)))))

(defn clone
  "Make a PropnNet pnet that has its own new copy of pnet's mask, activns, and 
  wt-mat matrix, but possibly sharing pnet's other data structures."
  [pnet]
  (assoc pnet 
         :mask          (mx/clone (:mask pnet))
         :activns       (mx/clone (:activns pnet))
         :all-wt-mat    (mx/matrix (:all-wt-mat pnet))
         :linger-wt-mat (mx/matrix (:linger-wt-mat pnet))
         :link-mat (if-let [link-mat (:link-mat pnet)]
                     (mx/matrix link-mat) ; make fresh copy of link-mat
                     nil))) ; source link-mat is nil, too

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-propn-to-extended-fams-ids
  "Create a map from each propn id to a vector of seqs of ids (not indexes) of propns.
  Each such seq records the ids of propns in one extended family of which the key propn
  is a member, whether it is the matriarch of the family or not."
  [pnet]
  (let [idx-to-id (:id-vec pnet)
        idx-fams (vals (:propn-to-descendant-propn-idxs pnet))] ; we already have extended fams as idxs, so use them
    (group-by 
      first            ; group/hashmap on id of the first idx
      (mapcat ug/rotations 
              (map #(map idx-to-id %) idx-fams))))) ; list all rotations of each idx fam in order to put each id first

(defn make-propn-to-extended-descendant-propn-idxs
  "Create a map from propn ids to seqs of indexes into the propn vector (not ids).
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

(defn new-linger-wt-mat
  "Creates and returns a new matrix that's zero except for weights of elements
  chosen by ids in each semantic iff specification in iffs, and each semantic
  if specifcation in ifs. Specifications have the form [wt id1 id2].  'iff' 
  specifications create bidirectional links (symlinks).  'if' specifications 
  create a directional link from the id2 propn to the id1 propn.  (That may
  seem backwards, but it matches the direction between matrix indexes given
  how matrix multiplication is done in popco2."
  [id-to-idx iffs ifs]
  (let [dim (count id-to-idx) ; note count is O(1) on a map
        mat (mx/zero-matrix dim dim)]
    (doseq [[wt id1 id2] iffs]
      (nn/symlink! mat (id-to-idx id1) (id-to-idx id2) wt)) ; bidirectional links--activation goes both ways
    (doseq [[wt id1 id2] ifs]
      (nn/dirlink! mat (id-to-idx id1) (id-to-idx id2) wt))    ; unidirectional: activation will flow from the id2 propn to the id1 propn
    mat))

(def extract-salient-wts (comp 
                           (partial mx/matrix :persistent-vector) 
                           first 
                           mx/columns 
                           nn/wt-mat 
                           :propn-net 
                           `first 
                           :persons))
