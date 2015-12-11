;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.netlogo
  (:require [popco.core.main :as mn]
            [popco.core.person :as prs]
            [popco.core.population :as pp]
            ;[popco.communic.listen :as cl]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]
            [sims.bali.collections :as c]))

(def num-subaks 172)

;; I adopt the convention of naming variables containing atoms with a trailing ampersand:
(def popn&
  ;;               ID   UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/worldly-perc-pnet     c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/worldly-neg-perc-pnet c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["ignored"]    num-subaks  nil         prs/constantly1)]
    (atom 
      (pp/make-population
        (vec (concat [aat aaf] ; TODO adjust below if number of pundits changes
                     (map (partial prs/new-person-from-old subak)
                          (map double (range num-subaks))))))))) ; subak ids: Doubles from 0 to num-subaks-1. that's what NetLogo will send.

(defn update-talk-to-persons
  "Update talk-to-persons fields in persons in popn based on args:
  speaker-ids and listener-id-seqs are sequences of the same length.  Each
  sequence of speaker ids in listener-id-seqs provides the ids to be used to
  fill talk-to-persons in the person with the corresponding id in speaker-ids."
  [popn speaker-listener-map]
  (assoc popn :persons                                                   ; replace persons in popn with
         (map #(assoc % :talk-to-persons (speaker-listener-map (:id %))) ; old persons but w/ talk-to-persons updated from speaker-listener-map
              (:persons popn))))

(def num-worldly-peasant-propns (count c/worldly-peasant-propn-idxs))

(defn avg-worldly-peasant-activn
  "Computes mean of activations of worldly-peasant propns."
  [pers]
  (/ (mx/esum
       (mx/select (:activns (:propn-net pers)) 
                  c/worldly-peasant-propn-idxs))
     num-worldly-peasant-propns))

(defn avg-worldly-peasant-activns
  "Returns sequence of mean activations of worldly-peasant propns for each subak."
  [popn]
  (map avg-worldly-peasant-activn 
       (drop 2                  ; skip pundits TODO ADJUST IF PUNDITS CHANGE
             (:persons popn))))

(defn bali-once
  "Run once on population after updating its members' talk-to-persons fields.
  speaker-listener-hashtable is a java.util.HashTable in which keys are person
  ids and values are sequences of ids of persons to talk to."
  [speaker-listener-hashtable]
  (let [speaker-listener-map (into {} speaker-listener-hashtable)]
    (swap! popn& 
           (mn/once 
             (update-talk-to-persons @popn& speaker-listener-map)))
    (avg-worldly-activns @popn&))) ;; return per-subak average worldly activn vals
