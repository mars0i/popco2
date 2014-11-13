;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.propns
  (:use popco.core.lot)
  (:require [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]))

;;; PROPOSITIONS FOR BALI ANALOGIES BASED ON STEPHEN LANSING's publications,
;;; especially _Perfect Order_, along with works by other authors such as
;;; Clifford Geertz and Hildred Geertz.  See
;;; doc/sims/bali for documentation, including notes on motivation for 
;;; this representation in initialnts*.md.


;; PROPOSITION NAMING CONVENTIONS:
;; Brahmanic propns start with "B".
;; Peasant/subak propns start with "P".
;; Spiritual propns start with "BS" or "PS".
;; Worldly propns start with "BW" or "PW".
;; Propns shared between Brahmanic and peasant domains start with "XS" or "XW".

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
;(defpred Preventative-if)  ; not currently used
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
  [[-1.0 :Is-ordered :Is-disordered]
   ;[-1.0 :Causal-if :Preventative-if]  ; at present, not using Preventative-if, so this causes a NPE since there are no such propns
   ;[-1.0 :Is-king :Is-peasant]
   [-1.0 :Persists :Ceases]
   [-1.0 :Succeeds :Fails]])

; examples:
; (def semantic-iffs [[-0.1 :CB-vpp :V-ipa] [-0.1 :CV-rpa :B-abp]])
; (def semantic-iffs [[-0.1 :B-king :P-subak]])
(def semantic-iffs [])

;; QUESTIONS:
;; There are several completely parallel propns here--i.e. identical but for the propn name/id.
;; This is reasonable for some e.g. wrt demons, and I suppose you could say that e.g. king and state
;; have both spiritual and worldyl aspects, so that Is-king has a slightly different spiritual and
;; worldly meaning.  Maybe that's a stretch.  However, is it at all reasonable to talk of persisting
;; as having both spiritual and worldly versions??

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Brahmanic

(def worldly-brahmanic-propns 
  [(defpropn BW-demon-bhutakala Is-bhutakala [demon])
   (defpropn BW-king Is-king [king])
   (defpropn BW-state Is-negara [state])
   (defpropn BW-enemy-bhutakala Is-bhutakala [enemy])
   (defpropn BW-king-against-enemy Struggles [king enemy])
   (defpropn BW-king-succeeds-against-enemy Succeeds [BW-king-against-enemy])
   (defpropn BW-state-ordered Is-ordered [state])
   (defpropn BW-state-disordered Is-disordered [state])
   (defpropn BW-succeed-enemy-order Causal-if [BW-king-succeeds-against-enemy BW-state-ordered])
   (defpropn BW-king-fails-against-enemy Fails [BW-king-against-enemy])
   (defpropn BW-state-fail-enemy->disorder Causal-if [BW-king-fails-against-enemy BW-state-disordered])
   (defpropn BW-state-persists Persists [state])
   (defpropn BW-state-ordered->persists Causal-if [BW-state-ordered BW-state-persists])
   (defpropn BW-state-ceases Ceases [state])
   (defpropn BW-state-disordered->ceases Causal-if [BW-state-disordered BW-state-ceases])])

(def spiritual-brahmanic-propns 
  [(defpropn BS-demon-bhutakala Is-bhutakala [demon])
   (defpropn BS-king Is-king [king])
   (defpropn BS-state Is-negara [state])
   (defpropn BS-water-sacred Is-sacred [water])
   (defpropn BS-water-nourishes-state Nourishes [water state])  ;; "nourishes"??  "state"??
   (defpropn BS-water-state-ordered Is-ordered [BS-water-nourishes-state])
   (defpropn BS-king-against-demon Struggles [king demon])
   (defpropn BS-king-succeeds-against-demon Succeeds [BS-king-against-demon])
   (defpropn BS-state-ordered Is-ordered [state])
   (defpropn BS-state-disordered Is-disordered [state])
   (defpropn BS-state-succeed-demon->order Causal-if [BS-king-succeeds-against-demon BS-state-ordered])
   (defpropn BS-king-fails-against-demon Fails [BS-king-against-demon])
   (defpropn BS-king-fail-demon->disorder Causal-if [BS-king-fails-against-demon BS-state-disordered])
   (defpropn BS-state-persists Persists [state])
   (defpropn BS-state-ordered->persists Causal-if [BS-state-ordered BS-state-persists])
   (defpropn BS-state-ceases Ceases [state])
   (defpropn BS-state-disordered->ceases Causal-if [BS-state-disordered BS-state-ceases])])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Subak

