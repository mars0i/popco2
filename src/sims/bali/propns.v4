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
(defpred Is-ordered)
(defpred Is-disordered)
;(defpred Is-bhutakala)
;(defpred Is-king)
;(defpred Is-negara)
;(defpred Is-peasant)
;(defpred Is-rice)
;(defpred Is-sacred)
;(defpred Is-subak)
(defpred Member-of)
(defpred Nourishes)
(defpred Persists)
;(defpred Preventative-if)  ; not currently used
(defpred Shares)
(defpred Struggles-alone)
(defpred Struggles-together)
(defpred Struggles-on-behalf)
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
   ;[-0.9 :Is-king :Is-subak]
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
  [;(defpropn WB-demon-bhutakala Is-bhutakala [demon])
   ;(defpropn WB-king Is-king [king])
   ;(defpropn WB-state Is-negara [state])
   ;(defpropn WB-enemy-bhutakala Is-bhutakala [enemy])
   (defpropn WB-water-nourishes-state Nourishes [water state])  ;; "nourishes"??  "state"?? 
   (defpropn WB-king-for-subaks Struggles-on-behalf [king subak])
   (defpropn WB-king-for-peasant1 Struggles-on-behalf [king peasant1])
   (defpropn WB-king-for-peasant2 Struggles-on-behalf [king peasant2])
   (defpropn WB-king-against-enemy Struggles-alone [king enemy])
   (defpropn WB-king-succeeds-against-enemy Succeeds [WB-king-against-enemy])
   (defpropn WB-state-ordered Is-ordered [state])
   (defpropn WB-state-disordered Is-disordered [state])
   (defpropn WB-succeed-enemy-order Causal-if [WB-king-succeeds-against-enemy WB-state-ordered])
   (defpropn WB-king-fails-against-enemy Fails [WB-king-against-enemy])
   (defpropn WB-state-fail-enemy->disorder Causal-if [WB-king-fails-against-enemy WB-state-disordered])
   (defpropn WB-state-persists Persists [state])
   (defpropn WB-state-ordered->persists Causal-if [WB-state-ordered WB-state-persists])
   (defpropn WB-state-ceases Ceases [state])
   (defpropn WB-state-disordered->ceases Causal-if [WB-state-disordered WB-state-ceases])])

(def spiritual-brahmanic-propns 
  [;(defpropn SB-demon-bhutakala Is-bhutakala [demon])
   ;(defpropn SB-king Is-king [king])
   ;(defpropn SB-state Is-negara [state])
   ;(defpropn SB-water-sacred Is-sacred [water])
   (defpropn SB-water-nourishes-state Nourishes [water state])  ;; "nourishes"??  "state"?? 
   ;(defpropn SB-water-state-ordered Is-ordered [SB-water-nourishes-state])
   (defpropn SB-king-for-subaks Struggles-on-behalf [king subak])
   (defpropn SB-king-for-peasant1 Struggles-on-behalf [king peasant1])
   (defpropn SB-king-for-peasant2 Struggles-on-behalf [king peasant2])
   (defpropn SB-king-against-demon Struggles-alone [king demon])
   (defpropn SB-king-succeeds-against-demon Succeeds [SB-king-against-demon])
   (defpropn SB-state-ordered Is-ordered [state])
   (defpropn SB-state-disordered Is-disordered [state])
   (defpropn SB-state-succeed-demon->order Causal-if [SB-king-succeeds-against-demon SB-state-ordered])
   (defpropn SB-king-fails-against-demon Fails [SB-king-against-demon])
   (defpropn SB-king-fail-demon->disorder Causal-if [SB-king-fails-against-demon SB-state-disordered])
   (defpropn SB-state-persists Persists [state])
   (defpropn SB-state-ordered->persists Causal-if [SB-state-ordered SB-state-persists])
   (defpropn SB-state-ceases Ceases [state])
   (defpropn SB-state-disordered->ceases Causal-if [SB-state-disordered SB-state-ceases])])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Subak

