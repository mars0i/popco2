(ns popco.core.acme
  (:require [utils.general :as ug]))

(defrecord Propn [pred args id])
(ug/add-to-docstr ->Propn
  "\n  pred: Predicate - should be keyword with initial uppercase.
  args: Array of argments, which could either be objects (keyword 
        starting with 'ob-' [OBSOLETE?]) or names of other propositions.
  id:   Name for proposition, which should start with all uppercase
        domain id, then dash (e.g. :CV-blah-blah)." )

(defmacro defpropn
  "Creates a Propn with pred as :pred, args as :args, and (keyword nm)--i.e.
  : + value of nm--as :id.  Then defines nm to have this Propn as its value,
  and returns the Propn."
  [nm pred args]
  `(do 
     (def ~nm (->Propn ~pred ~args (keyword '~nm)))
     ~nm))

(defrecord Obj [id])
(ug/add-to-docstr ->Obj
  "\n  id: Name for proposition, which should start with 'ob-' [OBSOLETE?].")

(defmacro defobj
  "Creates an Obj with (keyword nm)--i.e. : + value of nm--as :id.  Then 
  defines nm to have this Obj as its value, and returns the Obj."
  [nm]
  `(do 
     (def ~nm (->Obj (keyword '~nm)))
     ~nm))

(declare propns-match? args-match?)

(defn propns-match?
  [p1 p2]
  (let [args1 (:args p1)
        args2 (:args p2)]
  (and (= (count args1) (count args2))
       (every? identity (map args-match? args1 args2)))))

;; write as single conditional?
(defmulti args-match? (fn [x y] [(class x) (class y)]) )
(defmethod args-match? [Obj Propn] [_ _] false)
(defmethod args-match? [Propn Obj] [_ _] false)
(defmethod args-match? [Obj Obj] [_ _] true)
(defmethod args-match? [Propn Propn] [p1 p2]
  (every? identity (map propns-match? (:args p1) (:args p2))))

(defn matched-propn-pairs
  [pset1 pset2]
  (for [p1 pset1
        p2 pset2
        :when (propns-match? p1 p2)]
    [p1 p2]))

;; Handy for displaying output of matched-propn-pairs:
(defn pair-ids
  "Return sequence of pairs of :id fields of objects from sequence prs of pairs."
  [prs]
  (map 
    (fn [[p1 p2]] 
      [(:id p1) (:id p2)])
    prs))

