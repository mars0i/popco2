(ns popco.nn.propn
  (:use popco.core.lot)
  (:require [popco.nn.core :as nn]
            [utils.general :as ug]
            [clojure.core.matrix :as mx])
  (:import [popco.core.lot Propn]
           [popco.nn.core PropnNet])
  (:gen-class))
