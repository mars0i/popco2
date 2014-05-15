(ns popco.core.population
  (:require [utils.general :as ug]))

(defrecord Population [tick persons])

(ug/add-to-docstr ->Population
  "\n  Fields tick, persons: members of the population at time tick.")
