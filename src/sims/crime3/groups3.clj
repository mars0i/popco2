(ns sims.crime3.groups3
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

;; ************************
;; TIPS:
;; - If a person starts with all propns unmasked, the analogy net will never change.
;; - If the only things uttered are propns that are already perceived, the propn net will never change.
;; ************************

(let [propns (concat pns/crime-propns pns/living-propns)  ; ok if lazy; make-* functions will realize it

      virus-ids (map :id pns/virus-propns)
      beast-ids (map :id pns/beast-propns)
      viral-crime-ids (map :id pns/viral-crime-propns)
      beastly-crime-ids (map :id pns/beastly-crime-propns)
      crime-ids (map :id pns/crime-propns)
      living-ids (map :id pns/living-propns)

      ;; PECEPTION-IFS
      ;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
      crime-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns)
      virus-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/virus-propns)
      beast-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/beast-propns)

      ;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
      ;; ALL POSSIBLE PROPNS ARE UNMASKED HERE:
      no-perc-pnet (pn/make-propn-net propns pns/semantic-iffs nil) ; second arg is bidirectional links; third is unidirectional
      crime-perc-pnet (pn/make-propn-net propns pns/semantic-iffs crime-perception-ifs) ; second arg is bidirectional links; third is unidirectional
      virus-perc-pnet (pn/make-propn-net propns pns/semantic-iffs virus-perception-ifs) ; second arg is bidirectional links; third is unidirectional
      beast-perc-pnet (pn/make-propn-net propns pns/semantic-iffs beast-perception-ifs) ; second arg is bidirectional links; third is unidirectional

      ;; ANALOGY NET (TO BE SHARED BY EVERYONE)
      both-bias-anet (an/make-analogy-net pns/crime-propns pns/living-propns pns/conceptual-relats)
      virus-bias-anet (an/make-analogy-net pns/crime-propns pns/virus-propns pns/conceptual-relats)
      beast-bias-anet (an/make-analogy-net pns/crime-propns pns/beast-propns pns/conceptual-relats)

      ;; args:              id unmasked pnet            anet utterable-ids mygroups talk-to-groups max-talk-to
      e1 (pers/make-person :e1 propns   crime-perc-pnet virus-bias-anet crime-ids    [:east]  [:east :west]  2)
      w1 (pers/make-person :w1 propns   crime-perc-pnet beast-bias-anet crime-ids    [:west]  [:east :west]  2)]

  (def popn (pp/make-population (vec (concat
                                       [e1 w1]
                                       [(pers/new-person-from-old e1 :e2)
                                        (pers/new-person-from-old w1 :w2)]
                                       )))))
