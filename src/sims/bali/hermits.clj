;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.hermits
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.propns :as p]))

;; SIMPLE TEST OF WHETHER ANALOGIES WORKING AS INTENDED
;; No communication.  Everyone has all propositions, but differ in what they perceive.


;; args:                          ID           UNMASKED            PROPN-NET                       ANALOGY-NET  UTTERABLE-IDS        GROUPS            TALK-TO-GROUPS        MAX-TALK-TO
;;                                           (propns entertained) (propns perceived)                           (propns I can say)   (Groups I'm in)   (Groups I talk to)    (Max number of people I talk to in one tick)
(let [sabrina    (pers/make-person :sabrina    p/all-propns        p/spiritual-brahmanic-perc-pnet p/anet       []                   []                []                    0)
      wilbur     (pers/make-person :wilbur     p/all-propns        p/worldly-brahmanic-perc-pnet   p/anet       []                   []                []                    0)
      sasparilla (pers/make-person :sasparilla p/all-propns        p/spiritual-peasant-perc-pnet   p/anet       []                   []                []                    0)
      winifred   (pers/make-person :winifred   p/all-propns        p/worldly-peasant-perc-pnet     p/anet       []                   []                []                    0)]
  (def popn (pp/make-population [sabrina wilbur sasparilla winifred])))

