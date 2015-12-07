;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.population
  (:require [utils.general :as ug]
            [utils.string :as us]
            [popco.core.person :as pers]))

(declare make-population init-popn make-population)

(defrecord Population [tick 
                       persons
                       groups
                       utterance-map])

(us/add-to-docstr ->Population
  "\n  tick: timestep.
  persons: members of the population at time tick.
  groups: map from group id to person ids representing membership.
  utterance-map: used at runtime to pass utterances between the 
                 \"everyone speaks\" and \"everyone listens\" steps.")

;; non-lazy
;; utterance-map left empty at first
(defn make-population
  "Make a population containing the persons in members, after updating
  their talk-to-persons fields.  Also fills other fields of the population,
  except for utterance-map, which is initially nil."
  [members]
  (let [person-to-groups (apply hash-map 
                                (mapcat #(vector (:id %) (:groups %))
                                        members))
        groups (ug/invert-coll-map person-to-groups)
        updated-members (vec (map (partial pers/update-talk-to-persons groups) members))] ; vec: to constrain dimensions of laziness
    (->Population 0 updated-members groups nil)))  ; utterance-map empty at first

(defn person-ids
  "List IDs of persons in popn."
  [popn]
  (map :id (:persons popn)))

;; This does a linear search, so it's intended only for uses where efficiency
;; doesn't matter--especially for a large population.  An option is to add a
;; hash map to the population, and use this for the lookup, but that means
;; that two data structures have to be maintained in population when persons
;; are added or removed (unless we convert a map to a seq at runtime when we're
;; iterating through persons).
(defn get-person [id popn]
  "Return person with id from population popn, or nil."
  (some #(when 
           (= id (:id %))
           %)
        (:persons popn)))
