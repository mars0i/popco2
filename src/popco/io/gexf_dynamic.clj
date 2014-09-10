(ns popco.io.gexf-dynamic
  (:require [clojure.data.xml :as x]
            [clojure.core.matrix :as mx]
            [popco.core.population :as popn]
            [popco.nn.nets :as nn]
            [popco.nn.matrix :as px]
            [utils.general :as ug]))

;; gut rehab.  see gexf_dynamic_old.clj, gexf_static.clj for other code

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

(def node-size 25)  ; GEXF size
(def edge-weight 10) ; i.e. GEXF weight property, = thickness/weight for e.g. Gephi

;; Generate unique GEXF id numbers for nodes and edges.  More convenient than label-based ids for incorporating multiple persons into one graph.
(def node-id-num (atom 0))
(def edge-id-num (atom 0))
(def popco-to-gexf-node-id (atom {})) ; store relationship between popco ids and gexf node ids so I can look them up to provide source/target ids for edges

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS TO ADD TICKS TO NEURAL NET STRUCTURES

(defn net-with-tick
  [person-id net-key popn]
  (assoc (net-key (popn/get-person person-id popn)) :tick (:tick popn)))

(defn nets-with-ticks
  [person-id net-key popns]
  (map (partial net-with-tick person-id net-key) popns))

(defn analogy-net-with-tick
  [person-id popn]
  (net-with-tick person-id :analogy-net popn))

(defn analogy-nets-with-ticks
  [person-id popns]
  (nets-with-ticks person-id :analogy-net popns))

(defn propn-net-with-tick
  [person-id popn]
  (net-with-tick person-id :propn-net popn))

(defn propn-nets-with-ticks
  [person-id popns]
  (nets-with-ticks person-id :propn-net popns))

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

(defn node-data-from-net
  [node-data net-with-tick]
  (reduce add-data node-data
          (map (partial idx-to-node-data net-with-tick)
               (unmasked-nonzero-idxs net-with-tick))))

(defn net-node-data-from-nets
  [node-data nets-with-ticks]
  (reduce node-data-from-net node-data nets-with-ticks))

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

(defn edge-data-from-net
  [edge-data net-with-tick]
  (reduce add-data edge-data
          (map (partial idxs-to-edge-data net-with-tick)
               (unmasked-non-zero-links net-with-tick))))

(defn net-edge-data-from-nets
  [edge-data nets-with-ticks]
  (reduce edge-data-from-net edge-data nets-with-ticks))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS TO GENERATE clojure.data.xml SPECS FROM NODE, EDGE DATA COLLECTIONS

(defn node-entry-to-node
  "Expects one hashmap entry from node-data."
  [[id tick-data-entries] & size-s] ; size-s is a hack so that you can special-case size for some nodes

  (swap! popco-to-gexf-node-id ug/assoc-if-new id (swap! node-id-num inc)) ; update popco-id to id-num hashmap

  (let [first-tick (ffirst tick-data-entries) ; if tick, i.e. first element of first entry is nil, assume that they're all nil.
        size (or (first size-s) node-size)
        node-spec {:id (str @node-id-num) :label (name id)} ; i.e. gexf id is a number. label stores the popco id.
        make-tick-data-attr (fn [[tick activn]]
                              (let [activn-spec {:for "popco-activn" :value (str activn)}]
                                [:attvalue (if tick
                                             (merge activn-spec {:start (str (float tick)) :endopen (str (inc (float tick)))})
                                             activn-spec)])) ]
    [:node  (if first-tick
              (merge node-spec {:start (str (float first-tick))})
              node-spec)
     (into [:attvalues {}] (map make-tick-data-attr tick-data-entries))
     [:viz:position {:x (str (- (rand 100) 50)) :y (str (- (rand 100) 50)) :z "0.0"}] ; doesn't matter for Gephi, but can be useful for other programs to provide a starting position
     [:viz:size {:value (str size)}]]))


