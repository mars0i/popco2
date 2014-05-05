(ns sims.crime3.example3
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.main :as mn]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns)) 

;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector 1.0 (:id %) :SALIENT) pns/crime-propns))

(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional links; third is unidirectional

(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               an/+pos-link-increment+
                               an/+neg-link-value+
                               pns/conceptual-relats))

(def jo (pers/make-person :jo 
                          (concat pns/crime-propns pns/living-propns) pnet anet))

(def job (pers/make-person :job 
                           (concat pns/crime-propns pns/beast-propns) pnet anet))

(def jov (pers/make-person :jov 
                           (concat pns/crime-propns pns/virus-propns) pnet anet))

(def popn (pp/->Population 0 [jo job jov]))

;(mn/init popn) ; note popn is unchanged, but @folks has been updated.
