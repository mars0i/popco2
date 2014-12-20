;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.successbias
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.communic.listen :as cl]
            [sims.bali.collections :as c]
            [sims.bali.success :as s]))

(let [;spiritual+worldly-brahmanic-propns (concat c/spiritual-propns c/worldly-brahmanic-propns)
      spiritual+worldly-peasant-propns   (concat c/spiritual-propns c/worldly-peasant-propns)
;; args:                        ID    UNMASKED                           PROPN-NET                 ANALOGY-NET UTTERABLE-IDS          GROUPS      TALK-TO-GROUPS             MAX-TALK-TO BIAS-FILTER           QUALITY-FN
      aat     (pers/make-person :aat  c/spiritual-propns                 c/spiritual-perc-pnet     c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1      nil                   (constantly 1))
      aaf     (pers/make-person :aaf  c/spiritual-propns                 c/spiritual-neg-perc-pnet c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1      nil                   (constantly 1))
     ;brahman (pers/make-person :brah spiritual+worldly-brahmanic-propns c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:brahmans] [:brahmans]                     1) 
      peasant (pers/make-person :peas spiritual+worldly-peasant-propns   c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:peasants] [:peasants]                     5      cl/max-quality-filter s/success)
     ;both    (pers/make-person :both c/all-propns                       c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:bothans]  [:bothans]                      1)
     ] 
      
  (def popn (pp/make-population (vec
                                  (concat
                                    [aat aaf]
                                    ;(take 50 (pers/new-person-seq-from-old brahman))
                                    (take 50 (pers/new-person-seq-from-old peasant))
                                    ;(take 50 (pers/new-person-seq-from-old both))
                                   )))))
