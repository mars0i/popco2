;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.functions
  (:require [sims.bali.collections :as bc]
            [clojure.core.matrix :as mx]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extra functions for Bali sims

(def idxs 
  (sort 
    (map (:id-to-idx bc/no-perc-pnet)
         bc/spiritual-peasant-propn-ids)))

(def idx-start (first idxs))
(def idx-end (inc (last idxs)))
(def idx-len (count idxs))

(when-not (= idxs (range start end))
  (throw (Exception. "Indexes for these propositions are not sequential.")))

(defn clip-to-prob
  [x]
  (max 0.0 (min 1.0 x)))

(defn worth-saying
  [pers abs-activn]
  (let [activns (:activns (:propn-net pers))
        dom-mean (/ (mx/esum 
                      (mx/subvector activns idx-start idx-end))
                    idx-len)
        prob (clip-to-prob (+ abs-activn dom-mean))] ; ???? TODO
    (< (ran/next-double (:rng pers)) 
       prob)))
