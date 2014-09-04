(ns popco.io.gexf-dynamic
  (:require [clojure.data.xml :as x]
            [clojure.core.matrix :as mx]
            [popco.core.population :as popn]
            [popco.nn.nets :as nn]
            [popco.nn.matrix :as px]
            [utils.general :as ug]))

;; gut rehab.  see gexf_dynamic_old.clj, gexf_static.clj for other code

(defn net-with-tick
  [person-id net-key popn]
  (assoc (net-key (popn/get-person person-id popn)) :tick (:tick popn)))

(defn analogy-net-with-tick
  [person-id popn]
  (net-with-tick person-id :analogy-net popn))

(defn propn-net-with-tick
  [person-id popn]
  (net-with-tick person-id :propn-net popn))

;; pseudocode for nodes:
;; iterate through nets (need not be same type--could be both analogy and proposition)
;; in each net, run through all nodes
;; if node is new, add entry to node-data
;;   with key propn-id (or also person)
;;   and val+tick ([tick activn]).
;; if node is old, conj [tick activn] onto existing seq of [tick activn]s.

;; pseudocode for edges
;; iterate through nets
;; in each net, run through all edges
;; if edge is new, add entry to edge-data
;;   with constructed key representing the two ends (maybe also person as part of id)
;;   and weight+tick ([tick popco-wt]).
;; if edge is old, conj [tick popco-wt] onto existing seq of [tick popco-wt]s.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS USED FOR BOTH NODE AND EDGE DATA COLLECTION

(defn add-data
  [collected-data id-data-pair]
  (let [[id data] id-data-pair]
    (update-in collected-data
               [id] 
               (fnil conj []) data))) ; i.e. if the key id exists in node-data, conj data onto existing value; otherwise conj it onto an empty vec

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NODE DATA COLLECTION FUNCTIONS

(defn idx-to-node-data
  "Given an net and a seq containing a single index, returns a pair
  containing the node id corresponding to that index in that net,
  and the activation value of that node."
  [net-with-tick k]
  (let [[idx] k
        id (:id ((:node-vec net-with-tick) idx))
        tick (:tick net-with-tick)
        activn (mx/mget (:activns net-with-tick) idx)]
    [id [tick activn]]))

(defn unmasked-nonzero-idxs
  [net]
  (px/non-zero-indices (:mask net)))

(defn dynamic-node-data-from-net
  [node-data net-with-tick]
  (reduce add-data node-data
          (map (partial idx-to-node-data net-with-tick)
               (unmasked-nonzero-idxs net-with-tick))))

(defn dynamic-node-data-from-nets
  [node-data nets-with-ticks]
  (reduce dynamic-node-data-from-net node-data nets-with-ticks))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EDGE DATA COLLECTION FUNCTIONS

(defn idxs-to-edge-data 
  [net-with-tick [idx1 idx2 wt]]
  (let [node-vec (:node-vec net-with-tick) ; node-vec is a Clojure vector of Propns
        id #{(:id (node-vec idx1)) (:id (node-vec idx2))} ; use set as hashmap key so order of ids doesn't matter
        tick (:tick net-with-tick)]
    [id [tick wt]]))

(defn unmasked-non-zero-links
  "Returns a sequence of triplets containing indexes and wts from net's wt-mat
  whenever wt is nonzero and is between unmasked nodes.  Doesn't distinguish
  between directed and undirected links, and assumes that all links can be
  found in the lower triangle (including diagonal) of wt-mat."
  [net]
  (let [wt-mat (nn/wt-mat net)
        mask (:mask net)
        size (first (mx/shape mask))]
    (for [i (range size)
          j (range (inc i)) ; iterate through lower triangle including diagonal
          :let [wt (mx/mget wt-mat i j)]
          :when (and (not= 0.0 wt)
                     (pos? (mx/mget mask i))    ; mask values are never negative
                     (pos? (mx/mget mask j)))]  ;  (and almost always = 1)
      [i j wt])))

(defn dynamic-edge-data-from-net
  [edge-data net-with-tick]
  (reduce add-data edge-data
          (map (partial idxs-to-edge-data net-with-tick)
               (unmasked-non-zero-links net-with-tick))))

(defn dynamic-edge-data-from-nets
  [edge-data nets-with-ticks]
  (reduce dynamic-edge-data-from-net edge-data nets-with-ticks))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TOP-LEVEL FUNCTIONS

(defn dynamic-net-data
  "node-data and edge-data are hashes for data on subsequent states
  of nodes and edges, respectively.  nets-with-ticks contains one
  or more nets, each with an added tick (e.g. by net-with-tick).
  Example usage: (dynamic-net-data {} {} (analogy-net-with-tick :worf popn))"
  [node-data edge-data & nets-with-ticks]
  [(dynamic-node-data-from-nets node-data nets-with-ticks)
   (dynamic-edge-data-from-nets edge-data nets-with-ticks)])
  
