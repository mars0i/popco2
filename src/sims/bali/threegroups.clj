;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.threegroups
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.propns :as p]))

(let [spiritual+worldly-brahmanic-propns (concat p/spiritual-propns p/worldly-brahmanic-propns)
      spiritual+worldly-peasant-propns   (concat p/spiritual-propns p/worldly-peasant-propns)
;; args:                        ID    UNMASKED                           PROPN-NET             ANALOGY-NET UTTERABLE-IDS          GROUPS      TALK-TO-GROUPS                  MAX-TALK-TO
      aa      (pers/make-person :aa   p/spiritual-propns                 p/spiritual-perc-pnet p/anet      p/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1)
      brahman (pers/make-person :brah spiritual+worldly-brahmanic-propns p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:brahmans] [:brahmans]                     1) 
      peasant (pers/make-person :peas spiritual+worldly-peasant-propns   p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:peasants] [:peasants]                     1) 
      both    (pers/make-person :both p/all-propns                       p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:bothans]  [:bothans]                      1)]
      
  (def popn (pp/make-population (vec
                                  (concat
                                    [aa]
                                    (take 40 (pers/new-person-seq-from-old brahman))
                                    (take 40 (pers/new-person-seq-from-old peasant))
                                    (take 40 (pers/new-person-seq-from-old both)))))))

;; old version:
;                                    (take 40 (repeatedly (partial pers/new-person-from-old brahman)))
;                                    (take 40 (repeatedly (partial pers/new-person-from-old peasant)))
;                                    (take 40 (repeatedly (partial pers/new-person-from-old both))))
