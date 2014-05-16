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

(defn join-pair-seq
  "Given a map whose values are collections, return a sequence of maps each
  of which has a key from the original map, and one of the members of the 
  collection that was its value.  This is essentially a join table of all
  unique pairs licensed by the original map."
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
         (comp flatten vector)
         (map st/map-invert 
              (join-pair-seq m))))

(defn invert-coll-map2
  [m]
  (let [v-elts (set (apply concat (vals m)))
        init-maps (map #(hash-map % []) v-elts) ; we need maps with empty colls as keys, so conj will work
        data-maps (for [[k v] m              ; a coll of maps from elts in val colls, to the keys of those colls
                        elt v-elts           ; actually this does the same thing as join-pair-seq
                        :when (contains? (set v) elt)] {elt k})] ; (set v) to make contains? work
    (apply merge-with conj 
           (concat init-maps
                   data-maps))))