(def worldly-peasant-propns
  [(defpropn PW-demon-bhutakala Is-bhutakala [demon])
   (defpropn PW-peasant1 Is-peasant [peasant1])
   (defpropn PW-peasant2 Is-peasant [peasant2])
   (defpropn PW-subak Is-subak [subak])
   (defpropn PW-peasant1-in-subak Member-of [peasant1 subak])
   (defpropn PW-peasant2-in-subak Member-of [peasant2 subak])
   (defpropn PW-subak-ordered Is-ordered [subak])
   (defpropn PW-subak-disordered Is-disordered [subak])
   (defpropn PW-subak-persists Persists [subak])
   (defpropn PW-subak-ordered->persists Causal-if [PW-subak-ordered PW-subak-persists])
   (defpropn PW-subak-ceases Ceases [subak])  ; should always receive negative activation
   (defpropn PW-subak-disordered->persists Causal-if [PW-subak-disordered PW-subak-persists]) ; NOTE this differs from Brahmanic
   (defpropn PW-subak-shares-water Shares [subak water]) ; Should really have multiple subaks as args; this is a simplification.
   (defpropn PW-water-nourishes-peasant1 Nourishes [water peasant1])  ;; "nourishes"??
   (defpropn PW-water-nourishes-peasant2 Nourishes [water peasant2])
   (defpropn PW-water-peasant1-ordered Is-ordered [PW-water-nourishes-peasant1])
   (defpropn PW-water-peasant2-ordered Is-ordered [PW-water-nourishes-peasant2])
   (defpropn PW-rice Is-rice [rice])
   (defpropn PW-rat-bhutakala Is-bhutakala [rat])
   (defpropn PW-water-nourishes-rice Nourishes [water rice])
   (defpropn PW-water-rice-ordered Is-ordered [PW-water-nourishes-rice])
   (defpropn PW-peasants-against-rat Struggles-together [peasant1 peasant2 rat])
   (defpropn PW-subak-against-rat Struggles [subak rat])
   (defpropn PW-peasants->subak-against-rat Causal-if [PW-peasants-against-rat PW-subak-against-rat])
   (defpropn PW-subak-succeeds-against-rat Succeeds [PW-subak-against-rat])
   (defpropn PW-subak-succeed-rat->disorder Causal-if [PW-subak-succeeds-against-rat PW-subak-disordered])
   (defpropn PW-subak-fails-against-rat Fails [PW-subak-against-rat])
   (defpropn PW-subak-fail-rat->disorder Causal-if [PW-subak-fails-against-rat PW-subak-disordered])])

(def spiritual-peasant-propns
  [(defpropn PS-demon-bhutakala Is-bhutakala [demon])
   (defpropn PS-peasant1 Is-peasant [peasant1])
   (defpropn PS-peasant2 Is-peasant [peasant2])
   (defpropn PS-subak Is-subak [subak])
   (defpropn PS-peasant1-in-subak Member-of [peasant1 subak])
   (defpropn PS-peasant2-in-subak Member-of [peasant2 subak])
   (defpropn PS-subak-ordered Is-ordered [subak])
   (defpropn PS-subak-disordered Is-disordered [subak])
   (defpropn PS-subak-persists Persists [subak])
   (defpropn PS-subak-ordered->persists Causal-if [PS-subak-ordered PS-subak-persists])
   (defpropn PS-subak-ceases Ceases [subak])  ; should always receive negative activation
   (defpropn PS-subak-disordered->persists Causal-if [PS-subak-disordered PS-subak-persists]) ; NOTE this differs from Brahmanic
   (defpropn PS-subak-shares-water Shares [subak water]) ; Should really have multiple subaks as args; this is a simplification.
   (defpropn PS-water-nourishes-peasant1 Nourishes [water peasant1])  ;; "nourishes"??
   (defpropn PS-water-nourishes-peasant2 Nourishes [water peasant2])
   (defpropn PS-water-peasant1-ordered Is-ordered [PS-water-nourishes-peasant1])
   (defpropn PS-water-peasant2-ordered Is-ordered [PS-water-nourishes-peasant2])
   (defpropn PS-water-sacred Is-sacred [water])
   (defpropn PS-peasants-against-demon Struggles-together [peasant1 peasant2 demon])
   (defpropn PS-subak-against-demon Struggles [subak demon])
   (defpropn PS-peasants->subak-against-demon Causal-if [PS-peasants-against-demon PS-subak-against-demon])
   (defpropn PS-subak-succeeds-against-demon Succeeds [PS-subak-against-demon])
   (defpropn PS-subak-succeed-demon->order Causal-if [PS-subak-succeeds-against-demon PS-subak-ordered])
   (defpropn PS-subak-fails-against-demon Fails [PS-subak-against-demon])
   (defpropn PS-subak-fail-demon->disorder Causal-if [PS-subak-fails-against-demon PS-subak-disordered])])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Convenient collections


