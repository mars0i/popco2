(ns utils.random
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]])
  (:import [ec.util MersenneTwister MersenneTwisterFast] ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE
           [SFMT19937])) ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE

