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

;; TODO: Define collections of propositions, summary collections, id collections, etc.
;; TODO: Define collections of links to SALIENT.

(defpred Causal-if)
(defpred Ceases)
(defpred Fails)
(defpred Is-bhutakala)
(defpred Is-disordered)
(defpred Is-king)
(defpred Is-negara)
(defpred Is-ordered)
(defpred Is-peasant)
(defpred Is-rice)
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
(defobj rice)
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
;;
;; Brahmanic propns start with "B".
;; Peasant/subak propns start with "P".
;; Spiritual propns start with "BS" or "PS".
;; Worldly propns start with "BW" or "PW".
;; Propns shared between Brahmanic and peasant domains start with "XS" or "XW".



;;; SHARED BETWEEN BRAHMANIC AND PEASANT DOMAINS:

(defpropn XS-demon-bhutakala Is-bhutakala [demon])

;;; BRAHMANIC

;;;; OBJECTS

(defpropn B-king Is-king [king])
(defpropn B-state Is-negara [state])

;;;;; SPIRITUAL

(defpropn BS-water-sacred Is-sacred [water])

;;;;; WORLDLY

(defpropn XW-enemy-bhutakala Is-bhutakala [enemy])

;;;; Relations

;;;;; BOTH

(defpropn B-state-ordered Is-ordered [state])
(defpropn B-state-disordered Is-disordered [state])

(defpropn B-state-persists Persists [state])
(defpropn B-state-ordered->persists Causal-if [B-state-ordered B-state-persists])

(defpropn B-state-ceases Ceases [state])
(defpropn B-state-disordered->ceases Causal-if [B-state-disordered B-state-ceases])

;;;;; SPIRITUAL

(defpropn BS-water-nourishes-state Nourishes [water state])  ;; "nourishes"??  "state"??
(defpropn BS-water-state-ordered Is-ordered [BS-water-nourishes-state])

;; STRUGGLE
(defpropn BS-king-against-demon Struggles [king demon])

(defpropn BS-king-succeeds-against-demon Succeeds [BS-king-against-demon])
(defpropn BS-state-succeed-demon->order Causal-if [BS-king-succeeds-against-demon B-state-ordered])

(defpropn BS-king-fails-against-demon Fails [BS-king-against-demon])
(defpropn BS-king-fail-demon->disorder Causal-if [BS-king-fails-against-demon B-state-disordered])

;;;;; WORLDLY

;; STRUGGLE
(defpropn BW-king-against-enemy Struggles [king enemy])

(defpropn BW-king-succeeds-against-enemy Succeeds [BW-king-against-enemy])
(defpropn BW-succeed-enemy-order Causal-if [BW-king-succeeds-against-enemy B-state-ordered])

(defpropn BW-king-fails-against-enemy Fails [BW-king-against-enemy])
(defpropn BW-state-fail-enemy->disorder Causal-if [BW-king-fails-against-enemy B-state-disordered])


;;; SUBAK

;;;; OBJECTS

;;;;; BOTH

(defpropn P-peasant1 Is-peasant [peasant1])
(defpropn P-peasant2 Is-peasant [peasant2])
(defpropn P-subak Is-subak [subak])
(defpropn PW-rice Is-rice [rice])

;;;;; SPIRITUAL

(defpropn PS-water-sacred Is-sacred [water])

;;;;; WORLDLY

(defpropn PW-rat-bhutakala Is-bhutakala [rat])

;;;; RELATIONS

;;;;; BOTH

(defpropn P-peasant1-in-subak Member-of [peasant1 subak])
(defpropn P-peasant2-in-subak Member-of [peasant2 subak])

(defpropn P-subak-ordered Is-ordered [subak])
(defpropn P-subak-disordered Is-disordered [subak])

(defpropn P-subak-persists Persists [subak])
(defpropn P-subak-ordered->persists Causal-if [P-subak-ordered P-subak-persists])

(defpropn P-subak-ceases Ceases [subak])  ; should always receive negative activation
(defpropn P-subak-disordered->persists Causal-if [P-subak-disordered P-subak-persists]) ; NOTE this differs from Brahmanic

(defpropn P-subak-shares-water Shares [subak water])  ; Should really have multiple subaks as args; this is a simplification.

;;;;; SPIRITUAL

(defpropn P-water-nourishes-peasant1 Nourishes [water peasant1])  ;; "nourishes"??
(defpropn P-water-nourishes-peasant2 Nourishes [water peasant2])
(defpropn P-water-peasant1-ordered Is-ordered [P-water-nourishes-peasant1])
(defpropn P-water-peasant2-ordered Is-ordered [P-water-nourishes-peasant2])

;; STRUGGLE
(defpropn PS-peasants-against-demon Struggles-together [peasant1 peasant2 demon])
(defpropn PS-subak-against-demon Struggles [subak demon])
(defpropn PS-peasants->subak-against-demon Causal-if [PS-peasants-against-demon PS-subak-against-demon])

(defpropn PS-subak-succeeds-against-demon Succeeds [PS-subak-against-demon])
(defpropn PS-subak-succeed-demon->order Causal-if [PS-subak-succeeds-against-demon P-subak-ordered])

(defpropn PS-subak-fails-against-demon Fails [PS-subak-against-demon])
(defpropn PS-subak-fail-demon->disorder Causal-if [PS-subak-fails-against-demon P-subak-disordered])

;;;;; WORLDLY

(defpropn PW-water-nourishes-rice Nourishes [water rice])
(defpropn PW-water-rice-ordered Is-ordered [PW-water-nourishes-rice])

;; STRUGGLE
(defpropn PW-peasants-against-rat Struggles-together [peasant1 peasant2 rat])
(defpropn PW-subak-against-rat Struggles [subak rat])
(defpropn PW-peasants->subak-against-rat Causal-if [PW-peasants-against-rat PW-subak-against-rat])

(defpropn PW-subak-succeeds-against-rat Succeeds [PW-subak-against-rat])
(defpropn PW-subak-succeed-rat->disorder Causal-if [PW-subak-succeeds-against-rat P-subak-disordered])

(defpropn PW-subak-fails-against-rat Fails [PW-subak-against-rat])
(defpropn PW-subak-fail-rat->disorder Causal-if [PW-subak-fails-against-rat P-subak-disordered])
