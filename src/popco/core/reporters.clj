;; Namespace for aliases to functions used to report data from
;; the stream of generations/ticks.  (It was too hard to remember
;; where these functions lived, but it still made sense to have them
;; live in different homes.)
(ns popco.core.reporters
  (:require [popco.communic.listen]
            [popco.core.main]
            [popco.io.propncsv]
            [popco.nn.nets]))

;; MOST of these functions take a single population, generate some
;; output, and return the population unchanged.  This illustrates
;; a typical use, where popn contains an intial population:
;;    (def popns (map ticker (take 100 (many-times popn))))

(def ticker                          popco.core.main/ticker)
(def dotter                          popco.core.main/dotter)

(def display-utterances              popco.communic.listen/display-utterances)

(def display-salient-wts             popco.nn.nets/display-salient-wts)
(def display-semantic-wts            popco.nn.nets/display-semantic-wts)
(def show-utterance-salient-effects  popco.nn.nets/show-utterance-salient-effects)

;; THIS FUNCTION DOES NOT RETURN POPULATIONS.
;; It expects an entire *sequence* of populations, does not return it.
(def write-propn-activns-csv         popco.io.propncsv/write-propn-activns-csv)
