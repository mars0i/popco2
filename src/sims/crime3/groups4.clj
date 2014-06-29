;; Test of group/talk-to data structure creation
(ns sims.crime3.groups4
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
      crime-ids (map :id pns/crime-propns) ; ok if lazy; make-* functions will realize it

      ;; PECEPTION-IFS
      ;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
      crime-perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns) ; ok if lazy; make-* functions will realize it

      ;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
      pnet (pn/make-propn-net propns pns/semantic-iffs nil) ; second arg is bidirectional links; third is unidirectional
      pundit-pnet (pn/make-propn-net propns pns/semantic-iffs crime-perception-ifs) ; second arg is bidirectional links; third is unidirectional

      ;; ANALOGY NET (TO BE SHARED BY EVERYONE)
      anet (an/make-analogy-net pns/crime-propns 
                                pns/living-propns 
                                pns/conceptual-relats)

      pundit (pers/make-person :aa         ; name
                                                  pns/crime-propns ; beast bias on crime propns
                                                  pundit-pnet                    ; proposition-network
                                                  anet                    ; analogy-network
                                                  crime-ids               ; utterable-ids: ids of propns I'm willing to talk about
                                                  [:east]                 ; groups: what groups I'm in
                                                  [:west :east]           ; talk-to-groups: groups whose members I'm willing to say something to
                                                  1)                      ; max=talk-to: maximum number of people I'm willing to say something to

      virus-biased-crime-talker (pers/make-person :v         ; name
                                                  (concat pns/crime-propns pns/virus-propns) ; beast bias on crime propns
                                                  pnet                    ; proposition-network
                                                  anet                    ; analogy-network
                                                  crime-ids               ; utterable-ids: ids of propns I'm willing to talk about
                                                  [:east]                 ; groups: what groups I'm in
                                                  [:west :east]           ; talk-to-groups: groups whose members I'm willing to say something to
                                                  1)                      ; max=talk-to: maximum number of people I'm willing to say something to

      beast-biased-crime-talker (pers/make-person :b         ; name
                                                  (concat pns/crime-propns pns/beast-propns) ; virus bias on crime propns
                                                  pnet                    ; proposition-network
                                                  anet                    ; analogy-network
                                                  crime-ids               ; utterable-ids: ids of propns I'm willing to talk about
                                                  [:west]                 ; groups: what groups I'm in
                                                  [:west :east]           ; talk-to-groups: groups whose members I'm willing to say something to
                                                  1)                      ; max=talk-to: maximum number of people I'm willing to say something to
      ]

  (def popn (pp/make-population (vec (concat
                                       [pundit]
                                       (repeatedly 5 #(pers/new-person-from-old virus-biased-crime-talker))
                                       (repeatedly 5 #(pers/new-person-from-old beast-biased-crime-talker)))))))

;; Useful in order to see what's going on:
; (do (println "\n" (:groups popn) "\n") (domap #(do (println (:id %) (:talk-to-groups %)) (println (:talk-to-persons %) "\n")) (sort-by :id (:persons popn))))
