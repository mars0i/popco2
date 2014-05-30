(ns popco.nn.nets
  (:require [utils.general :as ug]
            [popco.core.lot :as lot]
            [popco.core.constants :as cn]
            [popco.nn.matrix :as pmx]
            [clojure.core.matrix :as mx]))

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
  (wt-mat 
    [nnstru]
    "Returns the nnstru's full (positive and negative) weight matrix.") 
  (pos-wt-mat 
    [nnstru]
    "Returns the nnstru's positive-only weight matrix.")
  (neg-wt-mat 
    [nnstru]
    "Returns the nnstru's negative-only weight matrix.")
)

;; TODO since analogy net doesn't change, maybe I should have a third matrix
;; so that wt-mat doesn't have to calculate it.

;; AnalogyNets store weight matrices, node activations, and associated semantic
;; information for a proposition network.
;; AnalogyNets are created with make-analogy-net in nn/analogy.clj
(defrecord AnalogyNet [pos-wt-mat neg-wt-mat node-vec id-to-idx ids-to-idx]
  NNMats
  (wt-mat [nnstru] (mx/add (:pos-wt-mat nnstru) (:neg-wt-mat nnstru)))
  (pos-wt-mat [nnstru] (:pos-wt-mat nnstru))
  (neg-wt-mat [nnstru] (:neg-wt-mat nnstru)))

(ug/add-to-docstr ->AnalogyNet
  "Makes an ACME analogy neural-net structure, i.e. a structure that 
  represents an ACME analogy constraint satisfaction network.  See docstring 
  for make-analogy-net for details.")

;; PropnNets store a weight matrix, node activations, and associated semantic
;; information for a proposition network.
(defrecord PropnNet [wt-mat linger-wt-mat node-vec id-to-idx]
  NNMats
  (wt-mat [nnstru] (:wt-mat nnstru))
  (pos-wt-mat [nnstru] (mx/emap posify (:wt-mat nnstru)))
  (neg-wt-mat [nnstru] (mx/emap negify (:wt-mat nnstru))))

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
  (max cn/+zero+ x))

(defn negify
  "Return the non-positive number closest to x, i.e. 0 if x > 0, else x."
  [x]
  (min cn/+zero+ x))

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
  (max cn/+neg-one+ (min cn/+one+ x)))

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
  (set-mask! mask idx cn/+one+))

(defn node-unmasked?
  "Given a core.matrix vector representing a mask, and an index
  into the mask, return true if the mask is 1 at that index;
  otherwise false."
  [mask idx]
  (= cn/+one+ (mx/mget mask idx)))

(defn node-masked?
  "Given a core.matrix vector representing a mask, and an index
  into the mask, return true if the mask is 0 at that index;
  otherwise false."
  [mask idx]
  (= cn/+zero+ (mx/mget mask idx)))

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

(defn display-feeder-wts
  "For each person in popn, print to stdout the column of weights from 
  the feeder node in the neural network identified by net-keyword, and 
  return the population unchanged.  Inserts newline before and after
  each column (displayed as a row)."
  [net-keyword popn]
  (doall 
    (map 
      (comp pmx/pm-with-breaks pmx/col1 wt-mat net-keyword)
      (:persons popn)))
  popn)

(def display-salient-wts (partial display-feeder-wts :propn-net))
(def display-semantic-wts (partial display-feeder-wts :analogy-net))
