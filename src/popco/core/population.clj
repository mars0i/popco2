(ns popco.core.population
  (:require [clojure.set :as st]
            [utils.general :as ug]))


(declare invert-coll-map)

(defrecord Population [tick 
                       persons
                       group-to-persons
                       person-to-groups
                       person-talk-to-groups])

(ug/add-to-docstr ->Population
  "\n  tick: timestep.
  persons: members of the population at time tick.
  group-to-persons: map from group id to person ids representing membership.
  person-to-groups: map from person id to group ids--i.e. what groups am I in?
  person-talk-to-groups: map from person id to groups of persons to whom person talks.")

(defn make-population
  "ADD DOCSTRING"
  [persons group-to-persons person-talk-to-groups]
  (->Population 0 
                persons 
                group-to-persons 
                (invert-coll-map group-to-persons) 
                person-talk-to-groups))


;; THERE MUST BE A SIMPLER METHOD THAN THE FOLLOWING ....
;; And if I start revising groups during runtime, this might not be fast enough.

(defn split-coll-map
  "Given a map whose values are collections, return a sequence of maps each
  of which has a key from the original map, and one of the members of the 
  collection that was its value."
  [m]
  (mapcat 
    (fn [[k vs]]
      (map #(hash-map k %) vs))
    m))

(defn invert-coll-map
  "Given a map whose values are collections, return a map of the same sort,
  but in which each val member is now a key, and the members of their val
  collections are the keys for the current vals' former collections."
  [m]
  (apply merge-with 
         (comp vec flatten list) ; set instead of vec doesn't work. why?? flatten returns lazy seq.
         (map st/map-invert 
              (split-coll-map m))))

(defn invert-coll-map2
  "Given a map whose values are collections, return a map of the same sort,
  but in which each val member is now a key, and the members of their val
  collections are the keys for the current vals' former collections."
  [m]
  (apply merge-with 
         (comp flatten list)
         (map st/map-invert 
              (split-coll-map m))))

(defn invert-coll-map4
  "Given a map whose values are collections, return a map of the same sort,
  but in which each val member is now a key, and the members of their val
  collections are the keys for the current vals' former collections."
  [m]
  (apply merge-with 
         (comp flatten vector)
         (map st/map-invert 
              (split-coll-map m))))
