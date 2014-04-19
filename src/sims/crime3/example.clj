(ns sims.crime3.example
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.main :as mn]
            [sims.crime3.propns :as pns]))



(def propns (concat pns/crime-propns pns/living-propns)) 

(def pnet (pn/make-propn-net propns))

(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               an/pos-link-increment 
                               an/neg-link-value 
                               pns/sem-relats))

(def jo (pers/make-person :jo 
                          (concat pns/crime-propns pns/living-propns) pnet anet))

(def job (pers/make-person :job 
                           (concat pns/crime-propns pns/beast-propns) pnet anet))

(def jov (pers/make-person :jov 
                           (concat pns/crime-propns pns/virus-propns) pnet anet))


(def popn (pp/->Population 0 [jo job jov]))
(reset! mn/folks popn)
;(swap! mn/folks assoc :members [jo job jov])

(mn/init popn)

(def popn-evol (mn/many popn))
;(def popn-evol (mn/popco popn))
