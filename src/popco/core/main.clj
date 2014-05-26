(ns popco.core.main
  (:use popco.core.population)
  (:require [popco.nn.update :as up]
            [popco.nn.nets :as nn]
            [popco.communic.listen :as cl]
            [popco.communic.speak :as cs]
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
  Returns the population in its new state.  popn is the popn before applying
  this function.  mapfn is either Clojure's pmap (default) or map.
  (Tip: If there's an exception, and the stacktrace doesn't show any popco
  functions, try using map instead of pmap.) Should be purely functional."
  ([popn] (once pmap popn))
  ([mapfn popn]
   ;; Note speaker-plus-utterances merely passes through persons from update-person-nets. (Avoids restarting pmap.)
   (let [[pre-communic-persons utterance-maps] (mx/transpose
                                                 ; Getting error
                                                 ;  RuntimeException Can't convert to persistent vector array: inconsistent shape.
                                                 ; here from transpose.  I think it's that seeing a lazy seq outside triggers a call
                                                 ; to core.matrix internal function convert-to-nested-vectors in persistent-vector.clj.
                                                 ; And I think that doesn't like something about different dimensionalities inside.
                                                 ; i.e. because you have persons on one side and vectors of maps on the other.
                                                 ; you can fix this by mapping seq over the next line, but then 
                                                 ; combine-speaker-utterance-maps is unhappy.
                                                    (mapfn (comp cs/speaker-plus-utterances up/update-person-nets)
                                                           (:persons popn)))
         ;; Communication crossover: switch from mapping over speakers to mapping over listeners.
         post-communic-persons (mapfn (partial cl/receive-utterances 
                                                (cl/combine-speaker-utterance-maps utterance-maps))
                                       pre-communic-persons)]
     (assoc popn
            :tick (inc (:tick popn))
            :persons post-communic-persons))))

 ; Combine firsts, seconds from [pers, utterance-map] pairs produced by speaker-plus-utterances.

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
  "Prints a period to console and returns the population unchanged."
  [popn]
  (print ".")
  (flush)
  popn)
