;; Test of group/talk-to data structure creation
(ns sims.crime3.groups2
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns))  ; ok if lazy; make-* functions will realize it
(def crime-ids (map :id pns/crime-propns)) ; ok if lazy; make-* functions will realize it

;; PECEPTION-IFS
;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns)) ; ok if lazy; make-* functions will realize it

;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional links; third is unidirectional

;; ANALOGY NET (TO BE SHARED BY EVERYONE)
(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats))

(def c1 (pers/make-person :c1 propns pnet anet crime-ids [:central] [:central :west :east] 1))

(def e1 (pers/make-person :e1 propns pnet anet crime-ids [:east] [:east :west] 2))

(def s1 (pers/make-person :s1 propns pnet anet crime-ids [:west :east] [:central] 3)) ;"s1": split

(def w1 (pers/make-person :w1 propns pnet anet crime-ids [:west] [:west :east] 4))

(def popn (pp/make-population (vec (concat
                                     [c1 w1 e1 s1]
                                     (repeatedly 25 #(pers/new-person-from-old c1 :c2))
                                     (repeatedly 25 #(pers/new-person-from-old w1 :w2))
                                     (repeatedly 25 #(pers/new-person-from-old e1 :e2))
                                     (repeatedly 25 #(pers/new-person-from-old s1 :s2))))))

;; Useful in order to see what's1 going on:
; (do (println "\n" (:groups popn) "\n") (domap #(do (println (:id %) (:talk-to-groups %)) (println (:talk-to-persons %) "\n")) (sort-by :id (:persons popn))))
