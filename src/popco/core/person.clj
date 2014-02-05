(ns popco.core.person
  (:require [utils.general :as ug]
            [popco.core.communic :as cc]
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
  [nm propns propn-net analogy-net]
  (let [num-poss-propn-nodes (count (:node-vec propn-net))
        num-poss-analogy-nodes (count (:node-vec analogy-net))
        propn-ids (map :id propns)
        pers (->Person nm 
                       propn-net
                       (mx/zero-vector num-poss-propn-nodes)     ; propn-mask
                       (mx/zero-vector num-poss-propn-nodes)     ; propn-activns
                       analogy-net
                       (mx/zero-vector num-poss-analogy-nodes)   ; analogy-mask
                       (mx/zero-vector num-poss-analogy-nodes))] ; analogy-activns
    (doseq [propn propns] (cc/add-to-propn-net pers (:id propn)))   ; better to fill propn mask before
    (doseq [propn propns] (cc/add-to-analogy-net pers (:id propn))) ;  analogy mask, so propns are known
    pers))
