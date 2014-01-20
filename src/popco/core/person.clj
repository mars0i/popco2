(ns popco.core.person
  (:require [utils.general :as ug]))

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

(defn make-person
  "Creates a person with name (nm), propns with propn-ids, and a pre-constructed
  propn-net and analogy-net.  Uses propns to construct propn-mask and
  analogy-mask.  Important: The propn-net passed in should be new, with a fresh
  weight matrix (:wt-mat), since each person may modify its own propn weight
  matrix.  The analogy net can be shared with every other person, however, since
  this will not be modified.  (The analogy mask might be modified.)"
  [nm propn-ids propn-net analogy-net]
  (let [propn-mask nil])
  ;; TODO
  )

;; TODO
(defn make-mask
  "ADD DOCSTRING"
  [node-ids id-to-idx]
  (letfn [(id-to-boolint [id] (if (id-to-idx id) 1 0))]
    (let [all-idxs (range (count id-to-idx))]
        
(vec (map #(
            ) all-idxs))

        )))

