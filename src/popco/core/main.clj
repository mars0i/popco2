(ns popco.core.main
  (:use popco.core.population)
  (:require [popco.nn.update :as up]
            [popco.nn.nets :as nn]
            [popco.communic.receive :as cr]
            [popco.communic.send :as cs]
            [utils.general :as ug]
            [clojure.core.matrix :as mx])) ; for transpose

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SEE src/popco/start.md and src/popco/core/main.md for notes. ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare once many-times unparalleled-many-times ticker inc-tick)

(defn many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate once popn))

(defn unparalleled-many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate (partial once map) popn))

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  ([popn] (once pmap popn))
  ([mapfn popn]
   (assoc popn
          :tick (inc (:tick popn))
          :persons (doall
                     (let [[persons utterance-maps] (mx/transpose
                                                     (mapfn (comp cs/speaker-plus-utterances up/update-person-nets)
                                                            (:persons popn)))
                           utterance-map (apply merge-with (comp vec concat) utterance-maps)] ; TODO: faster methods for join-pairs-...? cf. http://stackoverflow.com/questions/23745440/map-of-vectors-to-vector-of-maps
                       ;(clojure.pprint/pprint utterance-map)(flush)
                       (mapfn (partial cr/receive-transmissions utterance-map)
                              persons))))))

(defn ticker
  "Prints tick number to console, erasing previous tick number, and returns
  the population unchanged."
  [popn]
  (let [tickstr (str (:tick popn))]
    (ug/erase-chars (count tickstr)) ; new tick string is always at least as long as previous
    (print tickstr)
    (flush))
  popn)

(defn dotter
  "Prints dots to console, and returns the population unchanged."
  [popn]
  (print ".")
  (flush)
  popn)
