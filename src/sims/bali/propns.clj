;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; PROPOSITIONS FOR BALI ANALOGIES BASED ON STEPHEN LANSING's publications,
;;; especially _Perfect Order_ (e.g. p. 150), along with works by other authors
;;; such as Clifford Geertz and Hildred Geertz.  See doc/sims/bali for documentation,
;;; including notes on motivation for this representation in initialnts*.md.
;;; see src/sims/bali/old for earlier experiments, including notes about choices.

(ns sims.bali.propns
  (:use popco.core.lot)
  (:require [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]))

;; PROPOSITION NAMING CONVENTIONS:
;; Spiritual propns start with "SB" or "SP".
;; Worldly propns start with "WB" or "WP".
;; Brahmanic propns include "B" in prefix.
;; peasant/subak propns include "P" in prefix.
;; Propns shared between Brahmanic and peasant domains would start with "SX" or "WX" (but there are none).

(defpred Causal-if)
(defpred Ceases)
(defpred Fails)
(defpred Is-ordered)
(defpred Is-disordered)
(defpred Is-pest)
(defpred Is-enemy-of-state)
(defpred Is-demon)
(defpred Member-of)
(defpred Nourishes)
(defpred Persists)
(defpred Shares)
(defpred Struggles-alone)
(defpred Struggles-together)
(defpred Struggles-on-behalf)
(defpred Succeeds)

(defobj king)
(defobj state)
(defobj demon)
(defobj demon)
(defobj water)
(defobj rice)
(defobj enemy)
(defobj peasant1)
(defobj peasant2)
(defobj subak)
(defobj rat)

;; Note these have no effect if there is no map node that maps the two predicates.
;; There can only be a map node if there exist propositions using one predicate
;; in one of the major analogues (i.e. "source" and "target", formerly), and one
;; in the other.  Here the major analogues are "worldly" and "spiritual".
(def conceptual-relats 
  [[-1.0 :Is-ordered :Is-disordered]
   [-1.0 :Persists :Ceases]
   [-1.0 :Succeeds :Fails]])

(def semantic-iffs [
                    ;[-0.1 :WP-peasants-against-rat :SB-king-against-demon] ; (check) adding this links the 2-node subnet into the rest in siobhan e.g. but the negative activn isn't flowing into the other node:
                    ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; "WORLDLY" PROPOSITIONS (one of the two major analogues--one side of the mappings in the analogy net)

(def worldly-brahmanic-propns 
  [(defpropn WB-water-nourishes-state Nourishes [water state])  ;; "nourishes"??  "state"?? 
   (defpropn WB-king-for-subaks Struggles-on-behalf [king subak])
   (defpropn WB-king-for-peasant1 Struggles-on-behalf [king peasant1])
   (defpropn WB-king-for-peasant2 Struggles-on-behalf [king peasant2])
   (defpropn WB-king-against-enemy Struggles-alone [king enemy])
   (defpropn WB-king-succeeds-against-enemy Succeeds [WB-king-against-enemy])
   (defpropn WB-state-ordered Is-ordered [state])
   (defpropn WB-state-disordered Is-disordered [state])
   (defpropn WB-king-succeed-enemy->order Causal-if [WB-king-succeeds-against-enemy WB-state-ordered])
   (defpropn WB-king-fails-against-enemy Fails [WB-king-against-enemy])
   (defpropn WB-state-fail-enemy->disorder Causal-if [WB-king-fails-against-enemy WB-state-disordered])
   (defpropn WB-state-persists Persists [state])
   (defpropn WB-state-ordered->persists Causal-if [WB-state-ordered WB-state-persists])
   (defpropn WB-state-ceases Ceases [state])
   (defpropn WB-state-disordered->ceases Causal-if [WB-state-disordered WB-state-ceases])])

(def worldly-peasant-propns
  [(defpropn WP-peasant1-in-subak Member-of [peasant1 subak])
   (defpropn WP-peasant2-in-subak Member-of [peasant2 subak])
   (defpropn WP-subak-ordered Is-ordered [subak])
   (defpropn WP-subak-disordered Is-disordered [subak])
   (defpropn WP-subak-persists Persists [subak])
   (defpropn WP-subak-ordered->persists Causal-if [WP-subak-ordered WP-subak-persists])
   (defpropn WP-subak-ceases Ceases [subak])  ; should always receive negative activation
   (defpropn WP-subak-disordered->persists Causal-if [WP-subak-disordered WP-subak-persists]) ; NOTE this differs from Brahmanic
   (defpropn WP-subak-shares-water Shares [subak water]) ; Should really have multiple subaks as args; this is a simplification.
   (defpropn WP-water-nourishes-rice Nourishes [water rice]) ;
   (defpropn WP-water-rice-ordered Is-ordered [WP-water-nourishes-rice])
   (defpropn WP-peasants-against-rat Struggles-together [peasant1 peasant2 rat])
   (defpropn WP-subak-succeeds-against-rat Succeeds [WP-peasants-against-rat])
   (defpropn WP-subak-succeed-rat->order Causal-if [WP-subak-succeeds-against-rat WP-subak-ordered])
   (defpropn WP-subak-fails-against-rat Fails [WP-peasants-against-rat])
   (defpropn WP-subak-fail-rat->disorder Causal-if [WP-subak-fails-against-rat WP-subak-disordered])])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; "SPIRITUAL" PROPOSITIONS (one of the two major analogues--one side of the mappings in the analogy net)

(def spiritual-brahmanic-propns 
  [(defpropn SB-water-nourishes-state Nourishes [water state])  ;; "nourishes"??  "state"?? 
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

(def spiritual-peasant-propns
  [(defpropn SP-peasant1-in-subak Member-of [peasant1 subak])
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
   (defpropn SP-subak-succeeds-against-demon Succeeds [SP-peasants-against-demon])
   (defpropn SP-subak-succeed-demon->order Causal-if [SP-subak-succeeds-against-demon SP-subak-ordered])
   (defpropn SP-subak-fails-against-demon Fails [SP-peasants-against-demon])
   (defpropn SP-subak-fail-demon->disorder Causal-if [SP-subak-fails-against-demon SP-subak-disordered])])

;(println "Numbers of propositions:")
;(println "worldly brahman: " (count worldly-brahmanic-propns))
;(println "spiritual brahman: " (count spiritual-brahmanic-propns))
;(println "worldly peasant: " (count worldly-peasant-propns))
;(println "spiritual peasant: " (count spiritual-peasant-propns))
