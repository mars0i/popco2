;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;; Two pundits, three isolated groups, each with spiritual propns, but 
;;; with different worldly biases: worldly-brahmanic, worldly-peasant, and both.
(ns sims.bali.threegroups2pundits
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.collections :as c]))

(let [spiritual+worldly-brahmanic-propns (concat c/spiritual-propns c/worldly-brahmanic-propns)
      spiritual+worldly-peasant-propns   (concat c/spiritual-propns c/worldly-peasant-propns)
;; args:                        ID    UNMASKED                           PROPN-NET                 ANALOGY-NET UTTERABLE-IDS          GROUPS      TALK-TO-GROUPS                  MAX-TALK-TO
      aat     (pers/make-person :aat  c/spiritual-propns                 c/spiritual-perc-pnet     c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1)
      aaf     (pers/make-person :aaf  c/spiritual-propns                 c/spiritual-neg-perc-pnet c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1)
      brahman (pers/make-person :brah spiritual+worldly-brahmanic-propns c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:brahmans] [:brahmans]                     1) 
      peasant (pers/make-person :peas spiritual+worldly-peasant-propns   c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:peasants] [:peasants]                     1)
      both    (pers/make-person :both c/all-propns                       c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:bothans]  [:bothans]                      1)] 
      
  (def popn (pp/make-population (vec
                                  (concat
                                    [aat aaf]
                                    (take 40 (pers/new-person-seq-from-old brahman))
                                    (take 40 (pers/new-person-seq-from-old peasant))
                                    (take 40 (pers/new-person-seq-from-old both)))))))


;; Note that the non-pundits that are first defined never do anything; they're simply models
;; for the creation of the actual population members.
