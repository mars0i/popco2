(ns popco)

(defrecord Propn [pred args id])

;; Add doc-string for class to the Clojure constructor function:
(alter-meta! #'->Propn update-in [:doc] str 
  "\n  pred: Predicate - should be keyword with initial uppercase.
  args: Array of argments, which could either be objects (keyword starting with 'ob-')
        or names of other propositions.
  id:   Name for proposition, which should start with all upper domain id, then dash
        (e.g. :CV-blah-blah).")
