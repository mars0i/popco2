(ns sims.crime3.few1
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

(def all-propns (concat pns/crime-propns pns/living-propns))  ; ok if lazy; make-* functions will realize it
(def crime-ids (map :id pns/crime-propns)) ; ok if lazy; make-* functions will realize it
(def living-ids (map :id pns/living-propns))

;; PECEPTION-IFS
;; Make all crime propns perceived all the time
;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector cn/+one+ % :SALIENT) crime-ids)) ; ok if lazy; make-* functions will realize it

;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
(def pnet (pn/make-propn-net all-propns 
                             pns/semantic-iffs ; bidirectional links 
                             perception-ifs))  ; unidirectional links

;; ANALOGY NET (TO BE SHARED BY EVERYONE)
(def anet (an/make-analogy-net pns/crime-propns        ; first analogue
                               pns/living-propns       ; second analogue
                               pns/conceptual-relats)) ; related predicates

(def fred (pers/make-person :fred         ; name
                            all-propns  ; all of the propns I might know
                            pnet        ; proposition network structure
                            anet        ; analogy network structure
                            crime-ids   ; utterable-ids - ids of propns I'm willing to talk about
                            [:everyone] ; groups - what groups am I in
                            [] ; talk-to-groups groups whose members I am willing to talk to
                            1))         ; max-talk-to: max number of people I'll say something to per tick

(def joanne (pers/make-person :joanne 
                            all-propns
                            pnet
                            anet
                            living-ids
                            [:everyone]
                            []
                            1))

(def popn (pp/make-population [fred joanne]))
