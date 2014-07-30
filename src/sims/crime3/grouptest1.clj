(ns sims.crime3.grouptest1
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

;; This is modeled on popco/crime/crime3socnet?.lisp .
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
      crime-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns)
      ;virus-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/virus-propns)
      ;beast-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/beast-propns)

      ;; PROPOSITION NETS (TEMPLATES FOR INDIVIDUAL NETS)
      no-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs nil) ; second arg is bidirectional links; third is unidirectional
      crime-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs crime-perception-ifs) ; second arg is bidirectional links; third is unidirectional
      ;virus-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs virus-perception-ifs) ; second arg is bidirectional links; third is unidirectional
      ;beast-perc-pnet (pn/make-propn-net all-propns pns/semantic-iffs beast-perception-ifs) ; second arg is bidirectional links; third is unidirectional

      ;; ANALOGY NET (TO BE SHARED BY EVERYONE)
      anet (an/make-analogy-net pns/crime-propns pns/living-propns pns/conceptual-relats)

      ;; args:              ID      UNMASKED           PROPN-NET        ANALOGY-NET  UTTERABLE-IDS        GROUPS I'M IN     TALK-TO-GROUPS        MAX-TALK-TO

      ;; The pundit has no biases, but fully believes all crime propns.  It might also be called "environment" or "world".  It's what feeds information to others.
      aa   (pers/make-person :aa    pns/crime-propns   crime-perc-pnet  anet         pns/crime-propn-ids  [:pundits]        [:vulcans :bajorans]  1)  ; PUNDIT: talks to everyone, listens to no one

      ;; Vulcans have a virus-bias, but don't initially perceive anything.
      vul1 (pers/make-person :vul1  crime+virus-propns no-perc-pnet     anet         pns/crime-propn-ids  [:vulcans]        [:vulcans]            1)  ; A normal Vulcan: Talks only to Vulcans
      worf (pers/make-person :worf  crime+virus-propns no-perc-pnet     anet         pns/crime-propn-ids  [:vulcans :ds9]   [:vulcans :ds9]       1)  ; Worf is a Vulcan who also talks to DS9 officers

      ;; Bajorans have a beast-bias, but don't initially perceive anything.
      baj1 (pers/make-person :baj1  crime+beast-propns no-perc-pnet     anet         pns/crime-propn-ids  [:bajorans]       [:bajorans]           1)  ; A normal Bajoran: Talks only to Bajorans
      kira (pers/make-person :kira  crime+beast-propns no-perc-pnet     anet         pns/crime-propn-ids  [:bajorans :ds9]  [:bajorans :ds9]      1)  ; Kira is a Bajoran who also talks to DS9 officers
     ]

  (def popn (pp/make-population [aa
                                 vul1 worf (pers/new-person-from-old vul1 :vul2)
                                 baj1 kira (pers/new-person-from-old baj1 :baj2)])))
