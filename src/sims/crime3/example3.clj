(ns sims.crime3.example3
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns)) 
(def crime-ids (map :id pns/crime-propns))

;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns))

(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional links; third is unidirectional

(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats))

;(def group-to-persons {:everyone [:jo :job :jov]})

(def jo (pers/make-person :jo propns pnet anet crime-ids [:everyone] [:everyone] 1))

(def job (pers/make-person :job propns pnet anet crime-ids [:everyone] [:everyone] 1))

(def jov (pers/make-person :jov propns pnet anet crime-ids [:everyone] [:everyone] 1))

;(def persons [jo job jov])

(def popn (pp/make-population [jo job jov]))

;(def popn (pp/init-popn (pp/->Population 0 persons nil)))

;(def popn (pp/make-population persons 
;                           {:everyone persons}
;                           (apply merge (map #(hash-map % :everyone) persons))))

;(def popn* (pp/->Population 0 (map popco.nn.update/update-person-nets [jo job jov])))
;(def popn+ (pp/->Population 0 (map #(popco.nn.update/settle-analogy-net % popco.nn.constants/+settling-iters+) [jo job jov])))


