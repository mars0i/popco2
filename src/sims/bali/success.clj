;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.success
  (:require [sims.bali.collections :as bc]
            [clojure.core.matrix :as mx]
            [utils.random :as ran]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extra functions for Bali sims

;; Define function that returns the overall influence of spiritual-peasant propositions.
;; This will be a function of the mean of their activations.

;; Define start, end, len from continuous sequence of indexes of activations the spiritual-peasant propositions:
(let [idxs (sort 
             (map (:id-to-idx bc/no-perc-pnet)
                  bc/spiritual-peasant-propn-ids))] ; CHANGE THIS LINE to use a different range of propositions for success
  (def ^:const +idxs-len+ (count idxs))
  (def ^:const +idxs-start+ (first idxs))
  (def ^:const +idxs-end+ (inc (last idxs)))
  ;; sanity check:
  (when-not (= idxs (range +idxs-start+ +idxs-end+))
    (throw (Exception. "Indexes for these propositions are not sequential."))))

(defn sp-subvector-mean
  "Returns the mean of subvector of pers's activation vector.  The subvector
  is defined by constants in the source file."
  [pers]
  (let [activns (:activns (:propn-net pers))]
    (/ (mx/esum (mx/subvector activns +idxs-start+ +idxs-end+))
       +idxs-len+)))

(defn make-spiritual-peasant-ness
  "Return a function of the mean of a subvector of activations defined
  by sp-subvector-mean, scaled by scaling-fn."
  [scaling-fn pers]
  (scaling-fn (sp-subvector-mean pers)))

; Using identity as scaling-fn makes success a linear function of mean activation in the domain:
(def spiritual-peasant-ness (partial make-spiritual-peasant-ness identity))

;; When NetLogo is involved, these will be different, 
;; but for a popco-only system we close the loop.
;; See FakeNetLogo.md or files referenced there.
(def success spiritual-peasant-ness)

(defn random-quality
  "Returns a randomly chosen 'quality' value in [0.0,1.0) ."
  [pers]
  (ran/next-double (:rng pers)))
