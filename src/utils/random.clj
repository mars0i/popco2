(ns utils.random
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]])
  (:import [ec.util MersenneTwister MersenneTwisterFast] ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE
           [SFMT19937])) ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE

(defn make-rand-idx
  [rng n]
  (* n (blahblah)))

;; note these are lazy

(defn make-sample-with-replacement-1
  [rand-idx num-samples coll]
  (repeatedly num-samples 
              #(nth coll (partial rand-idx (count coll))))) ; does repeatedly make this get reevaluated every time?


;; This version is inspired by Incanter, which does it like this:
;;        (map #(nth x %) (sample-uniform size :min 0 :max max-idx :integers true))
(defn make-sample-with-replacement-2
  [rand-idx num-samples coll]
  (map #(nth x %) 
       (repeatedly num-samples 
                   (partial rand-idx (count coll))))
