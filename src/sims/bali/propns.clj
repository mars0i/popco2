;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.propns
  (:use popco.core.lot)
  (:require [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]))

;;; Propositions for bali analogies based on Stephen Lansing's work,
;;; especially _Perfect Order_, along with works by other authors.  See
;;; doc/sims/bali for documentation, including notes on motivation for 
;;; this representation in initialnts*.md.

(defpred Causal-if)
(defpred Ceases)
(defpred Fails)
(defpred Is-bhutakala)
(defpred Is-disordered)
(defpred Is-king)
(defpred Is-negara)
(defpred Is-ordered)
(defpred Is-peasant)
(defpred Is-sacred)
(defpred Is-subak)
(defpred Member-of)
(defpred Nourishes)
(defpred Persists)
(defpred Preventative-if)  ; not currently used
(defpred Shares)
(defpred Struggles)
(defpred Struggles-together)
(defpred Succeeds)

(defobj king)
(defobj state)
(defobj demon)
(defobj water)
(defobj enemy)
(defobj peasant1)
(defobj peasant2)
(defobj subak)
(defobj rat)

(def conceptual-relats 
  [[-1.0 :Causal-if :Preventative-if]
   [-1.0 :Is-ordered :Is-disordered]
   [-1.0 :Is-king :Is-peasant]
   [-0.9 :Subject-of :Member-of] ; probably not needed
   [-1.0 :Persists :Ceases]
   [-1.0 :Succeeds :Fails]])


;; PROPOSITION NAMING CONVENTIONS:
;;    Brahmanic propns start with "B".
;;    Peasant/subak propns start with "P".
;;    Spiritual propns start with "BS" or "PS".
;;    Worldly propns start with "BW" or "PW".
;;    If a propn is shared between Brahmanic and peasant domains,
;;     it starts with "XS" or "XW".


;;; Brahmanic

;;;; Objects

Is-king [king] B-king
Is-negara [state] B-state

;;;;; spiritual

Is-bhutakala [demon] XS-demon-bhutakala  ; shared with subak domain
Is-sacred [water] BS-water-sacred

;;;;; worldly

Is-bhutakala [enemy] XW-enemy-bhutakala

;;;; Relations

;;;;; both

Is-ordered [state] B-state-ordered
Is-disordered [state] B-state-disordered

Persists [state] B-state-persists
Causal-if [B-state-ordered B-state-persists] B-state-ordered->persists

Ceases [state] B-state-ceases
Causal-if [B-state-disordered B-state-ceases] B-state-disordered->ceases

;;;;; spiritual

Nourishes [water state] BS-water-nourishes-state  ;; "nourishes"??  "state"??
Is-ordered [B-water-nourishes-state] BS-water-state-ordered

;; STRUGGLE
Struggles [king demon] BS-king-against-demon

Succeeds [BS-king-against-demon] BS-king-succeeds-against-demon
Causal-if [BS-king-succeeds-against-demon B-state-ordered] BS-state-succeed-demon->order

Fails [BS-king-against-demon] BS-fails-against-demon
Causal-if [BS-king-fails-against-demon B-state-disordered] BS-king-fail-demon->disorder

;;;;; worldly

;; STRUGGLE
Struggles [king enemy] BW-king-against-enemy

Succeeds [BW-king-against-enemy] BW-king-succeeds-against-enemy
Causal-if [BW-king-succeeds-against-enemy B-state-ordered] BW-succeed-enemy-order

Fails [BW-king-against-enemy] BW-king-fails-against-enemy
Causal-if [BW-king-fails-against-enemy B-state-disordered] BW-state-fail-enemy->disorder


;;; Subak

;;;; Objects

;;;;; both

Is-peasant [peasant1] P-peasant1
Is-peasant [peasant2] P-peasant2
Is-subak [subak] P-subak

;;;;; spiritual

Is-bhutakala [demon]  ; shared with Brahmanic domain
Is-sacred [water] S-water-sacred

;;;;; worldly

Is-bhutakala [rat]

;;;; Relations

;;;;; both

Member-of [peasant1 subak]
Member-of [peasant2 subak]

Is-ordered [subak]   P-subak-ordered
Is-disordered [subak] P-subak-disordered

Persists [subak] S-subak-persists
Causal-if [S-subak-ordered S-subak-persists]

Ceases [subak] S-subak-ceases  ; should always receive negative activation
Causal-if [S-subak-disordered S-subak-persists] ; NOTE this differs from Brahmanic

Shares [subak water] S-subak-shares-water  ; Should really have multiple subaks as args; this is a simplification.

;;;;; spiritual

Nourishes [water peasant] S-water-nourishes-peasant  ;; "nourishes"??
Is-ordered [S-water-nourishes-peasant]

;; STRUGGLE
Struggles-together [peasant1 peasant2 demon]   P-peasants-against-demon
Struggles [subak demon]   P-subak-against-demon
Causal-if [P-peasants-against-demon P-subak-against-demon] P-peasants->subak-against-demon

Succeeds [P-subak-against-demon] P-subak-succeeds-against-demon
Causal-if [P-succeeds-against-demon P-subak-ordered] P-subak-succeed-demon->order

Fails [P-subak-against-demon] P-subak-fails-against-demon
Causal-if [P-subak-fails-against-demon P-subak-disordered] P-subak-fail-demon->disorder

;;;;; worldly

Nourishes [water rice] S-water-nourishes-rice
Is-ordered [S-water-nourishes-rice]

;; STRUGGLE
Struggles-together [peasant1 peasant2 rat]     P-peasants-against-rat
Struggles [subak rat]     P-subak-against-rat
Causal-if [P-peasants-against-rat   P-subak-against-rat]   P-peasants->subak-against-rat

Succeeds [P-subak-against-rat]   P-subak-succeeds-against-rat
Causal-if [P-subak-succeeds-against-rat P-subak-disordered] P-subak-succeed-rat->disorder

Fails [P-subak-against-rat]   P-subak-fails-against-rat
Causal-if [P-subak-fails-against-rat P-subak-disordered] P-subak-fail-rat->disorder


