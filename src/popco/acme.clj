(ns popco)

(defrecord Propn [pred args id])
;; Add doc-string for class to the constructor function:
(alter-meta! #'->Propn update-in [:doc] str 
  "\n  pred: Predicate - should be keyword with initial uppercase.
  args: Array of argments, which could either be objects (keyword 
        starting with 'ob-') or names of other propositions.
  id:   Name for proposition, which should start with all uppercase
        domain id, then dash (e.g. :CV-blah-blah)." )

(defrecord Obj [id])

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