;; collections of propositions:

(def spiritual-propns (seq (set (concat spiritual-brahmanic-propns spiritual-peasant-propns)))) ; seq + set to remove dupe shared propns
(def worldly-propns (seq (set (concat worldly-brahmanic-propns worldly-peasant-propns))))
(def all-propns (seq (set (concat spiritual-propns worldly-propns))))


;; collections of proposition ids:

(def spiritual-brahmanic-propn-ids (map :id spiritual-brahmanic-propns)) 
(def spiritual-peasant-propn-ids (map :id spiritual-peasant-propns))
(def spiritual-propn-ids (seq (set (concat spiritual-brahmanic-propn-ids spiritual-peasant-propn-ids)))) ; seq + set to remove dupe shared propn-ids

(def worldly-brahmanic-propn-ids (map :id worldly-brahmanic-propns)) 
(def worldly-peasant-propn-ids (map :id worldly-peasant-propns))
(def worldly-propn-ids (seq (set (concat worldly-brahmanic-propn-ids worldly-peasant-propn-ids))))

(def all-propn-ids (seq (set (concat spiritual-propn-ids worldly-propn-ids))))


;; collections of specifications that certain propns should be "perceived", i.e. have a fully positive link to SALIENT:

(def spiritual-brahmanic-perception-ifs (map #(vector 1.0 % :SALIENT) spiritual-brahmanic-propn-ids))
(def spiritual-peasant-perception-ifs (map #(vector 1.0 % :SALIENT) spiritual-peasant-propn-ids))
(def spiritual-perception-ifs (seq (set (concat spiritual-brahmanic-perception-ifs spiritual-peasant-perception-ifs))))

(def worldly-brahmanic-perception-ifs (map #(vector 1.0 % :SALIENT) worldly-brahmanic-propn-ids))
(def worldly-peasant-perception-ifs (map #(vector 1.0 % :SALIENT) worldly-peasant-propn-ids))
(def worldly-perception-ifs (seq (set (concat worldly-brahmanic-perception-ifs worldly-peasant-perception-ifs))))


;; Proposition nets (templates for individual nets--i.e. clone from these rather than using them directly):
;; second arg is bidirectional links; third is unidirectional
;; more specific versions can be made in specific model files

(def no-perc-pnet (pn/make-propn-net all-propns semantic-iffs nil)) ; nothing perceived

(def spiritual-perc-pnet (pn/make-propn-net all-propns semantic-iffs spiritual-perception-ifs))                     ; spiritual propns perceived
(def spiritual-brahmanic-perc-pnet (pn/make-propn-net all-propns semantic-iffs spiritual-brahmanic-perception-ifs)) ; spiritual brahmanic propns perceived
(def spiritual-peasant-perc-pnet (pn/make-propn-net all-propns semantic-iffs spiritual-peasant-perception-ifs))     ; spiritual peasant propns perceived

(def worldly-perc-pnet (pn/make-propn-net all-propns semantic-iffs worldly-perception-ifs))                     ; worldly propns perceived
(def worldly-brahmanic-perc-pnet (pn/make-propn-net all-propns semantic-iffs worldly-brahmanic-perception-ifs)) ; worldly brahmanic propns perceived
(def worldly-peasant-perc-pnet (pn/make-propn-net all-propns semantic-iffs worldly-peasant-perception-ifs))     ; worldly peasant propns perceived


;; Standard analogy net--can be shared by everyone:

(def anet (an/make-analogy-net spiritual-propns worldly-propns conceptual-relats))
