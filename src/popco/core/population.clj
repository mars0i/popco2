(ns popco.core.population
  (:require [utils.general :as ug]
            [popco.core.person :as pers]))

(declare make-population update-person-talk-to init-popn make-population)

(defrecord Population [tick 
                       persons
                       group-to-persons])

(ug/add-to-docstr ->Population
  "\n  tick: timestep.
  persons: members of the population at time tick.
  group-to-persons: map from group id to person ids representing membership.
  person-to-groups: map from person id to group ids--i.e. what groups am I in?
  person-talk-to-groups: map from person id to groups of persons to whom person talks.")

(defn make-population
  [members]
  (init-popn (->Population 0 members nil)))

(defn init-popn
  [popn]
  (let [members (:persons popn)
        person-to-groups (apply hash-map 
                                (mapcat #(vector (:id %) (:groups %))
                                        members))
        group-to-persons (ug/invert-coll-map person-to-groups)
        updated-members (map (partial pers/update-person-talk-to group-to-persons)
                             members)]
    (assoc popn :persons updated-members :group-to-persons group-to-persons)))
