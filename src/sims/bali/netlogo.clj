;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.netlogo
  (:require [popco.core.main :as mn]
            [popco.core.person :as prs]
            [popco.core.population :as pp]
            ;[popco.communic.listen :as cl]
            [popco.nn.analogy :as an]
            [sims.bali.collections :as c]))

(def num-subaks 172)

;; I adopt the convention of naming variables containing atoms with a trailing ampersand:
(def popn& (atom "not-yet-defined"))

(defn bali-init 
  "Create a population of popco persons representing subaks, storing it in popn&."
  []
               ;;               ID   UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/worldly-perc-pnet     c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/worldly-neg-perc-pnet c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["ignored"]    num-subaks  nil         prs/constantly1)]
    (reset! popn&
            (concat [aat aaf]
                    (map (partial prs/new-person-from-old subak)
                         (map double (range num-subaks))))))) ; subak ids: Doubles from 0 to num-subaks-1. that's what NetLogo will send.

(defn update-talk-to-persons
  "Update talk-to-persons fields in persons in popn based on args:
  speaker-ids and listener-id-seqs are sequences of the same length.  Each
  sequence of speaker ids in listener-id-seqs provides the ids to be used to
  fill talk-to-persons in the person with the corresponding id in speaker-ids."
  [popn speaker-ids listener-id-seqs]
  (let [subak-id-map (zipmap speaker-ids listener-ids)] ; can I make this in the NetLogo extension?? can I avoid making it?
    (assoc popn :persons 
           (map 
             #(assoc % :talk-to-persons 
                     (vec (subak-id-map (:id %)))) ; map then id, since subak ids are Doubles. note result is [] if speaker-id missing
             (:persons popn)))))

(defn avg-worldly-activns
  [popn]
  )

(defn bali-once
  "Run once on population after updating its members talk-to-persons fields.
  speaker-ids and listener-id-seqs are sequences of the same length.  Each
  sequence of speaker ids in listener-id-seqs provides the ids to be used to
  fill talk-to-persons in the person with the corresponding id in speaker-ids."
  [speaker-ids listener-id-seqs]
  (swap! popn& 
         (mn/once 
           (update-talk-to-persons @popn& speaker-ids listener-id-seqs)))
  (avg-worldly-activns @popn&) ;; return per-subak average worldly activn vals
  (reverse subak-ids)) ; for testing only - delete this line
