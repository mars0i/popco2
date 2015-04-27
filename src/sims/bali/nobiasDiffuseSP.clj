;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; TODO IN PROGRESS
;; Goal is to have peasant spiritual propns diffuse, and gradually enter into
;; the analogy net, and thus gradually come to be selected in contrast to
;; spiritual brahmanic propns, in response to worldly-peasant propns.

;; innov is the person who's supposed to diffuse the spiritual peasant propns.
;; she's the innovator.  

;; Question: What do I want the pundits to do???

;; Q: How did I do this in the Sanday sims in "Moderate Role"?
;; A: I gave most members of pop all propositions except the ones to
;; be diffused (the earth origin propns).  Then for each earth origin
;; propn, added one indiv that had all of the propns that the other
;; indvivs had, plus a unique earth origin propn.  These played the
;; pundit role.  The diffusion happened while the contrary worldly
;; analog, hunting, was salient.  Then I switched the salience from
;; hunting to parenting, and the earth origin propns kicked in.


(ns sims.bali.nobiasDiffuseSP
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.communic.listen :as cl]
            [sims.bali.collections :as c]
            [sims.bali.success :as s]))

(let [;spiritual+worldly-brahmanic-propns (concat c/spiritual-propns c/worldly-brahmanic-propns)
      ;spiritual+worldly-peasant-propns   (concat c/spiritual-propns c/worldly-peasant-propns)
      spiritual-brahmanic+worldly-peasant-propns (concat c/spiritual-brahmanic-propns c/worldly-peasant-propns)
      noSPanet (an/make-analogy-net spiritual-brahmanic-propns worldly-propns conceptual-relats) ;; DO I WANT WORLDLY BRAHMANIC PROPNS??

;; args:                        ID     UNMASKED                           PROPN-NET                 ANALOGY-NET UTTERABLE-IDS          GROUPS      TALK-TO-GROUPS             MAX-TALK-TO BIAS-FILTER           QUALITY-FN
      aat     (pers/make-person :aat   c/spiritual-propns                 c/spiritual-perc-pnet     c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1      nil                   (constantly 1))
      aaf     (pers/make-person :aaf   c/spiritual-propns                 c/spiritual-neg-perc-pnet c/anet      c/spiritual-propn-ids  [:pundits]  [:brahmans :peasants :bothans]  1      nil                   (constantly 1))
      innov   (pers/make-person :innov c/spiritual-peasant-propns                                               c/spiritual-peasant-propn-ids  ;; FIXME
      peasant (pers/make-person :peas  spiritual+worldly-peasant-propns   c/no-perc-pnet            c/noSPanet      c/spiritual-propn-ids  [:peasants] [:peasants]                     1      nil                   (constantly 1))] 
      
  (def popn (pp/make-population (vec
                                  (concat
                                    [aat aaf innov]
                                    (take 100 (pers/new-person-seq-from-old peasant)))))))

