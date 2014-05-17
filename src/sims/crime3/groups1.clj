;; Test of group/talk-to data structure creation
(ns sims.crime3.groups1
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns)) 

;; PECEPTION-IFS
;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns))

;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional links; third is unidirectional

;; ANALOGY NET (TO BE SHARED BY EVERYONE)
(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats))

(def c (pers/make-person :c 
                          (concat pns/crime-propns pns/living-propns)
                          pnet anet [:central] [:central :west :east] 1))

(def split (pers/make-person :split
                          (concat pns/crime-propns pns/living-propns)
                          pnet anet [:west :east] [:central] 1))

(def w (pers/make-person :w 
                           (concat pns/crime-propns pns/beast-propns)
                           pnet anet [:west] [:west :east] 1))

(def e (pers/make-person :e 
                           (concat pns/crime-propns pns/virus-propns)
                           pnet anet [:east] [:east :west] 1))

(def popn (pp/make-population (vec (concat
                                     [c split w e]
                                     (repeatedly 1 #(pers/new-person-from-old c))
                                     (repeatedly 1 #(pers/new-person-from-old split))
                                     (repeatedly 1 #(pers/new-person-from-old w))
                                     (repeatedly 1 #(pers/new-person-from-old e))))))

;; Useful in order to see what's going on:
; (do (println "\n" (:groups popn) "\n") (domap #(do (println (:id %) (:talk-to-groups %)) (println (:talk-to-persons %) "\n")) (sort-by :id (:persons popn))))
