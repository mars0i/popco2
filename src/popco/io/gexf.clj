(ns popco.io.gexf
  (:require [clojure.data.xml :as x]
            [clojure.core.matrix :as mx]
            [popco.nn.matrix :as px]))

(def as-elem x/sexp-as-element) ; convenience abbreviation

; The xml declaration will be generated by emit and its cousins. i.e. <?xml version=\"1.0\" encoding=\"UTF-8\"?>")

(def node-size-multiplier 50)

(defn gexf-graph
  [nodes edges]
  (as-elem [:gexf {:xmlns "http://www.gexf.net/1.2draft"
                   :xmlns:viz "http://www.gexf.net/1.1draft/viz"
                   :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"
                   :xsi:schemaLocation "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd"
                   :version "1.2"}
            [:graph {:defaultedgetype "undirected" :mode "static"} 
	            [:attributes {:class "node"}
		                 [:attribute {:id "activn" :title "activation" :type "float"}
				             [:default {} "0.0"]]]
	            [:attributes {:class "edge"}
		                 [:attribute {:id "popco-wt" :title "popco weight" :type "float"}
				             [:default {} "0.0"]]]
                    [:nodes {:count (count nodes)} nodes]
                    [:edges {:count (count edges)} edges]]]))

(defn node
 "id should be a string. It will also be used as label. 
 activn is a POPCO activation value."
 [id activn]
 (let [color (cond (pos? activn) {:r "255" :g "255" :b "0"} ; yellow
	      (neg? activn) {:r "0" :g "0" :b "255"}
	      :else {:r "128" :g "128" :b "128"})]
  [:node {:id id :label id} 
         [:attvalues {} [:attvalue {:for "activn" :value (str activn)}]]
	 [:viz:color color]
	 [:viz:size {:value (str (* node-size-multiplier (mx/abs activn)))}] ] ))

(defn popco-to-gexf-wt
  "Translate a popco link weight into a string suitable for use as an edge
  weight in a GEXF specification for Gephi, by taking the absolute value and
  possibly making that absolute value larger or smaller."
  [popco-wt]
  (str (+ 5 (* 10 (mx/abs popco-wt)))))

(defn edge
  "node1-id and node2-id are strings that correspond to id's passed to the
  function node.  popco-wt should be a POPCO link weight.  It will determine
  edge thickness via the GEXF weight attribute via function popco-to-gexf-wt,
  but will also be stored as the value of attribute popco-wt."
  [node1-id node2-id popco-wt]
  (let [gexf-wt (popco-to-gexf-wt popco-wt)
        color (cond (pos? popco-wt) {:r "0" :g "255" :b "0"}
                    (neg? popco-wt) {:r "255" :g "0" :b "0"}
                    :else {:r "0" :g "0" :b "0"})]
    [:edge {:id (str node1-id "::" node2-id)
            :source node1-id
            :target node2-id
            :weight gexf-wt}
     [:attvalues {} [:attvalue {:for "popco-wt" :value (str popco-wt)}]]
     [:viz:thickness {:value gexf-wt}] ; IGNORED, APPARENTLY
     [:viz:color color]]))

(defn nn-to-nodes
  "Given an PropnNet or AnalogyNet, return a seq of node specifications,
  one for each unmasked node, to pass to gexf-graph."
  [nnstru]
  (let [activns (:activns nnstru)
        node-vec (:node-vec nnstru)
        key-to-node (fn [k]
                      (let [[idx] k]                      ; keys from non-zeros are vectors of length 1
                        (node (name (:id (node-vec idx))) ; node-vec is a Clojure vector of Propns
                              (mx/mget activns idx))))]   ; activns is a core.matrix vector of numbers
    (map key-to-node 
         (px/non-zero-indices (:mask nnstru)))))

(defn nn-to-edges
  "Given an PropnNet or AnalogyNet, return a seq of edge specifications,
  one for each edge between unmasked nodes, to pass to gexf-graph.  Doesn't
  distinguish between one-way and two-way links, and assumes that the only
  one-way links are from the feeder node."
  [nnstru]
  (let [wt-mat (wt-mat nnstru)
        node-vec (:node-vec nnstru)
        key-to-edge (fn [k]
                      (let [[idx1 idx2] k]
                        (edge (name (:id (node-vec idx1))) ; node-vec is a Clojure vector of Propns
                              (name (:id (node-vec idx2))) ; node-vec is a Clojure vector of Propns
                              (mx/mget wt-mat idx1 idx2))))]   ; activns is a core.matrix vector of numbers
    (map key-to-edge 
         (filter #(>= idx1 idx2)  ; Get only lower triangle including diagonal, containing feeder weights plus weights duplicated in upper triangle.
                 (px/non-zero-indices (:mask nnstru))))))  ; TODO NEED TO MAKE THIS A MATRIX OF UNMASKED ELEMENTS SO TO SPEAK

(defn nn-to-graph
  [nnstru]
  (gexf-graph (nn-to-nodes nnstru)
              (nn-to-edges nnstru)))

;; IMPORTANT: During import into Gephi, uncheck "auto-scale".  Otherwise it does funny things with node sizes.
(defn gexf-test []
 (gexf-graph
  (list             ; can't be a vector--vectors aren't seqs
   (node "A" 1.0)
   (node "B" 0.25)
   (node "C" -0.5))
  (list
   (edge "A" "B" -2.0)
   (edge "A" "C" 1.0)
   (edge "C" "B" 4.0))))


;(defn nodes
;  [& body]
;  [:nodes {} body])
;
;(defn edges
;  [& body]
;  [:edges {} body])
