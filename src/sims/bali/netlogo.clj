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

(def num-pundits 2) ; used in defs below to treat pundits and subaks differently. PUNDITS MUST BE FIRST.

(defn add-id-as-group
  "Returns a person that's just like pers, but with an additional group identity
  whose name is identical to pers's id."
  [pers]
  (update pers :groups conj (:id pers)))

;; PUNDITS MUST BE FIRST
(def initial-popn
  ;;                           ID    UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/worldly-perc-pnet     c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/worldly-neg-perc-pnet c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["bypassed"]   num-subaks  nil         prs/constantly1)]
    (pp/make-population
      (vec (concat [aat aaf]
                   (map add-id-as-group
                        (map (partial prs/new-person-from-old subak)
                             (map double (range num-subaks))))))))) ; subak ids: Doubles from 0 to num-subaks-1. that's what NetLogo will send.

(def current-popn& (atom initial-popn))

(def num-worldly-peasant-propns (count c/worldly-peasant-propn-idxs))

(defn avg-worldly-peasant-activn
  "Computes mean of activations of worldly-peasant propns in person pers."
  [pers]
  ;(println (matrix :persistent-vector (:activns (:propn-net pers))))  ; DEBUG
  (/ (mx/esum
       (mx/select (:activns (:propn-net pers)) 
                  c/worldly-peasant-propn-idxs))
     num-worldly-peasant-propns))

(defn avg-worldly-peasant-activns
  "Returns sequence of mean activations of worldly-peasant propns for each subak."
  [popn]
  (map avg-worldly-peasant-activn 
       (drop num-pundits
             (:persons popn))))

(defn maybe-replace-talk-to-persons
  "Replaces talk-to-persons if pers is a non-upndit:  Given a speaker-listener
  map and an index, if idx is >= num-pundits, replaces pers's talk-to-persons
  field with the value in speaker-listener-map corresponding to its id."
  [speaker-listener-map idx pers]
  (if (< idx num-pundits)
    pers    ; pundits passed through unchanged
    (assoc pers :talk-to-persons (speaker-listener-map (:id pers))))) ; old persons but w/ talk-to-persons updated from speaker-listener-map

(defn replace-subaks-talk-to-persons
  "Update talk-to-persons fields in persons in popn based on args:
  speaker-ids and listener-id-seqs are sequences of the same length.  Each
  sequence of speaker ids in listener-id-seqs provides the ids to be used to
  fill talk-to-persons in the person with the corresponding id in speaker-ids."
  [popn speaker-listener-map]
  (assoc popn :persons                                                   ; replace persons in popn with
         (map-indexed (partial maybe-replace-talk-to-persons speaker-listener-map)
                      (:persons popn))))

(defn bali-once
  "Run popco.core.main/once on population, after updating its members'
  talk-to-persons fields from speaker-listener-hashtable, which is a
  java.util.HashTable in which keys are person ids and values are sequences
  of ids of persons the key person should talk to.  Returns a sequence of
  per-subak average activations (currently of worldly peasant propns only)
  that will be used in place of relig-type in BaliPlus.nlogo."
  [speaker-listener-hashtable]
  (let [speaker-listener-map (into {} speaker-listener-hashtable)] ; values are org.nlogo.api.LogoLists, but those are java.util.Collections, so OK
    ;(println speaker-listener-map) ; DEBUG
    ;(println (:utterance-map current-popn&)) ; DEBUG
    ;(println (map :talk-to-persons (:persons @current-popn&))) ; DEBUG
    (avg-worldly-peasant-activns  ; return per-subak average worldly activn vals
      (swap! current-popn& 
             #(mn/once (replace-subaks-talk-to-persons % speaker-listener-map))))))
