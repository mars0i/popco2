;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.main
;  (:use popco.core.population)
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
  "Returns a lazy sequence of population states, one for each tick.  Attempts 
  to split processing of persons across multiple CPU cores, if possible, through 
  use of the pmap function.  
  The first element in the resulting sequence is the original, unchanged Population 
  popn that was passed to this function.  Each subsequent population in the sequence 
  records the effects of operations by the function `once` on the previous population 
  in the sequence.  Note that the contents of field :utterance-map contains the 
  utterances that have been received by the persons in the :persons field, but that 
  these utterances won't have their full effects on each person's internal networks 
  until the next tick.  If the function more is present, it should be a function that
  takes a population as an argument and returns a population.  This function can be
  used, for example, to modify or add information to persons in the population,
  without having to modify the core communication processes of popco."
  [popn]
  (iterate once popn))

(defn unparalleled-many-times
  "Returns a lazy sequence of population states, one for each tick.  Does not
  split processing of persons across multiple CPU cores.
  The first element in the resulting sequence is the original, unchanged Population 
  popn that was passed to this function.  Each subsequent population in the sequence 
  records the effects of operations by the function `once` on the previous population 
  in the sequence.  Note that the contents of field :utterance-map contains the 
  utterances that have been received by the persons in the :persons field, but that 
  these utterances won't have their full effects on each person's internal networks 
  until the next tick."
  [popn]
  (iterate (partial once map) popn))

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  popn contains the state before applying
  this function.  mapfn is either Clojure's pmap (default) or map.
  (Tip: If there's an exception, and the stacktrace doesn't show any popco
  functions, try using map instead of pmap.) Should be purely functional.
  Sketch of sequence of operations:
  1. Update networks in each person: ...nn.update/update-person-nets.
     This involves, in each person:
     a. partially settling the analogy net
     b. updating proposition link weights from proposition map nodes in analogy net
     c. partially settling the proposition net
  2. Persons create utterances for listeners: ...communic.speak/speaker-plus-utterances.
  3. Send utterances to designated listeners: ...communic.listen/receive-utterances.
     This can cause changes to both the proposition net and the analogy net."
  ([popn] (once pmap popn))
  ([mapfn popn]
   ;; next mapfn expression creates seq of [person, utterance-map] pairs; transpose groups them as persons, utterance-maps.
   ;; Note speaker-plus-utterances merely passes through persons from update-person-nets. (Avoids restarting pmap.)
   (let [[pre-communic-persons speaker-utterance-maps] (mx/transpose
                                                         (mapfn (comp cs/speaker-plus-utterances cs/update-qualities up/update-person-nets)
                                                                (:persons popn)))
         utterance-map (cl/combine-speaker-utterance-maps speaker-utterance-maps) ; combine speaker-specific maps
         ;; Communication crossover: switch from mapping over speakers to mapping over listeners.
         post-communic-persons (mapfn (partial cl/receive-utterances utterance-map)
                                       pre-communic-persons)]
     (assoc popn
            :tick (inc (:tick popn))
            :persons post-communic-persons
            :utterance-map utterance-map))))

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
