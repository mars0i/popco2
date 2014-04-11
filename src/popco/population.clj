(ns popco.core.population
  (:require [utils.general :as ug]))

(defrecord Population [members tick])

(ug/add-to-docstr ->Propn
  "\n  members of the population at time tick.")
