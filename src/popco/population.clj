(ns popco.core.population
  (:require [utils.general :as ug]))

(defrecord Population [tick members])

(ug/add-to-docstr ->Propn
  "\n  Fields tick, members: members of the population at time tick.")
