;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.


;; SIMPLE TEST OF WHETHER ANALOGIES WORKING AS INTENDED
;; No communication.  Everyone has all propositions, but differ in what they perceive.
;; Should duplicate results of crime*basicCheck*.lisp in popco 1.  cf. crime2BasicCheck.pdf (was in dir software/cohere/data).
(ns sims.crime3.hermits
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.crime3.propns :as p]))


      ;; args:                 ID      UNMASKED          PROPN-NET                 ANALOGY-NET  UTTERABLE-IDS        GROUPS            TALK-TO-GROUPS        MAX-TALK-TO
      ;;                            (propns entertained) (propns perceived)                   (propns I can say)   (Groups I'm in)   (Groups I talk to)    (Max number of people I talk to in one tick)
(let [bea    (pers/make-person :bea    p/all-propns      p/beast-perc-pnet         p/anet       []                   []                []                    0)
      becky  (pers/make-person :becky  p/all-propns      p/beastly-crime-perc-pnet p/anet       []                   []                []                    0)
      vicky  (pers/make-person :vicky  p/all-propns      p/viral-crime-perc-pnet   p/anet       []                   []                []                    0)
      virgil (pers/make-person :virgil p/all-propns      p/virus-perc-pnet         p/anet       []                   []                []                    0)]
  (def popn (pp/make-population [bea becky vicky virgil])))