(def worldly-peasant-propns
  [;(defpropn WP-demon-bhutakala Is-bhutakala [demon])
   ;(defpropn WP-peasant1 Is-peasant [peasant1])
   ;(defpropn WP-peasant2 Is-peasant [peasant2])
   ;(defpropn WP-subak Is-subak [subak])
   (defpropn WP-peasant1-in-subak Member-of [peasant1 subak])
   (defpropn WP-peasant2-in-subak Member-of [peasant2 subak])
   (defpropn WP-subak-ordered Is-ordered [subak])
   (defpropn WP-subak-disordered Is-disordered [subak])
   (defpropn WP-subak-persists Persists [subak])
   (defpropn WP-subak-ordered->persists Causal-if [WP-subak-ordered WP-subak-persists])
   (defpropn WP-subak-ceases Ceases [subak])  ; should always receive negative activation
   (defpropn WP-subak-disordered->persists Causal-if [WP-subak-disordered WP-subak-persists]) ; NOTE this differs from Brahmanic
   (defpropn WP-subak-shares-water Shares [subak water]) ; Should really have multiple subaks as args; this is a simplification.
   ;(defpropn WP-water-nourishes-peasant1 Nourishes [water peasant1])  ;; "nourishes"?? ;
   ;(defpropn WP-water-nourishes-peasant2 Nourishes [water peasant2]) ;
   ;(defpropn WP-water-peasant1-ordered Is-ordered [WP-water-nourishes-peasant1])
   ;(defpropn WP-water-peasant2-ordered Is-ordered [WP-water-nourishes-peasant2])
   ;(defpropn WP-rice Is-rice [rice])
   ;(defpropn WP-rat-bhutakala Is-bhutakala [rat])
   (defpropn WP-water-nourishes-rice Nourishes [water rice]) ;
   (defpropn WP-water-rice-ordered Is-ordered [WP-water-nourishes-rice])
   (defpropn WP-peasants-against-rat Struggles-together [peasant1 peasant2 rat])
   ;(defpropn WP-subak-against-rat Struggles-alone [subak rat])
   ;(defpropn WP-peasants->subak-against-rat Causal-if [WP-peasants-against-rat WP-subak-against-rat])
   ;(defpropn WP-subak-succeeds-against-rat Succeeds [WP-subak-against-rat])
   (defpropn WP-subak-succeeds-against-rat Succeeds [WP-peasants-against-rat])
   (defpropn WP-subak-succeed-rat->disorder Causal-if [WP-subak-succeeds-against-rat WP-subak-disordered])
   (defpropn WP-subak-fails-against-rat Fails [WP-peasants-against-rat])
   (defpropn WP-subak-fail-rat->disorder Causal-if [WP-subak-fails-against-rat WP-subak-disordered])])

(def spiritual-peasant-propns
  [;(defpropn SP-demon-bhutakala Is-bhutakala [demon])
   ;(defpropn SP-peasant1 Is-peasant [peasant1])
   ;(defpropn SP-peasant2 Is-peasant [peasant2])
   ;(defpropn SP-subak Is-subak [subak])
   ;(defpropn SP-water-sacred Is-sacred [water])
   (defpropn SP-peasant1-in-subak Member-of [peasant1 subak])
   (defpropn SP-peasant2-in-subak Member-of [peasant2 subak])
   (defpropn SP-subak-ordered Is-ordered [subak])
   (defpropn SP-subak-disordered Is-disordered [subak])
   (defpropn SP-subak-persists Persists [subak])
   (defpropn SP-subak-ordered->persists Causal-if [SP-subak-ordered SP-subak-persists])
   (defpropn SP-subak-ceases Ceases [subak])  ; should always receive negative activation
   (defpropn SP-subak-disordered->persists Causal-if [SP-subak-disordered SP-subak-persists]) ; NOTE this differs from Brahmanic
   (defpropn SP-subak-shares-water Shares [subak water]) ; Should really have multiple subaks as args; this is a simplification.
   (defpropn SP-water-nourishes-peasant1 Nourishes [water peasant1])  ;; "nourishes"??
   (defpropn SP-water-nourishes-peasant2 Nourishes [water peasant2])
   (defpropn SP-water-peasant1-ordered Is-ordered [SP-water-nourishes-peasant1])
   (defpropn SP-water-peasant2-ordered Is-ordered [SP-water-nourishes-peasant2])
   (defpropn SP-peasants-against-demon Struggles-together [peasant1 peasant2 demon])
   ;(defpropn SP-subak-against-demon Struggles-alone [subak demon])
   ;(defpropn SP-peasants->subak-against-demon Causal-if [SP-peasants-against-demon SP-subak-against-demon])
   ;(defpropn SP-subak-succeeds-against-demon Succeeds [SP-subak-against-demon])
   (defpropn SP-subak-succeeds-against-demon Succeeds [SP-peasants-against-demon])
   (defpropn SP-subak-succeed-demon->order Causal-if [SP-subak-succeeds-against-demon SP-subak-ordered])
   (defpropn SP-subak-fails-against-demon Fails [SP-peasants-against-demon])
   (defpropn SP-subak-fail-demon->disorder Causal-if [SP-subak-fails-against-demon SP-subak-disordered])])


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
