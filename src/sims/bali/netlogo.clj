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

(def popn$ (atom "not-yet-defined"))

(defn bali-init "Initialize population of popco persons representing subaks." []
               ;;               ID   UNMASKED   PROPN-NET      ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
  (let [subak (prs/make-person :temp all-propns c/no-perc-pnet c/anet      c/spiritual-propn-ids ["ignored"] ["ignored"]    num-subaks  nil         prs/constantly1)]
    (reset! popn$
            (map (partial prs/new-person-from-old subak)
                 (map double (range num-subaks)))))) ; subak ids: Doubles from 0 to num-subaks. that's what NetLogo will send.

(defn bali-once
  [subak-ids]
  ;; (swap! popn$ <set talk-to-persons from input, run one tick>)
  ;; return per-subak average worldly activn vals
  (reverse subak-ids)) ; for testing only
