;;; This software is copyright 2015 by Marshall Abrams, and is distributed under
;;; the Gnu General Public License version 3.0 as specified in the file LICENSE.

(ns sims.bali.groupDiffuseSP
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.communic.listen :as cl]
            [popco.nn.analogy :as an]
            [sims.bali.collections :as c]))

;; The many indiv bindings below allow adding individual SP propns to persons.
;; Maybe in future use a macro or something to define all of this stuff automatically.

(let [;; structures with all propns except SP propns
      all-but-SP-propns (concat c/spiritual-brahmanic-propns c/worldly-peasant-propns)
      all-but-SP-anet (an/make-analogy-net c/spiritual-brahmanic-propns c/worldly-propns c/conceptual-relats)

      ;; define function that repeats a group so that its members are more likely to be talked to:
      talk-to-group-repeat 9 ; how many times to repeat talk-to groups to give them an advantage in communication over arbitrary members of the pop
      make-t2g (fn [repeating-group & other-groups] (vec (concat other-groups (repeat talk-to-group-repeat repeating-group))))

;; args:                         ID     UNMASKED           PROPN-NET                 ANALOGY-NET     UTTERABLE-IDS         GROUPS                TALK-TO-GROUPS                 MAX-TALK-TO BIAS-FILTER QUALITY-FN
      aat      (pers/make-person :aat   c/spiritual-propns c/spiritual-perc-pnet     c/anet          c/spiritual-propn-ids [:pundits]            [:peasants]                    1           nil         (constantly 1))
      aaf      (pers/make-person :aaf   c/spiritual-propns c/spiritual-neg-perc-pnet c/anet          c/spiritual-propn-ids [:pundits]            [:peasants]                    1           nil         (constantly 1))
      ;; talk-to-groups: members of my group are 10X more likely to be spoken to (total # = 180 = 9*9 + 99):
      peasant0 (pers/make-person :peas0 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti0 :peasants] (make-t2g :masceti0 :peasants) 1           nil         (constantly 1))
      peasant1 (pers/make-person :peas1 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti1 :peasants] (make-t2g :masceti1 :peasants) 1           nil         (constantly 1))
      peasant2 (pers/make-person :peas2 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti2 :peasants] (make-t2g :masceti2 :peasants) 1           nil         (constantly 1))
      peasant3 (pers/make-person :peas3 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti3 :peasants] (make-t2g :masceti3 :peasants) 1           nil         (constantly 1))
      peasant4 (pers/make-person :peas4 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti4 :peasants] (make-t2g :masceti4 :peasants) 1           nil         (constantly 1))
      peasant5 (pers/make-person :peas5 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti5 :peasants] (make-t2g :masceti5 :peasants) 1           nil         (constantly 1))
      peasant6 (pers/make-person :peas6 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti6 :peasants] (make-t2g :masceti6 :peasants) 1           nil         (constantly 1))
      peasant7 (pers/make-person :peas7 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti7 :peasants] (make-t2g :masceti7 :peasants) 1           nil         (constantly 1))
      peasant8 (pers/make-person :peas8 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti8 :peasants] (make-t2g :masceti8 :peasants) 1           nil         (constantly 1))
      peasant9 (pers/make-person :peas9 all-but-SP-propns  c/no-perc-pnet            all-but-SP-anet c/spiritual-propn-ids [:masceti9 :peasants] (make-t2g :masceti9 :peasants) 1           nil         (constantly 1))
     ] 

  (def popn (pp/make-population (vec (concat [aat aaf]
                                             (take 10 (pers/new-person-seq-from-old peasant0))
                                             (take 10 (pers/new-person-seq-from-old peasant1))
                                             (take 10 (pers/new-person-seq-from-old peasant2))
                                             (take 10 (pers/new-person-seq-from-old peasant3))
                                             (take 10 (pers/new-person-seq-from-old peasant4))
                                             (take 10 (pers/new-person-seq-from-old peasant5))
                                             (take 10 (pers/new-person-seq-from-old peasant6))
                                             (take 10 (pers/new-person-seq-from-old peasant7))
                                             (take 10 (pers/new-person-seq-from-old peasant8))
                                             (take 10 (pers/new-person-seq-from-old peasant9)))))))

