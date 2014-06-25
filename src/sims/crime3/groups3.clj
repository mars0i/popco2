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
      crime-ids  (map :id pns/crime-propns) ; ok if lazy; make-* functions will realize it
      living-ids (map :id pns/living-propns)

      ;; PECEPTION-IFS
      ;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
      ;; ALL AND ONLY CRIME PROPNS ARE PERCEIVED
      perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns) ; ok if lazy; make-* functions will realize it

      ;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
      ;; ALL POSSIBLE PROPNS ARE UNMASKED HERE:
      pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs) ; second arg is bidirectional links; third is unidirectional

      ;; ANALOGY NET (TO BE SHARED BY EVERYONE)
      anet (an/make-analogy-net pns/crime-propns 
                                pns/living-propns 
                                pns/conceptual-relats)

      ;; args:              id propns pnet anet utterable-ids mygroups talk-to-groups max-talk-to
      e1 (pers/make-person :e1 propns pnet anet living-ids    [:east]  [:east :west]  2)
      w1 (pers/make-person :w1 propns pnet anet living-ids    [:west]  [:east :west]  2)]

  (def popn (pp/make-population (vec (concat
                                       [e1 w1]
                                       [(pers/new-person-from-old e1 :e2)
                                        (pers/new-person-from-old w1 :w2)]
                                       )))))
