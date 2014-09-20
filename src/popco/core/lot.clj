;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.lot
  (:require [utils.general :as ug]))

;;; LOT, language of thought: records/classes that represent what
;;; is believed, entertained, or communicated between persons.

;; CONVENTIONS:
;; The value of all fields are keywords.
;; Propn id's start with initial all-upper analog domain identifier,
;; then dash, then rest of propn id.
;; Predicate id's start with initial upper, then the rest lowercase.
;; Obj ids start with "ob-".
;; Disobeying these conventions should not cause anything to break,
;; but adhering to them makes it easier to understand what's happening
;; in the program, especially when displaying id's only.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Propn's represent what's believed, entertained, or communicated.

(defrecord Propn [pred args id])
(ug/add-to-docstr ->Propn
  "\n  pred: Predicate - should be keyword with initial uppercase.
  args: Array of argments, which could either be objects (keyword 
        starting with 'ob-' [OBSOLETE?]) or names of other propositions.
  id:   Name for proposition, which should start with all uppercase
        analog domain id, then dash (e.g. :CV-blah-blah)." )

(defmacro defpropn
  "Creates a Propn with pred as :pred, args as :args, and (keyword nm)--i.e.
  : + value of nm--as :id.  Then defines nm to have this Propn as its value,
  and returns the Propn."
  [nm pred args]
  `(do 
     (def ~nm (->Propn ~pred ~args (keyword '~nm)))
     ~nm))

(defn propn?
  [x]
  (= (class x) popco.core.lot.Propn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pred's represent the main property or predicate of a Propn

(defrecord Pred [id])
(ug/add-to-docstr ->Pred
  "\n  id: Name for proposition, which have initial cap and rest lowercase.")

(defmacro defpred
  "Creates a Pred with (keyword nm)--i.e. : + value of nm--as :id.  Then 
  defines nm to have this Pred as its value, and returns the Pred."
  [nm]
  `(do 
     (def ~nm (->Pred (keyword '~nm)))
     ~nm))

(defn pred?
  [x]
  (= (class x) popco.core.lot.Pred))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Obj's represent arguments which may satisfy a pred

(defrecord Obj [id])
(ug/add-to-docstr ->Obj
  "\n  id: Name for proposition, which should start with 'ob-'.")

(defmacro defobj
  "Creates an Obj with (keyword nm)--i.e. : + value of nm--as :id.  Then 
  defines nm to have this Obj as its value, and returns the Obj."
  [nm]
  `(do 
     (def ~nm (->Obj (keyword '~nm)))
     ~nm))

(defn obj?
  [x]
  (= (class x) popco.core.lot.Obj))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Give all of the above a common predicate:

;(derive Propn ::LOTelt)
;(derive Pred  ::LOTelt)
;(derive Obj  ::LOTelt)

(defn lot-item? [x]
  (or (instance? Propn x)
      (instance? Pred x)
      (instance? Obj x)))
