;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.crime3.threegroups
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.crime3.propns :as pns]))

;; JUST LIKE grouptest1.clj, BUT WITH ONE ADDED PERSON WHO IS MISSING CRIME PROPNS
;; AND SO MUST LEARN THEM FROM COMMUNICATION.
;; Note name of population is: popn2.

;; This is roughly modeled on popco/crime/crime3socnet?.lisp .
;; See grouptest1.md for explanation.

;; ************************
;; TIPS:
;; - If a person starts with all propns unmasked, the analogy net will never change.
;; - If the only things uttered are propns that are already perceived, the propn net will never change.
;; ************************

(let [all-propns (concat pns/crime-propns pns/living-propns)  ; LIST ALL POSSIBLE PROPNS.  ok if lazy; make-* functions will realize it
      crime+virus-propns (concat pns/crime-propns pns/virus-propns)
      crime+beast-propns (concat pns/crime-propns pns/beast-propns)

      ;; PECEPTION-IFS
      ;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
      crime-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) pns/crime-propns)
      ;virus-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) pns/virus-propns)
      ;beast-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) pns/beast-propns)

      ;; PROPOSITION NETS (TEMPLATES FOR INDIVIDUAL NETS)
      no-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs nil) ; second arg is bidirectional links; third is unidirectional
      crime-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs crime-perception-ifs) ; second arg is bidirectional links; third is unidirectional
      ;virus-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs virus-perception-ifs) ; second arg is bidirectional links; third is unidirectional
      ;beast-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs beast-perception-ifs) ; second arg is bidirectional links; third is unidirectional

      ;; ANALOGY NET (TO BE SHARED BY EVERYONE)
      anet (an/make-analogy-net pns/crime-propns pns/living-propns pns/conceptual-relats)

      ;; PERSONS
      ;; Vulcans have virus-bias, but don't initially perceive anything.
      ;; Bajorans have beast-bias, but don't initially perceive anything.
      ;; The pundit (aa) has no biases, but fully believes all crime propns.  It's what feeds opinions to others.
      ;; Note that even pundit only talks to one person on each tick: max-talk-to = 1.  might want to change that, e.g. to represent what's perceived in a common environment.
      ;; (Group names come from the TV show "Star Trek: Deep Space Nine", about a space station named "Deep Space Nine".  Kira and Worf work together on the space station.)

      ;; args:               ID     UNMASKED            PROPN-NET        ANALOGY-NET  UTTERABLE-IDS        GROUPS            TALK-TO-GROUPS        MAX-TALK-TO
      ;;                          (propns entertained) (propns perceived)             (propns I can say)   (Groups I'm in)   (Groups I talk to)    (Max number of people I talk to in one tick)
    ;;temp
      aa   (pers/make-person :aa    pns/crime-propns    crime-perc-pnet  anet         pns/crime-propn-ids  [:pundits]        [:vulcans :bajorans :bothans]  1)  ; PUNDIT: talks to everyone, listens to no one

      vul1 (pers/make-person :vul1  crime+virus-propns  no-perc-pnet     anet         pns/crime-propn-ids  [:vulcans]        [:vulcans]            1)  ; NORMAL VULCAN: Talks only to Vulcans

      baj1 (pers/make-person :baj1  crime+beast-propns  no-perc-pnet     anet         pns/crime-propn-ids  [:bajorans]       [:bajorans]           1)  ; NORMAL BAJORAN: Talks only to Bajorans

      bot1 (pers/make-person :bot1  all-propns          no-perc-pnet     anet         pns/crime-propn-ids  [:bothans]        [:bothans]            1)  ; NORMAL BAJORAN: Talks only to Bajorans
     ]

  (def popn (pp/make-population (vec
                                  (concat
                                    [aa]
                                    (take 40 (repeatedly (partial pers/new-person-from-old vul1)))
                                    (take 40 (repeatedly (partial pers/new-person-from-old baj1)))
                                    (take 40 (repeatedly (partial pers/new-person-from-old bot1))))
                                  ))))
