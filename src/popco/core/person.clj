(ns popco.core.person
  (:require [utils.general :as ug]))

;; Definition of person and related functions

(defrecord Person [propn-mask propn-activns propn-net analogy-mask analogy-activns analogy-net])
(ug/add-to-docstr ->Person
   "Makes a POPCO Person, with these fields:
   propn-mask -      vector of 1's (propn is entertained) and 0's (it isn't)
   propn-activns -   vector of activation values for nodes in propn net
   propn-net -       PropnNet for this person
   analogy-mask -    vector of 1's (mapnode is present) or 0's (it's absent)
   analogy-activns - activation values of nodes in analogy net
   analogy-net -     AnalogyNet (same for all persons)")

