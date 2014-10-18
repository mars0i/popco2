;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Namespace for aliases to functions used to report data from
;; the stream of generations/ticks.  (It was too hard to remember
;; where these functions lived, but it still made sense to have them
;; live in different homes.)  See original source files for documentation.

(ns popco.core.reporters
  (:require [popco.communic.listen]
            [popco.core.main]
            [popco.io.propncsv]
            [popco.io.gexf-dynamic] ; will probably change
            [popco.nn.nets]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; THESE FUNCTIONS TAKE A SINGLE POPULATION, GENERATE SOME
;; OUTPUT, AND RETURN THE POPULATION UNCHANGED.  This illustrates
;; a typical use, where popn contains an intial population:
;;    (def popns (map ticker (take 100 (many-times popn))))
;; Or e.g. using domap from utils/general:
;;    (def ps100 (take 100 (many-times popn)))
;;    (domap display-utterances ps100)

(def ticker                          popco.core.main/ticker)
(def dotter                          popco.core.main/dotter)

(def display-utterances              popco.communic.listen/display-utterances)

(def display-salient-wts             popco.nn.nets/display-salient-wts)
(def display-semantic-wts            popco.nn.nets/display-semantic-wts)
(def show-utterance-salient-effects  popco.nn.nets/show-utterance-salient-effects)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; THESE FUNCTIONS DO NOT RETURN POPULATIONS.
;; They expects an entire *sequence* of populations.  Store the population
;; sequence elsewhere if you want to reuse it.

(def write-propn-activns-csv         popco.io.propncsv/write-propn-activns-csv)
(def cook-name-for-R                 popco.io.propncsv/cook-name-for-R)

(def spit-graph                      popco.io.gexf-dynamic/spit-graph)
