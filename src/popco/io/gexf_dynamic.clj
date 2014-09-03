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

;; pseudocode:
;; iterate through nets
;; in each net, run through all nodes
;; if node is new, add entry to node-data
;;   with key propn-id (or also person)
;;   and val ([tick activn]).
;; if node is old, conj [tick activn] onto existing seq of [tick activns].

(defn key-to-node-data
  "Given an nnstru and a seq containing a single index, returns a pair
  containing the node id corresponding to that index in that nnstru,
  and the activation value of that node."
  [nnstru k]
  (let [[idx] k
        id (:id ((:node-vec nnstru) idx))
        tick (:tick nnstru)
        activn (mx/mget (:activns nnstru) idx)]
    [id [tick activn]]))

(defn add-node-data
  [node-data id-data-pair]
  (let [[id data] id-data-pair]
    (update-in node-data [id] (fnil conj []) data))) ; i.e. if the key id exists in node-data, conj data onto existing value; otherwise conj it onto an empty vec

(defn dynamic-node-data-from-net
  [node-data net-with-tick]
  (reduce add-node-data node-data
          (map (partial key-to-node-data net-with-tick)
               (px/non-zero-indices (:mask net-with-tick)))))

(defn dynamic-node-data-from-nets
  [node-data nets-with-ticks]
  (reduce dynamic-node-data-from-net node-data nets-with-ticks))

(defn dynamic-edge-data-from-nets
  [edge-data nets-with-ticks]
  edge-data) ; FIXME

(defn dynamic-net-data
  "node-data and edge-data are hashes for data on subsequent states
  of nodes and edges, respectively.  nets-with-ticks contains one
  or more nnstrus, each with an added tick (e.g. by net-with-tick).
  Example usage: (dynamic-net-data {} {} (analogy-net-with-tick :worf popn))"
  [node-data edge-data & nets-with-ticks]
  [(dynamic-node-data-from-nets node-data nets-with-ticks)
   (dynamic-edge-data-from-nets edge-data nets-with-ticks)])
  
