;;; This software is copyright 2015 by Marshall Abrams, and is distributed under
;;; the Gnu General Public License version 3.0 as specified in the file LICENSE.

;; TODO IN PROGRESS
;; Goal is to have peasant spiritual propns diffuse, and gradually enter into
;; the analogy net, and thus gradually come to be selected in contrast to
;; spiritual brahmanic propns, in response to worldly-peasant propns.
;; SEE nobiasDiffuseSP.md for more notes

(ns sims.bali.nobiasDiffuseSP
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.communic.listen :as cl]
            [popco.nn.analogy :as an]
            [sims.bali.collections :as c]))

;; The many indiv bindings below allow adding individual SP propns to persons.
;; Maybe in future use a macro or something to define all of this stuff automatically.

(let [;; an abbreviation for every propn in the SP group:
      sp0 (nth c/spiritual-peasant-propns 0), sp1 (nth c/spiritual-peasant-propns 1), sp2 (nth c/spiritual-peasant-propns 2), sp3 (nth c/spiritual-peasant-propns 3),
      sp4 (nth c/spiritual-peasant-propns 4), sp5 (nth c/spiritual-peasant-propns 5), sp6 (nth c/spiritual-peasant-propns 6), sp7 (nth c/spiritual-peasant-propns 7),
      sp8 (nth c/spiritual-peasant-propns 8), sp9 (nth c/spiritual-peasant-propns 9), sp10 (nth c/spiritual-peasant-propns 10), sp11 (nth c/spiritual-peasant-propns 11),
      sp12 (nth c/spiritual-peasant-propns 12), sp13 (nth c/spiritual-peasant-propns 13), sp14 (nth c/spiritual-peasant-propns 14), sp15 (nth c/spiritual-peasant-propns 15),
      sp16 (nth c/spiritual-peasant-propns 16), sp17 (nth c/spiritual-peasant-propns 17)

      ;; collection of all propns except the SP propns:
      all-but-SP-propns (concat c/spiritual-brahmanic-propns c/worldly-peasant-propns)

      ;; collections of propns with all of the non-SP propns, plus one SP propn:
      just-one-SP-propns-00 (conj all-but-SP-propns sp0)
      just-one-SP-propns-01 (conj all-but-SP-propns sp1)
      just-one-SP-propns-02 (conj all-but-SP-propns sp2)
      just-one-SP-propns-03 (conj all-but-SP-propns sp3)
      just-one-SP-propns-04 (conj all-but-SP-propns sp4)
      just-one-SP-propns-05 (conj all-but-SP-propns sp5)
      just-one-SP-propns-06 (conj all-but-SP-propns sp6)
      just-one-SP-propns-07 (conj all-but-SP-propns sp7)
      just-one-SP-propns-08 (conj all-but-SP-propns sp8)
      just-one-SP-propns-09 (conj all-but-SP-propns sp9)
      just-one-SP-propns-10 (conj all-but-SP-propns sp10)
      just-one-SP-propns-11 (conj all-but-SP-propns sp11)
      just-one-SP-propns-12 (conj all-but-SP-propns sp12)
      just-one-SP-propns-13 (conj all-but-SP-propns sp13)
      just-one-SP-propns-14 (conj all-but-SP-propns sp14)
      just-one-SP-propns-15 (conj all-but-SP-propns sp15)
      just-one-SP-propns-16 (conj all-but-SP-propns sp16)
      just-one-SP-propns-17 (conj all-but-SP-propns sp17)

      ;; an analogy net made from all propns except the SP propns:
      all-but-SP-anet (an/make-analogy-net c/spiritual-brahmanic-propns c/worldly-propns c/conceptual-relats) ;; DO I WANT WORLDLY BRAHMANIC PROPNS??

      ;; analogy nets made from all of the non-SP propns, plus one SP propn:
      just-one-SP-anet-00 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp0) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-01 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp1) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-02 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp2) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-03 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp3) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-04 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp4) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-05 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp5) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-06 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp6) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-07 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp7) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-08 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp8) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-09 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp9) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-10 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp10) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-11 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp11) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-12 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp12) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-13 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp13) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-14 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp14) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-15 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp15) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-16 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp16) c/worldly-propns c/conceptual-relats)
      just-one-SP-anet-17 (an/make-analogy-net (conj c/spiritual-brahmanic-propns sp17) c/worldly-propns c/conceptual-relats)

