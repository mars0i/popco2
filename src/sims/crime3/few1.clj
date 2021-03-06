;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.crime3.few1
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.crime3.propns :as pns]))

;; ************************
;; TIPS:
;; - If a person starts with all propns unmasked, the analogy net will never change.
;; - If the only things uttered are propns that are already perceived, the propn net will never change.
;; ************************

(def all-propns (concat pns/crime-propns pns/living-propns))  ; ok if lazy; make-* functions will realize it
(def crime-ids (map :id pns/crime-propns)) ; ok if lazy; make-* functions will realize it
(def living-ids (map :id pns/living-propns))

;; PECEPTION-IFS
;; Make all crime propns perceived all the time
;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector 1.0 % :SALIENT) crime-ids)) ; ok if lazy; make-* functions will realize it

;; PROPOSITION NET (TEMPLATE FOR INDIVIDUAL NETS)
(def pnet (pn/make-propn-net all-propns 
                             pns/semantic-iffs ; bidirectional links 
                             perception-ifs))  ; unidirectional links

;; ANALOGY NET (TO BE SHARED BY EVERYONE)
(def anet (an/make-analogy-net pns/crime-propns        ; first analogue
                               pns/living-propns       ; second analogue
                               pns/conceptual-relats)) ; related predicates

;; Two populations:

;; These people never talk:
(def silent (pp/make-population [(pers/make-person :fred       ; name
                                                    all-propns  ; all of the propns I might know
                                                    pnet        ; proposition network structure
                                                    anet        ; analogy network structure
                                                    crime-ids   ; utterable-ids - ids of propns I'm willing to talk about
                                                    [:everyone] ; groups - what groups am I in
                                                    []          ; talk-to-groups groups whose members I am willing to talk to
                                                    1)          ; max-talk-to: max number of people I'll say something to per tick
                                  (pers/make-person :joanne 
                                                    all-propns
                                                    pnet
                                                    anet
                                                    living-ids
                                                    [:everyone]
                                                    []
                                                    1)]))

;; Both people talk to each other:
(def talking (pp/make-population [(pers/make-person :fred       ; name
                                                    all-propns  ; all of the propns I might know
                                                    pnet        ; proposition network structure
                                                    anet        ; analogy network structure
                                                    crime-ids   ; utterable-ids - ids of propns I'm willing to talk about
                                                    [:everyone] ; groups - what groups am I in
                                                    [:everyone]          ; talk-to-groups groups whose members I am willing to talk to
                                                    1)          ; max-talk-to: max number of people I'll say something to per tick
                                  (pers/make-person :joanne 
                                                    all-propns
                                                    pnet
                                                    anet
                                                    living-ids
                                                    [:everyone]
                                                    [:everyone]
                                                    1)]))
