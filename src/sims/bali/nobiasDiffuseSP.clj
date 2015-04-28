;;; This software is copyright 2015 by Marshall Abrams, and is distributed under
;;; the Gnu General Public License version 3.0 as specified in the file LICENSE.

;; TODO IN PROGRESS
;; Goal is to have peasant spiritual propns diffuse, and gradually enter into
;; the analogy net, and thus gradually come to be selected in contrast to
;; spiritual brahmanic propns, in response to worldly-peasant propns.

;; innov is the person who's supposed to diffuse the spiritual peasant propns.
;; she's the innovator.  Wait--is that necessary?  the True Believer and the
;; Naysayer are already doing that.  Um, unless I restrict them to only asserting
;; non-SP propns.

;; Question: What do I want the pundits to do???

;; Q: How did I do this in the Sanday sims in "Moderate Role"?
;; A: I gave most members of pop all propositions except the ones to
;; be diffused (the earth origin propns).  Then for each earth origin
;; propn, added one indiv that had all of the propns that the other
;; indvivs had, plus a unique earth origin propn.  These played the
;; pundit role.  The diffusion happened while the contrary worldly
;; analog, hunting, was salient.  Then I switched the salience from
;; hunting to parenting, and the earth origin propns kicked in.

;; The thing is, I want the propns to diffuse, but I don't want them to
;; necessarily have high activation.  I just want them available in the
;; analogy net.  They have to have a certain amount of activation in 
;; order to get uttered.  But I want perception of the environment
;; to be the driver of proposition activation.  I don't have these
;; things separated.  Then again, I don't think the diffused propns
;; were perceived in the Sanday model 2 simulation.
;; Yep. "... they do end up getting activations other than zero, due to 
;; mappings in which they participate imperfectly."

;; OK so maybe this is what I want:
;; innov, The innovator is just another peasant, except that she has the new
;; spiritual peasant ideas as well as the other three domains.
;; 
;; This will work for the no-bias case, I think, but maybe not for the
;; success bias case, since then she will have a complete set of spiritual
;; propns.  Won't that make her more successful?
;;
;; So maybe I need to do it the Sanday model 2 way, and split the spiritual
;; propns up between diff indivs.


(ns sims.bali.nobiasDiffuseSP
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.communic.listen :as cl]
            [sims.bali.collections :as c]
            [sims.bali.success :as s]))

(let [;spiritual+worldly-brahmanic-propns (concat c/spiritual-propns c/worldly-brahmanic-propns)
      ;spiritual+worldly-peasant-propns   (concat c/spiritual-propns c/worldly-peasant-propns)
      allButSPpropns (concat c/spiritual-brahmanic-propns c/worldly-peasant-propns)
      allButSPanet (an/make-analogy-net spiritual-brahmanic-propns worldly-propns conceptual-relats) ;; DO I WANT WORLDLY BRAHMANIC PROPNS??

;; args:                        ID     UNMASKED                 PROPN-NET                 ANALOGY-NET    UTTERABLE-IDS                 GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER           QUALITY-FN
      aat     (pers/make-person :aat   c/worldly-peasant-propns c/spiritual-perc-pnet     c/anet         c/spiritual-propn-ids         [:pundits]  [:peasants]    1           nil                   (constantly 1))
      aaf     (pers/make-person :aaf   c/worldly-peasant-propns c/spiritual-neg-perc-pnet c/anet         c/spiritual-propn-ids         [:pundits]  [:peasants]    1           nil                   (constantly 1))
      innov   (pers/make-person :innov c/all-propns             c/no-perc-pnet            c/anet         c/spiritual-peasant-propn-ids [:peasants] [:peasants]    1           nil                   (constantly 1))
      peasant (pers/make-person :peas  allButSPpropns           c/no-perc-pnet            c/allButSPanet c/spiritual-propn-ids         [:peasants] [:peasants]    1           nil                   (constantly 1))] 
      
  (def popn (pp/make-population (vec (concat [aat aaf innov]
                                             (take 100 (pers/new-person-seq-from-old peasant)))))))

