(ns popco.nn.nets
  (:require [utils.general :as ug]
            [popco.core.lot :as lot]
            [popco.core.constants :as cn]
            [popco.nn.matrix :as pmx]
            [clojure.core.matrix :as mx]
            [clojure.pprint :as pp]))

;; Definitions of neural-net data types and related functions.
;; Defines AnalogyNet and PropnNet, and definitions that are common 
;; to both analogy networks and proposition networks or that are very 
;; closely related.  Definitions specific to the two kinds of networks
;; can be found in analogy.clj and propn.clj.

;; NOTE: Two fields shared by the an analogy net structure and a
;; proposition net structure are :node-vec and :id-to-idx.  However, they
;; share the accessors wt-mat, pos-wt-mat, and neg-wt-mat.  

;; NOTE All uses of mset! should be confined to this file.
;; There are also some core.matrix ! ops in update.clj.

(declare posify negify)

(defprotocol NNMats
  "Protocol for access to matrices in an nnstru, i.e. a neural-network 
  structure.  Defines accessors for the matrices in neural-net structures, 
  but not other fields.  It's efficient and simple for the analogy net to
  store two separate matrices, one for positive and one for negative weights,
  and sum them when necessary, while it's simpler for the
  proposition network to store a single neural net, and extra the
  positive and negative aspects of it when needed."
  (wt-mat [nnstru] "Returns the nnstru's full (positive and negative) weight matrix.") 
  (pos-wt-mat [nnstru] "Returns the nnstru's positive-only weight matrix.")
  (neg-wt-mat [nnstru] "Returns the nnstru's negative-only weight matrix.")
  (links [nnstru]
    "Returns a clojure.core.matrix in which nonzero entries represent the existence
    of neural network links/edges, and zero entries represent that there is no
    link between the two nodes that index the entry.  Note that entries representing
    links can have any nonzero value, including negative values.  (This function needed
    because proposition networks can include zero-weight links.)"))

;; AnalogyNets store weight matrices, node activations, and associated semantic
;; information for a proposition network.
;; AnalogyNets are created with make-analogy-net in nn/analogy.clj
(defrecord AnalogyNet [pos-wt-mat neg-wt-mat node-vec id-to-idx ids-to-idx]
  NNMats
  (wt-mat [nnstru] (mx/add (:pos-wt-mat nnstru) (:neg-wt-mat nnstru)))
  (pos-wt-mat [nnstru] (:pos-wt-mat nnstru))
  (neg-wt-mat [nnstru] (:neg-wt-mat nnstru))
  (links [nnstru] (wt-mat nnstru))) ; Analogy nets never have zero-weight links, so we can use the regular weight matrix.

(ug/add-to-docstr ->AnalogyNet
  "Makes an ACME analogy neural-net structure, i.e. a structure that 
  represents an ACME analogy constraint satisfaction network.  See docstring 
  for make-analogy-net for details.")

;; PropnNets store a weight matrix, node activations, and associated semantic
;; information for a proposition network.
;; By default, propn net's link-mat is empty (contains nil).  An external function applied 
;; to the popn can be used to fill it, if desired.  Otherwise 
;; we use the regular wt-mat as the value of links.  This can occasionally hide what should 
;; be considered a link even though it has weight zero.  See github issue #6 for discussion.
(defrecord PropnNet [wt-mat linger-wt-mat link-mat node-vec id-to-idx]
  NNMats
  (wt-mat [nnstru] (:wt-mat nnstru))
  (pos-wt-mat [nnstru] (mx/emap posify (:wt-mat nnstru)))
  (neg-wt-mat [nnstru] (mx/emap negify (:wt-mat nnstru)))
  (links [nnstru] (or (:link-mat nnstru) (wt-mat nnstru)))) ; By default, propn net's link-mat is empty (contains nil).  An external function applied to the popn can be used to fill it.

;; TODO DOCSTRING IS OBSOLETE?
(ug/add-to-docstr ->PropnNet
  "Makes a proposition neural-net structure, i.e. a structure that represents a
  POPCO proposition constraint satisfaction network.  Has these fields in addition
  to those documented for make-nn-core:
  :wt-mat -      A core.matrix square matrix with dimensions equal to the number
                 of nodes, representing all links.
  :linger-wt-mat -  A core.matrix square matrix with dimensions equal to the number
                 of nodes, representing the lingering semantic, perceptual, and
                 conversational influences on link weights.  (Contrast with the
                 effect of analogy net activations of propn-map-nodes, which is
                 recreated from scratch on every tick.)
  :link-mat      nil by default, but can be filled with a core.matrix matrix that
                 represents non-links by zero, and links by other values.  This allows
                 distinguishing proposition net links that have weight zero from non
                 links.  Filling should be done with an 'external' program, i.e. one
                 that is not normally run but that can be applied to a population,
                 returning a population in which the link-mats of the proposition nets
                 of one or more persons have been filled.  This allows keeping track of
                 zero-weight links by external network display routines.
  :propn-to-descendant-propn-idxs - a map from each propn id to a seq of 
                 of indexes from the propn's descendant propns.
  :propn-to-extended-fams-ids - a map from each propn id to a vec of seqs
                 of indexes from the propn's extended family propns.  Note there
                 can be more than one such extended family.
  Note: wt-mat will be a sum of the persistent weights in linger-wt-mat, and 
  weights determined by activation values in the analogy net (clipped to the min
  and max).")

(defn posify 
  "Return the non-negative number closest to x, i.e. 0 if x < 0, else x."
  [x]
  (max 0.0 x))

(defn negify
  "Return the non-positive number closest to x, i.e. 0 if x > 0, else x."
  [x]
  (min 0.0 x))

(defn make-id-to-idx-map
  "Given a sequence of things, returns a map from things to indexes.  
  Allows reverse lookup of indexes from the things."
  [ids]
  (zipmap ids (range (count ids))))

;; Both analogy nets and proposition nets have nodes and links between them.
;; That means that both have (1) information on the meaning of each node
;; and (2) a mapping from nodes to row or column indexes (same thing)
;; and back, to keep track of the relationship between nodes  and their
;; links.  This function initializes (A) a vector of node info, which are
;; maps containing, at least, an :id, so that node info can be looked  up
;; from indexes; (B) a map from node ids to indexes, so that indexes can
;; be looked up from nodes.
(defn make-nn-core
  "Given a sequence of data on individual nodes that contain (at least) an :id
  field, returns a clojure map with these entries:
  :node-vec -    A Clojure vector of data providing information about the meaning
                 of particular neural net nodes.  The indexes of the data items 
                 correspond to indexes into activation vectors and rows/columns
                 of weight matrices.  This vector may be identical to the sequence
                 of nodes passed in.
  :id-vec -      Same as node-vec, but just the ids, not the whole data structures.
  :id-to-idx -   A Clojure map from ids of the same data items to integers, 
                 allowing lookup of a node's index from its id."
  [node-seq]
  (let [id-to-idx (make-id-to-idx-map (map :id node-seq))] ; index order will be same as node-seq's order
    {:node-vec (vec node-seq)
     :id-vec (vec (map :id node-seq))
     :id-to-idx id-to-idx } ))

;; These next two are functionally identical wrappers for mset!:

(defn clip-to-extrema
  "Returns -1 if x < -1, 1 if x > 1, and x otherwise."
  [x]
  (max -1.0 (min 1.0 x)))

(defn set-activn!
  "Given a core.matrix vector representing a set of activations,
  and an index into the vector, set the indexed element to value v."
  [activn-mat idx v]
  (mx/mset! activn-mat idx v))

(defn set-mask!
  "Given a core.matrix vector representing a mask, and an index
  into the mask, set the indexed element of the mask to value v."
  [mask idx v]
  (mx/mset! mask idx v))

(defn unmask!
  "Given a core.matrix vector representing a mask, and an index
  into the mask, set the indexed element of the mask to 1."
  [mask idx]
  (set-mask! mask idx 1.0))

(defn node-unmasked?
  "Given a core.matrix vector representing a mask, and an index
  into the mask, return true if the mask is 1 at that index;
  otherwise false."
  [mask idx]
  (= 1.0 (mx/mget mask idx)))

(defn node-masked?
  "Given a core.matrix vector representing a mask, and an index
  into the mask, return true if the mask is 0 at that index;
  otherwise false."
  [mask idx]
  (= 0.0 (mx/mget mask idx)))

(defn mask-matrix
  "Given a weight matrix and a mask vector (1-dimensional, i.e. not a 1xN row 
  matrix or Nx1 column matrix), return a weight matrix with nonzero entries
  only between unmasked nodes."
  [mat mask-vec]
  (mx/emul mat
           (mx/mmul (mx/column-matrix mask-vec)  ; construct matrix with 1's at entries
                    (mx/row-matrix mask-vec))))  ; whose coordinates are unmasked

;; Note: We make symlinks other ways as well, e.g. nn.analogy/add-wts-to-mat! .
(defn symlink!
  "Create a symmetric link by setting mat to wt-val from i to j and from j to i."
  [mat i j wt-val]
  (mx/mset! mat i j wt-val)
  (mx/mset! mat j i wt-val))

;; This is nothing more than a wrapper around mset!.  However, it allows
;; temporarily replacing its contents with mset in cases in which it's useful
;; e.g. to experiment with using persistent-vector rather than vectorz.
(defn dirlink!
  "Create a directional link by setting mat to wt-val from from j to i.
  (See update.md for discussion of directionality).  Normally, this is
  just a wrapper around core.matrix/mset! ."
  [mat i j wt-val]
  (mx/mset! mat i j wt-val))

(defn link-from-feeder-node!
  [mat i wt-val]
  (dirlink! mat i cn/+feeder-node-idx+ wt-val))

(defn add-from-feeder-node!
  [mat i wt-val]
  (link-from-feeder-node! mat i 
                          (clip-to-extrema 
                            (+ wt-val 
                               (mx/mget mat i cn/+feeder-node-idx+)))))

(defn symlink-to-idxs!
  "Create symmetric links between index i and every element in js by setting
  mat to wt-val between i and j in js, and between j and i."
  [mat wt-val i js]
  (doseq [j js]
    (symlink! mat i j wt-val)))


;; This function concerns the relationship between the analogy net and the proposition
;; net, so it doesn't belong in the source file for either, although it uses both.
(defn make-analogy-idx-to-propn-idxs
  "Returns a Clojure map from indexes of proposition mapnodes in the analogy network
  anet to pairs of indexes of nodes in the proposition network pnet.  This can be
  used to update proposition network link weights from analogy network activations."
  [anet pnet]
  (let [a-id-to-idx (:id-to-idx anet)
        p-id-to-idx (:id-to-idx pnet)
        a-node-vec (:node-vec anet)
        a-propn-mapnodes (filter (comp lot/propn? :alog1) a-node-vec)] ; find the propn-mapnodes (since only propns are paired with propns, we only need to check :alog1)
    (apply merge
           (map #(hash-map (a-id-to-idx (:id %)) ; now that we know it's a propn mapnode, go back from id to index
                           [(p-id-to-idx (:id (:alog1 %)))   ; get propn net indexes
                            (p-id-to-idx (:id (:alog2 %)))]) ;  of mapnode sides
                a-propn-mapnodes))))

(defn display-feeder-wts-ids
  "For each person in popn, print to stdout the column of weights from 
  the feeder node in the neural network identified by net-keyword, and 
  return the population unchanged."
  [net-keyword popn]
  (let [persons (:persons popn)
        id-vec (:id-vec (net-keyword (first persons)))]
    (doall 
      (map 
        (comp (partial pmx/print-vec-with-labels id-vec) pmx/col1 wt-mat net-keyword #(do (println (:id %)) %))
        persons))
    popn))

(def display-salient-wts-ids (partial display-feeder-wts-ids :propn-net))
(def display-semantic-wts-ids (partial display-feeder-wts-ids :analogy-net))

(defn salient-wts
  "Return a vector containing weights of links to the SALIENT node 
  in the proposition net."
  [pers]
  (pmx/col1 (:wt-mat (:propn-net pers))))

(defn semantic-wts
  "Return a vector containing weights of links to the SEMANTIC node 
  in the proposition net."
  [pers]
  (pmx/col1 (:wt-mat (:analogy-net pers))))

(defn display-salient-wts [popn] (doall (map println (map salient-wts (:persons popn)))) (flush) popn)

(defn display-semantic-wts
  [popn]
  (map println (map semantic-wts (:persons popn)))
  (flush)
  popn)

;; Useful for testing.  Not implemented as a display- function used for
;; mapping over the sequence of populations because it has to look at
;; pairs of subsequent populations.
(defn show-utterance-salient-effects
  "Given a sequence of populations, prints utterance map of population n-1
  followed by non-zero weights from SALIENT in the propn net in population n."
  ([popns] (show-utterance-salient-effects 0 popns))
  ([to-skip popns]
   (let [pnet (:propn-net (first (:persons (first popns))))
         node-vec (:node-vec pnet)]
     (doseq [[prev curr] (partition 2 1 popns)]
       (println)
       (pp/cl-format true "~s: " (:tick prev))
       (pp/pprint (:utterance-map prev))
       (pp/cl-format true "~s: " (:tick curr))
       (pp/pprint (map 
                    #(vector (:id %) 
                             (map (fn [[[idx] wt]] [(:id (node-vec idx)) (ug/round2 3 wt)]) ; round to strip spurious small decimals from float noise
                                  (pmx/non-zeros (salient-wts %))))
                    (drop to-skip (:persons curr))))))))

