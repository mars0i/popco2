(ns popco)

(defrecord Propn [pred args id])
;; Add doc-string for class to the constructor function:
(alter-meta! #'->Propn update-in [:doc] str 
  "\n  pred: Predicate - should be keyword with initial uppercase.
  args: Array of argments, which could either be objects (keyword 
        starting with 'ob-') or names of other propositions.
  id:   Name for proposition, which should start with all uppercase
        domain id, then dash (e.g. :CV-blah-blah)." )

(defn match-propn
  [p1 p2]
  (println "match-propn is broken!")
  [p1 p2]) 

(defn match-propns 
  [pset1 pset2]

  (for [p1 pset1
        p2 pset2
        :when (match-propn p1 p2)]
    [p1 p2]))
