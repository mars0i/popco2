;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.threegroups
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.collections :as c]))

(let [spiritual+worldly-brahmanic-propns (concat c/spiritual-propns c/worldly-brahmanic-propns)
      spiritual+worldly-peasant-propns   (concat c/spiritual-propns c/worldly-peasant-propns)
;; args:                        ID    UNMASKED                           PROPN-NET             ANALOGY-NET UTTERABLE-IDS          GROUPS      TALK-TO-GROUPS                  MAX-TALK-TO
      aa      (pers/make-person :aa   c/spiritual-propns                 c/spiritual-perc-pnet c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1)
      brahman (pers/make-person :brah spiritual+worldly-brahmanic-propns c/no-perc-pnet        c/anet      c/spiritual-propn-ids  [:brahmans] [:brahmans]                     1) 
      peasant (pers/make-person :peas spiritual+worldly-peasant-propns   c/no-perc-pnet        c/anet      c/spiritual-propn-ids  [:peasants] [:peasants]                     1) 
      both    (pers/make-person :both c/all-propns                       c/no-perc-pnet        c/anet      c/spiritual-propn-ids  [:bothans]  [:bothans]                      1)]
      
  (def popn (pp/make-population (vec
                                  (concat
                                    [aa]
                                    (take 40 (pers/new-person-seq-from-old brahman))
                                    (take 40 (pers/new-person-seq-from-old peasant))
                                    (take 40 (pers/new-person-seq-from-old both)))))))

;; old version used 11/17-18/2014 to make bali.rdata:
;                                    (take 40 (repeatedly (partial pers/new-person-from-old brahman)))
;                                    (take 40 (repeatedly (partial pers/new-person-from-old peasant)))
;                                    (take 40 (repeatedly (partial pers/new-person-from-old both))))