(defn edge-entry-to-edge
  [[id-set tick-data-entries] & size-s]
  (let [first-tick (ffirst tick-data-entries) ; if tick, i.e. first element of first entry is nil, assume that they're all nil.
        [node1-id node2-id] (vec id-set)
        size (or (first size-s) node-size)
        edge-spec {:id (str (swap! edge-id-num inc))
                   :source (str (get @popco-to-gexf-node-id node1-id))
                   :target (str (get @popco-to-gexf-node-id node2-id))
                   :label (str (name node1-id) "<->" (name node2-id))}
        make-tick-data-attr (fn [[tick wt]]
                              (let [wt-spec {:for "popco-wt" :value (str wt)}]
                                [:attvalue (if tick
                                             (merge wt-spec {:start (str (float tick)) :endopen (str (inc (float tick)))})
                                             wt-spec)])) ]
    [:edge  (if first-tick
              (merge edge-spec {:start (str (float first-tick))})
              edge-spec)
     (into [:attvalues {}] (map make-tick-data-attr tick-data-entries))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TOP-LEVEL FUNCTIONS

;(defn net-data
;  "node-data and edge-data are hashes for data on subsequent states
;  of nodes and edges, respectively.  nets-with-ticks contains one
;  or more nets, each with an added tick (e.g. by net-with-tick).
;   usage: (net-data {} {} (analogy-net-with-tick :worf popn))"
;  [node-data edge-data nets-with-ticks]
;  [(net-node-data-from-nets node-data nets-with-ticks)
;   (net-edge-data-from-nets edge-data nets-with-ticks)])
;  
;(defn nodes-edges-from-data
;  [node-data edge-data]
;  (reset! node-id-num 0)
;  (reset! edge-id-num 0)
;  (reset! popco-to-gexf-node-id {})
;  ;; FIXME
;  )

(defn gexf-graph-from-data
  "Generate a GEXF specification suitable for reading by clojure.data.xml
  functions such as `emit` and `indent-str`.  nodes is a sequence (not vector)
  of clojure.data.xml specifications for GEXF nodes, which can be generated by
  popco.io.gexf/node.  edges is the same sort of thing for edge specifications,
  which can be generated by popco.io.gexf/edges.  mode, if present, should be
  one of the keywords :static (default) or :dynamic, which determine the GEXF 
  graph mode.  Dynamic graphs allow time indexing.  first-tick is ignored
  if mode is :static."
  [nodes edges mode first-tick]
  (let [mode-str (name mode)]
    (x/sexp-as-element [:gexf {:xmlns "http://www.gexf.net/1.2draft"
                               :xmlns:viz "http://www.gexf.net/1.1draft/viz"
                               :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"
                               :xsi:schemaLocation "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd"
                               :version "1.2"}
                        [:graph 
                         (cond (= mode :static) {:defaultedgetype "undirected" :mode "static"} 
                               (= mode :dynamic) {:defaultedgetype "undirected" :mode "dynamic" :timeformat "double" :start (str first-tick)}  ; TODO Is that the correct timeformat??
                               :else (throw (Exception. (str "Bad GEXF graph mode: " mode))))
                         [:attributes {:class "node" :mode mode-str}
                          [:attribute {:id "popco-activn" :title "popco-activn" :type "float"}
                           [:default {} "0.0"]]]
                         [:attributes {:class "edge" :mode mode-str}
                          [:attribute {:id "popco-wt" :title "popco-wt" :type "float"}
                           [:default {} "0.0"]]]
                         [:nodes {:count (count nodes)} nodes]
                         [:edges {:count (count edges)} edges]
                         ]])))

(defn reset-id-nums! []
  (reset! node-id-num 0)
  (reset! edge-id-num 0)
  (reset! popco-to-gexf-node-id {}))

(defn gexf-graph
  "Return GEXF graph data structure suitable for use by clojure.data.xml's
  emit and related functions.  popns is a collection of populations: If there
  is more than one population, the GEXF graph will be dynamic; otherwise it
  will be static.  pers-ids is a collection of person ids to be selected from
  each population.  net-ids contains one or both of :propn-net, :analogy-net."
  [person-ids net-keys popns]
  (reset! node-id-num 0) ; TODO Is this necessary? Desirable?
  (reset! edge-id-num 0) ; TODO Is this necessary? Desirable?
  (reset! popco-to-gexf-node-id {}) ; This one is needed in any event.
  (let [nets (apply concat
                    (for [person-id person-ids
                          net-key net-keys]
                      (nets-with-ticks person-id net-key popns)))]
    (gexf-graph-from-data
      (map node-entry-to-node (net-node-data-from-nets {} nets)) 
      (map edge-entry-to-edge (net-edge-data-from-nets {} nets))
      (if (> (count popns) 1) :dynamic :static)
      (:tick (first popns)))))
