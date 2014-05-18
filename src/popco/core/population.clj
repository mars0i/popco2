(ns popco.core.population
  (:require [utils.general :as ug]
            [popco.core.person :as pers]))

(declare make-population init-popn make-population)

(defrecord Population [tick 
                       persons
                       groups])

(ug/add-to-docstr ->Population
  "\n  tick: timestep.
  persons: members of the population at time tick.
  groups: map from group id to person ids representing membership.
  person-to-groups: map from person id to group ids--i.e. what groups am I in?
  person-talk-to-groups: map from person id to groups of persons to whom person talks.")

(defn make-population
  [members]
  (let [person-to-groups (apply hash-map 
                                (mapcat #(vector (:id %) (:groups %))
                                        members))
        groups (ug/invert-coll-map person-to-groups)
        updated-members (map (partial pers/update-talk-to-persons groups) members)]
    (->Population 0 updated-members groups)))
