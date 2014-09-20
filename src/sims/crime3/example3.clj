;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.crime3.example3
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns)) ; ok if lazy--will be realized by make-*
(def crime-ids (map :id pns/crime-propns) ; ok if lazy--will be realized by make-*

;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector 1.0 (:id %) :SALIENT) pns/crime-propns) ; ok if lazy--will be realized by make-*

(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional links; third is unidirectional

(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats))

(def jo (pers/make-person :jo propns pnet anet crime-ids [:everyone] [:everyone] 1))

(def job (pers/make-person :job propns pnet anet crime-ids [:everyone] [:everyone] 1))

(def jov (pers/make-person :jov propns pnet anet crime-ids [:everyone] [:everyone] 1))

(def popn (pp/make-population [jo job jov]))
