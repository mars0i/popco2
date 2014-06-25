;; Test of group/talk-to data structure creation
(ns sims.crime3.groups2
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

(let[propns (concat pns/crime-propns pns/living-propns)  ; ok if lazy; make-* functions will realize it
     crime-ids (map :id pns/crime-propns) ; ok if lazy; make-* functions will realize it

     ;; PECEPTION-IFS
     ;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
     perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns) ; ok if lazy; make-* functions will realize it

     ;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
     pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs) ; second arg is bidirectional links; third is unidirectional

     ;; ANALOGY NET (TO BE SHARED BY EVERYONE)
     anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats)

     c (pers/make-person :c                      ; name
                         propns                   ; all of the propositions I might come to know
                         pnet                     ; proposition-network
                         anet                     ; analogy-network
                         crime-ids                ; utterable-ids: ids of propns I'm willing to talk about
                         [:central]               ; groups: what groups I'm in
                         [:central :west :east]   ; talk-to-groups: groups whose members I'm willing to say something to
                         1)                      ; max=talk-to: maximum number of people I'm willing to say something to

     e (pers/make-person :e propns pnet anet crime-ids [:east] [:east :west] 2)

     s (pers/make-person :s propns pnet anet crime-ids [:west :east] [:central] 3) ;"s": split

     w (pers/make-person :w propns pnet anet crime-ids [:west] [:west :east] 4)]

  (def popn (pp/make-population (vec (concat
                                       [c w e s]
                                       (repeatedly 25 #(pers/new-person-from-old c))
                                       (repeatedly 25 #(pers/new-person-from-old w))
                                       (repeatedly 25 #(pers/new-person-from-old e))
                                       (repeatedly 25 #(pers/new-person-from-old s)))))))

;; Useful in order to see what's going on:
; (do (println "\n" (:groups popn) "\n") (domap #(do (println (:id %) (:talk-to-groups %)) (println (:talk-to-persons %) "\n")) (sort-by :id (:persons popn))))
