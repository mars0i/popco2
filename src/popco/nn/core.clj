(ns popco.nn.core
  (:require [utils.general :as ug]
            [clojure.core.matrix :as mx]))

;; Definitions of neural network types, associated functions, etc. for POPCO

;; NOTE: The two fields shared by the an analogy net structure and a
;; proposition net structure are :node-vec and :id-to-idx.  However, they
;; share the accessors wt-mat, pos-wt-mat, and neg-wt-mat.  

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

;; AnalogyNets store weight matrices, node activations, and associated semantic
;; information for a proposition network.
;; AnalogyNets are created with make-analogy-net in nn/analogy.clj
(defrecord AnalogyNet [pos-wt-mat neg-wt-mat node-vec id-to-idx ids-to-idx]
  NNMats
  (wt-mat [nnstru] (mx/add (:pos-wt-mat nnstru) (:neg-wt-mat nnstru)))
  (pos-wt-mat [nnstru] (:pos-wt-mat nnstru))
  (neg-wt-mat [nnstru] (:neg-wt-mat nnstru)))

(ug/add-to-docstr ->AnalogyNet
  "Makes an ACME analogy neural-net structure, i.e. a structure that represents an ACME 
  analogy constraint satisfaction network.  Has these fields:
  :pos-wt-mat -    A core.matrix square matrix with dimensions equal to the number of
                   nodes, representing positively weighted links.
  :neg-wt-mat -    A core.matrix square matrix with dimensions equal to the number of
                   nodes, representing negatively weighted links.
  :node-vec -      A Clojure vector of data providing information about the meaning
                   of particular neural net nodes.  Nodes represent possible mappings
                   between propositions and between components of propositions.
                   The indexes of the data items correspond to indexes into activation 
                   vectors and rows/columns of weight matrices.  This vector may be identical 
                   to the sequence of nodes passed in.
  :id-to-idx -     A Clojure map from ids of the same data items to integers, 
                   allowing lookup of a node's index from its id.
  :ids-to-idx -    This does roughly the same thing as :id-to-idx. The latter maps
                   mapnode ids to indexes into the node vector (or rows, or
                   columns of the matrices).  :ids-to-idx, by contrast, maps
                   vector pairs containing the ids of the two sides (from
                   which the mapnode id is constructed).  This is redundant 
                   information, but convenient.
  :propn-mn-to-mns -   A map from ids of propn-mapnodes to sets of ids of the 
                       associated component mapnodes (for humans).
  :propn-idx-to-idxs - A vector taking indexes of propn-mapnodes to sets of indexes of the
                       associated component mapnodes (for code).")

(declare posify negify)

;; PropnNets store a weight matrix, node activations, and associated semantic
;; information for a proposition network.
(defrecord PropnNet [wt-mat node-vec id-to-idx]
  NNMats
  (wt-mat [nnstru] (:wt-mat nnstru))
  (pos-wt-mat [nnstru] (mx/emap posify (:wt-mat nnstru)))
  (neg-wt-mat [nnstru] (mx/emap negify (:wt-mat nnstru))))

(ug/add-to-docstr ->PropnNet
  "Makes a proposition neural-net structure, i.e. a structure that represents a
  POPCO proposition constraint satisfaction network.  Has these fields:
  :wt-mat -      A core.matrix square matrix with dimensions equal to the number of
                 nodes, representing all links.
  :node-vec -    A Clojure vector of data providing information about the meaning
                 of particular neural net nodes.  The indexes of the data items
                 correspond to indexes into activation vectors and rows/columns
                 of weight matrices.  This vector may be identical to the sequence
                 of nodes passed in.
  :id-to-idx -   A Clojure map from ids of the same data items to integers, 
                 allowing lookup of a node's index from its id." )

(defn posify 
  "Return the non-negative number closest to x, i.e. 0 if x < 0, else x."
  [x]
  (max 0 x))

(defn negify
  "Return the non-positive number closest to x, i.e. 0 if x > 0, else x."
  [x]
  (min 0 x))

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
  "Given a sequence of data on individual nodes, returns a clojure map with 
  these entries:
  :node-vec -    A Clojure vector of data providing information about the meaning
                 of particular neural net nodes.  The indexes of the data items
                 correspond to indexes into activation vectors and rows/columns
                 of weight matrices.  This vector may be identical to the sequence
                 of nodes passed in.
  :id-to-idx -   A Clojure map from ids of the same data items to integers, 
                 allowing lookup of a node's index from its id."
  [node-seq]
  { :node-vec (vec node-seq)
    :id-to-idx (make-id-to-idx-map (map :id node-seq)) }) ; index order will be same as node-seq's order
