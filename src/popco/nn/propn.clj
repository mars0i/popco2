;; propn.clj
;; Functions for creating and working with PropnNets.
;; Further documentation on PropnNets can be read and maintained in net.clj
;; See lot.clj for documentation on Propns and their components.

(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [utils.general :as ug]
            [clojure.core.matrix :as mx])
  (:import [popco.core.lot Propn]
           [popco.nn.nets PropnNet]))

(declare make-propn-to-extended-descendant-propn-idxs
         propn-extended-descendant-propns make-propn-to-extended-fams-ids
         make-propn-net new-sem-wt-mat)

;; TODO APPLY SEMANTIC-IFFS
(defn make-propn-net
  "Constructs a proposition netword object with fields specified in doctrings
  of ->Propn-net and make-nn-core."
  [propnseq sem-iffs sem-ifs]
  (let [node-seq (cons {:id :SALIENT} propnseq)
        num-nodes (count node-seq)
        nncore (nn/make-nn-core node-seq)
        id-to-idx (:id-to-idx nncore)
        propn-map (assoc 
                  nncore
                  :wt-mat (mx/zero-matrix num-nodes num-nodes)
                  :sem-wt-mat (new-sem-wt-mat id-to-idx sem-iffs sem-ifs)
                  :propn-to-descendant-propn-idxs (make-propn-to-extended-descendant-propn-idxs 
                                                    node-seq id-to-idx))]
    (nn/map->PropnNet
      (assoc propn-map
             :propn-to-extended-fams-ids (make-propn-to-extended-fams-ids propn-map)))))

(defn clone
  "Make a PropnNet pnet that has its own new copy of pnet's wt-mat matrix, 
  but possibly sharing pnet's other data structures."
  [pnet]
  (assoc pnet 
         :wt-mat 
         (mx/matrix (:wt-mat pnet))))

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

(defn new-sem-wt-mat
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
      (mx/mset! mat (id-to-idx id1) (id-to-idx id2) wt))    ; unidirectional: activation will flow from the id2 propn to the id1 propn
    mat))
