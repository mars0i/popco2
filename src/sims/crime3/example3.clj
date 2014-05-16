(ns sims.crime3.example3
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns)) 

;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns))

(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional links; third is unidirectional

(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats))

(def jo (pers/make-person :jo 
                          (concat pns/crime-propns pns/living-propns) pnet anet))

(def job (pers/make-person :job 
                           (concat pns/crime-propns pns/beast-propns) pnet anet))

(def jov (pers/make-person :jov 
                           (concat pns/crime-propns pns/virus-propns) pnet anet))

(def persons [jo job jov])
(def person-ids (map :id persons))

;(def popn (pp/make-population persons 
;                           {:everyone persons}
;                           (apply merge (map #(hash-map % :everyone) persons))))

(def popn (pp/->Population 0 
                           persons
                           {:everyone person-ids}
                           (zipmap person-ids (repeat [:everyone]))))

;(def popn* (pp/->Population 0 (map popco.nn.update/update-person-nets [jo job jov])))
;(def popn+ (pp/->Population 0 (map #(popco.nn.update/settle-analogy-net % popco.nn.constants/+settling-iters+) [jo job jov])))


