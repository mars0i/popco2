;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.hermits
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.collections :as c]))

;; SIMPLE TEST OF WHETHER ANALOGIES WORKING AS INTENDED
;; No communication.  Everyone has all propositions, but differ in what they perceive.


;; args:                                            ID      UNMASKED            PROPN-NET                       ANALOGY-NET  UTTERABLE-IDS        GROUPS            TALK-TO-GROUPS        MAX-TALK-TO
;;                                                        (propns entertained) (propns perceived)                           (propns I can say)   (Groups I'm in)   (Groups I talk to)    (Max # of people I talk to in a tick)
(let [spiritual-brahman-perceiver (pers/make-person :siobhan c/all-propns        c/spiritual-brahmanic-perc-pnet c/anet       []                   []                []                    0)
      spiritual-peasant-perceiver (pers/make-person :sophie  c/all-propns        c/spiritual-peasant-perc-pnet   c/anet       []                   []                []                    0)
      worldly-brahman-perceiver   (pers/make-person :wilbur  c/all-propns        c/worldly-brahmanic-perc-pnet   c/anet       []                   []                []                    0)
      worldly-peasant-perceiver   (pers/make-person :wilfred c/all-propns        c/worldly-peasant-perc-pnet     c/anet       []                   []                []                    0)]

  (def popn (pp/make-population [spiritual-brahman-perceiver
                                 spiritual-peasant-perceiver
                                 worldly-brahman-perceiver
                                 worldly-peasant-perceiver])))


;; spiritual peasant names: shep, spencer, stephen, stephan, sophia, sophie, stephanie, sasparilla
;; spiritutal brahman names: sebastian, shelby, sabrina, sybil, siobhan
;; worldly peasant names: none with p. using f for p: winifred, wilfred
;; worldly brahman names: wilbur, webster 
