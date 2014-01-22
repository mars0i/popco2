(ns popco.core.person
  (:require [utils.general :as ug]
            [clojure.core.matrix :as mx]))

;; Definition of person and related functions

(defrecord Person [nm 
                   propn-net propn-mask propn-activns 
                   analogy-net analogy-mask analogy-activns])
(ug/add-to-docstr ->Person
   "Makes a POPCO Person, with these fields:
   nm -              name of person (TODO: What type?: keyword? symbol? string?)
   propn-net -       PropnNet for this person
   propn-mask -      vector of 1's (propn is entertained) and 0's (it isn't)
   propn-activns -   vector of activation values for nodes in propn net
   analogy-net -     AnalogyNet (same for all persons)
   analogy-mask -    vector of 1's (mapnode is present) or 0's (it's absent)
   analogy-activns - activation values of nodes in analogy net")

;; TODO Consider making code below more efficient if popco is extended
;; to involve regularly creating new persons in the middle of simulation runs
;; e.g. with natural selection.

(declare make-mask)

(defn make-person
  "Creates a person with name (nm), propns with propn-ids, and a pre-constructed
  propn-net and analogy-net.  Uses propns to construct propn-mask and
  analogy-mask.  Important: The propn-net passed in should be new, with a fresh
  weight matrix (:wt-mat), since each person may modify its own propn weight
  matrix.  The analogy net can be shared with every other person, however, since
  this will not be modified.  (The analogy mask might be modified.)"
  [nm propn-ids propn-net analogy-net]
  (let [num-poss-propn-nodes (first (mx/shape propn-net))
        num-poss-analogy-nodes (first (mx/shape analogy-net))]
    (->Person nm 
              propn-net
              (make-mask propn-ids (:id-to-idx propn-net)) ; propn-mask
              (mx/new-vector num-poss-propn-nodes)            ; propn-activns
              analogy-net
              ; How to make analogy mask? NOT RIGHT
              ; Answer?: Pretend that person received all of the propns as communication
              (make-mask propn-ids (:id-to-idx propn-net)) ; TODO: PLACEHOLDER
              (mx/new-vector num-poss-analogy-nodes))))            ; analogy-activns

(defn make-mask
  "ADD DOCSTRING"
  [node-ids id-to-idx]
  (let [mask (mx/new-vector (count id-to-idx))]
    (doseq [id node-ids]
      (mx/mset! mask (:id-to-idx id) 1.0))
    mask))

(defn functional-make-mask
  "ADD DOCSTRING"
  [node-ids id-to-idx]
  (let [num-nodes (count id-to-idx)
        found-nodes (map id-to-idx node-ids)]
    (assoc
      (vec (repeat num-nodes 0))
      (interleave
        found-nodes
        (repeat (count found-nodes) 1)))))
