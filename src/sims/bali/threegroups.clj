;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.threegroups
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.propns :as p]))

(let [spiritual+worldly-brahmanic-propns (concat p/spiritual-propns p/worldly-brahmanic-propns)
      spiritual+worldly-peasant-propns   (concat p/spiritual-propns p/worldly-peasant-propns)
;; args:                       ID    UNMASKED                           PROPN-NET             ANALOGY-NET UTTERABLE-IDS          GROUPS      TALK-TO-GROUPS                  MAX-TALK-TO
      aa    (pers/make-person :aa    p/spiritual-propns                 p/spiritual-perc-pnet p/anet      p/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1)
      brah1 (pers/make-person :brah1 spiritual+worldly-brahmanic-propns p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:brahmans] [:brahmans]                     1) 
      peas1 (pers/make-person :peas1 spiritual+worldly-peasant-propns   p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:peasants] [:peasants]                     1) 
      both1 (pers/make-person :both1 p/all-propns                       p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:bothans]  [:bothans]                      1)]
      
  (def popn (pp/make-population (vec
                                  (concat
                                    [aa]
                                    (take 40 (repeatedly (partial pers/new-person-from-old brah1)))
                                    (take 40 (repeatedly (partial pers/new-person-from-old peas1)))
                                    (take 40 (repeatedly (partial pers/new-person-from-old both1))))
                                  ))))
