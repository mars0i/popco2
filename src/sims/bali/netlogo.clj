;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.netlogo
  (:require [popco.core.main :as mn]
            [popco.core.person :as prs]
            [popco.core.population :as pp]
            [popco.nn.analogy :as an]
            [sims.bali.collections :as c]
            [clojure.core.matrix :as mx]))

;; NOTE: I adopt the convention of naming variables containing atoms with a trailing ampersand.

(def num-subaks 172)

(defn add-id-as-group
  "Returns a person that's just like pers, but with an additional group identity
  whose name is identical to pers's id."
  [pers]
  (update pers :groups conj (:id pers)))


;; PUNDITS MUST BE FIRST
(def num-pundits 2) ; used in defs below to treat pundits and subaks differently.
(def initial-popn
  ;;                           ID    UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/worldly-perc-pnet     c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/worldly-neg-perc-pnet c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["bypassed"]   num-subaks  nil         prs/constantly1)]
    (pp/make-population
      (vec (concat [aat aaf] ; pundits are first 
                   (map (comp prs/randomize-unif-propn-activns ; EXPERIMENT: uniform random propn activns (should it be e.g. Gaussian?)
                              add-id-as-group              ; give it a group name identical to its id
                              (partial prs/new-person-from-old subak))
                        (map double (range num-subaks)))))))) ; subak ids are doubles from 0 to num-subaks-1. (That's what NetLogo will send.)



(def current-popn& (atom initial-popn))

;; To get the mean, we divide by num propns; to scale result from [-1,1] to [-0.5,0.5], we also divide by 2.
(def num-worldly-peasant-propns-2x (* 2 (count c/worldly-peasant-propn-idxs)))

(defn scaled-worldly-peasant-activn
  "Computes mean of activations of worldly-peasant propns in person pers and scales
  the result lie in [0,1]."
  [pers]
  ;(println (matrix :persistent-vector (:activns (:propn-net pers))))  ; DEBUG
  (+ 0.5    ; shift [-0.5,0.5] to [0,1]
     (/ (mx/esum
          (mx/select (:activns (:propn-net pers)) 
                     c/worldly-peasant-propn-idxs))
        num-worldly-peasant-propns-2x))) ; to get the mean, we divide by num propns; to scale result from [-1,1] to [-0.5,0.5], we also divide by 2

(defn scaled-worldly-peasant-activns
  "Returns sequence of mean activations of worldly-peasant propns for each subak,
  in the order in which subaks appear in (:persons popn)."
  [popn]
  (map scaled-worldly-peasant-activn 
       (drop num-pundits
             (:persons popn))))

(defn replace-subaks-talk-to-persons
  "Replace talk-to-persons fields in persons in popn speaker-listener-map,
  in which keys are person ids and values are sequences of ids of persons 
  the key person should talk to."
  [popn speaker-listener-map]
  (let [persons (:persons popn)
        replace-ttp (fn [pers] (assoc pers :talk-to-persons (speaker-listener-map (:id pers))))]
    (assoc popn :persons
           (concat (take num-pundits persons) ; leave pundits as is
                   (map replace-ttp (drop num-pundits persons)))))) ; replace subaks' talk-to-persons from speaker-listener-map

(defn bali-once
  "Run popco.core.main/once on population, after updating its members'
  talk-to-persons fields from speaker-listener-hashtable, which is a
  java.util.HashTable in which keys are person ids and values are sequences
  of ids of persons the key person should talk to.  Returns a sequence of
  per-subak average activations (currently of worldly peasant propns only)
  that will be used in place of relig-type in BaliPlus.nlogo.  Values in this
  sequence are in subak order, i.e. the order in (:persons @current-popn&)."
  [speaker-listener-hashtable]
  (let [speaker-listener-map (into {} speaker-listener-hashtable)] ; values are org.nlogo.api.LogoLists, but those are java.util.Collections, so OK
    ;(println speaker-listener-map) ; DEBUG
    ;(println (:utterance-map current-popn&)) ; DEBUG
    ;(println (map :talk-to-persons (:persons @current-popn&))) ; DEBUG
    (scaled-worldly-peasant-activns  ; return per-subak average worldly activn vals
      (swap! current-popn& 
             #(mn/once (replace-subaks-talk-to-persons % speaker-listener-map))))))
