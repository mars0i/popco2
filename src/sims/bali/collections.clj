;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.collections
  (:require [sims.bali.propns :as p]
            [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Convenient collections

;; Carry over definitions from sims.bali.propns:
(def conceptual-relats p/conceptual-relats)
(def semantic-iffs p/semantic-iffs)
(def worldly-brahmanic-propns p/worldly-brahmanic-propns )
(def worldly-peasant-propns p/worldly-peasant-propns)
(def spiritual-brahmanic-propns p/spiritual-brahmanic-propns )
(def spiritual-peasant-propns p/spiritual-peasant-propns)

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

(def spiritual-brahmanic-neg-perception-ifs (map #(vector -1.0 % :SALIENT) spiritual-brahmanic-propn-ids))
(def spiritual-peasant-neg-perception-ifs (map #(vector -1.0 % :SALIENT) spiritual-peasant-propn-ids))
(def spiritual-neg-perception-ifs (seq (set (concat spiritual-brahmanic-neg-perception-ifs spiritual-peasant-neg-perception-ifs))))

(def worldly-brahmanic-neg-perception-ifs (map #(vector -1.0 % :SALIENT) worldly-brahmanic-propn-ids))
(def worldly-peasant-neg-perception-ifs (map #(vector -1.0 % :SALIENT) worldly-peasant-propn-ids))
(def worldly-neg-perception-ifs (seq (set (concat worldly-brahmanic-neg-perception-ifs worldly-peasant-neg-perception-ifs))))


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

(def spiritual-neg-perc-pnet (pn/make-propn-net all-propns semantic-iffs spiritual-neg-perception-ifs))                     ; spiritual propns neg-perceived
(def spiritual-brahmanic-neg-perc-pnet (pn/make-propn-net all-propns semantic-iffs spiritual-brahmanic-neg-perception-ifs)) ; spiritual brahmanic propns neg-perceived
(def spiritual-peasant-neg-perc-pnet (pn/make-propn-net all-propns semantic-iffs spiritual-peasant-neg-perception-ifs))     ; spiritual peasant propns neg-perceived

(def worldly-neg-perc-pnet (pn/make-propn-net all-propns semantic-iffs worldly-neg-perception-ifs))                     ; worldly propns neg-perceived
(def worldly-brahmanic-neg-perc-pnet (pn/make-propn-net all-propns semantic-iffs worldly-brahmanic-neg-perception-ifs)) ; worldly brahmanic propns neg-perceived
(def worldly-peasant-neg-perc-pnet (pn/make-propn-net all-propns semantic-iffs worldly-peasant-neg-perception-ifs))     ; worldly peasant propns neg-perceived


;; Standard analogy net--can be shared by everyone:

(def anet (an/make-analogy-net spiritual-propns worldly-propns conceptual-relats))