;; args:                        ID       UNMASKED                 PROPN-NET                       ANALOGY-NET         UTTERABLE-IDS                 GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
      aat     (pers/make-person :aat     c/worldly-peasant-propns c/worldly-peasant-perc-pnet     c/anet              c/worldly-peasant-propn-ids   [:pundits]  [:peasants]    1           nil         (constantly 1))
      aaf     (pers/make-person :aaf     c/worldly-peasant-propns c/worldly-peasant-neg-perc-pnet c/anet              c/worldly-peasant-propn-ids   [:pundits]  [:peasants]    1           nil         (constantly 1))
      innov00 (pers/make-person :innov00 just-one-SP-propns-00    c/no-perc-pnet                  just-one-SP-anet-00 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov01 (pers/make-person :innov01 just-one-SP-propns-01    c/no-perc-pnet                  just-one-SP-anet-01 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov02 (pers/make-person :innov02 just-one-SP-propns-02    c/no-perc-pnet                  just-one-SP-anet-02 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov03 (pers/make-person :innov03 just-one-SP-propns-03    c/no-perc-pnet                  just-one-SP-anet-03 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov04 (pers/make-person :innov04 just-one-SP-propns-04    c/no-perc-pnet                  just-one-SP-anet-04 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov05 (pers/make-person :innov05 just-one-SP-propns-05    c/no-perc-pnet                  just-one-SP-anet-05 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov06 (pers/make-person :innov06 just-one-SP-propns-06    c/no-perc-pnet                  just-one-SP-anet-06 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov07 (pers/make-person :innov07 just-one-SP-propns-07    c/no-perc-pnet                  just-one-SP-anet-07 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov08 (pers/make-person :innov08 just-one-SP-propns-08    c/no-perc-pnet                  just-one-SP-anet-08 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov09 (pers/make-person :innov09 just-one-SP-propns-09    c/no-perc-pnet                  just-one-SP-anet-09 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov10 (pers/make-person :innov10 just-one-SP-propns-10    c/no-perc-pnet                  just-one-SP-anet-10 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov11 (pers/make-person :innov11 just-one-SP-propns-11    c/no-perc-pnet                  just-one-SP-anet-11 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov12 (pers/make-person :innov12 just-one-SP-propns-12    c/no-perc-pnet                  just-one-SP-anet-12 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov13 (pers/make-person :innov13 just-one-SP-propns-13    c/no-perc-pnet                  just-one-SP-anet-13 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov14 (pers/make-person :innov14 just-one-SP-propns-14    c/no-perc-pnet                  just-one-SP-anet-14 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov15 (pers/make-person :innov15 just-one-SP-propns-15    c/no-perc-pnet                  just-one-SP-anet-15 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov16 (pers/make-person :innov16 just-one-SP-propns-16    c/no-perc-pnet                  just-one-SP-anet-16 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      innov17 (pers/make-person :innov17 just-one-SP-propns-17    c/no-perc-pnet                  just-one-SP-anet-17 c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil         (constantly 1))
      peasant (pers/make-person :peas    all-but-SP-propns        c/no-perc-pnet                  all-but-SP-anet     c/spiritual-propn-ids         [:peasants] [:peasants]    1           nil         (constantly 1))
     ] 
      
  (def popn (pp/make-population (vec (concat [aat aaf]
                                             [innov00 innov01 innov02 innov03 innov04 innov05 innov06 innov07 innov08 innov09 innov10 innov11 innov12 innov13 innov14 innov15 innov16 innov17]
                                             (take 80 (pers/new-person-seq-from-old peasant)))))))
