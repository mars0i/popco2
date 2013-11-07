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

;; Note that order within pairs matters.  It preserves the distinction
;; between the two analogue structures.
(defn match-propns
  "Returns a sequence of 2-element sequences, each containing two propositions
  that match according to propns-match?.  These are propositions that are
  isomorphic in the ACME sense and that can be used to construct map nodes."
  [pset1 pset2]
  (for [p1 pset1
        p2 pset2
        :when (propns-match? p1 p2)]
    [p1 p2]))

;; similar to deep-isomorphic-arglists in popco 1
(defn propns-match?
  [p1 p2]
  (let [args1 (:args p1)
        args2 (:args p2)]
  (and (= (count args1) (count args2))
       (every? identity (map args-match? args1 args2)))))

;; similar to isomorphic-args in popco 1
(defmulti  args-match? (fn [x y] [(class x) (class y)]) )
(defmethod args-match? [Obj Propn] [_ _] false)
(defmethod args-match? [Propn Obj] [_ _] false)
(defmethod args-match? [Obj Obj] [_ _] true)
(defmethod args-match? [Propn Propn] [p1 p2] (propns-match? p1 p2))


(declare match-args match-propn-components)

;; Note that order within pairs matters.  It preserves the distinction
;; between the two analogue structures, and allows predicates and objects
;; to have the same names in different analogue structures (sets don't allow).
(defn match-propn-components-too
  "Return sequence of matched Propns, predicates, and Objs from sequence 
  of pairs of Propns."
  [pairs]
  (mapcat match-propn-components pairs))

(defn match-propn-components
  [[p1 p2]]
  [[p1 p2]
   [(:pred p1) (:pred p2)]
   (mapcat match-args (:args p1) (:args p2))])

(defmulti  match-args (fn [x y] [(class x) (class y)]))
(defmethod match-args [Obj Obj] [o1 o2] [o1 o2])
(defmethod match-args [Propn Propn] [p1 p2] (match-propn-components p1 p2))
; not right--doesn't handle dupe propn matchs right.  That needs to be
; in weights.


(defn node-to-index-map
  "Given a sequence of node info entries (e.g. Propns, pairs of Propns or 
  Objs, etc.), returns a hashmap from node info entries to indexes."
  [nodes-info]
  (zipmap nodes-info (range (count nodes-info))))

(defn index-to-node-vec
  "Given a sequence of node info entries (e.g. Propns, pairs of Propns or 
  Objs, etc.), returns a vector allowing indexing node info entries."
  [nodes-info]
  (vec nodes-info))





;; Handy for displaying output of match-propns:
(defn pair-ids
  "Return sequence of pairs of :id fields of objects from sequence prs of pairs."
  [prs]
  (sort  ; does the right thing with pairs of keywords
    (map (fn [[p1 p2]] 
           [(:id p1) (:id p2)])
         prs)))
