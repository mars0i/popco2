(ns popco.core.acme
  (:require [utils.general :as ug]))

(defrecord Propn [pred args id])
(ug/add-to-docstr ->Propn
  "\n  pred: Predicate - should be keyword with initial uppercase.
  args: Array of argments, which could either be objects (keyword 
        starting with 'ob-' [OBSOLETE?]) or names of other propositions.
  id:   Name for proposition, which should start with all uppercase
        domain id, then dash (e.g. :CV-blah-blah)." )

(defrecord Obj [id])
(ug/add-to-docstr ->Obj
  "\n  id: Name for proposition, which should start with 'ob-' [OBSOLETE?].")

(defn propns-match?
  [p1 p2]
  (let [args1 (:args p1)
        args2 (:args p2)]
  (and (= (count args1) (count args2))
       (or (
            
            )))))


(defn matched-propn-pairs
  [pset1 pset2]

  (for [p1 pset1
        p2 pset2
        :when (propns-match? p1 p2)]
    [p1 p2]))
