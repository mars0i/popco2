;; Namespace for aliases to functions used to report data from
;; the stream of generations/ticks.  (It was too hard to remember
;; where these functions lived, but it still made sense to have them
;; live in different homes.)
(ns popco.core.reporters
  (:require [popco.communic.listen]
            [popco.core.main]
            [popco.io.propncsv]
            [popco.nn.nets]))

(def ticker                    popco.core.main/ticker)
(def dotter                    popco.core.main/dotter)

(def display-utterances        popco.communic.listen/display-utterances)

(def write-propn-activns-csv   popco.io.propncsv/write-propn-activns-csv)

(def display-salient-wts       popco.nn.nets/display-salient-wts)
(def display-semantic-wts      popco.nn.nets/display-semantic-wts)
