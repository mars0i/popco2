(ns popco.core.population
  (:require [utils.general :as ug]))


(defrecord Population [tick 
                       persons
                       group-to-persons
                       person-talk-to-groups])

(ug/add-to-docstr ->Population
  "\n  tick: timestep.
  persons: members of the population at time tick.
  group-to-persons: map from group id to person ids representing membership.
  person-to-groups: map from person id to group ids--i.e. what groups am I in?
  person-talk-to-groups: map from person id to groups of persons to whom person talks.")

;(defn make-population
;  "ADD DOCSTRING"
;  [persons group-to-persons person-talk-to-groups]
;  (->Population 0 
;                persons 
;                group-to-persons 
;                person-talk-to-groups))
