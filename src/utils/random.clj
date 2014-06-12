(ns utils.random
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]])
  (:import [ec.util MersenneTwister MersenneTwisterFast] ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE
           [SFMT19937])) ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE

;; Uses clojure.core's `rand-int` method of truncation to an int with `int`
;; rather than data.generator's `uniform` method of truncation using `Math/floor`
;; followed by clojure.core's `long`.  (Why call `Math/floor` before `long`?
;; Maybe experiment with adding this before the call to `int`.)
(defn make-rand-idx-from-next-double
  "This is essentially the same as Clojure's `rand-int` with an RNG argument.
  Works with any RNG that supports `.nextDouble`, including java.util.Random,
  and Sean Luke's MersenneTwister and MersenneTwisterFast.  Returns an int in
  [0,n).  Use `partial` to create a workalike for rand-int, maybe named 'rand-idx'."
  [rng n]
  (int (* n (.nextDouble rng))))

(def ^:const +int-range-double+ (double (- Integer/MAX_VALUE Integer/MIN_VALUE)))

;; ????
;; question: Am I producing ints from 0 to count-1 with this method?  Or is there
;; some kind of off by 1 type of issue?
(defn make-rand-idx-from-sfmt19937-next
  "This is essentially the same as Clojure's `rand-int` with an RNG argument.
  Works with any RNG that supports a method `.next` that returns an int, in
  particular SFMT19937.  Use `partial` to create a workalike for rand-int, which
  could be named 'rand-idx'."
  [rng n]
  (int (* n                            ; When we're all done, go back to an int.
          (- (/ (.next rng) +int-range-double+) ; SFMT19937 *only* produces ints; make it into a double.
             Integer/MIN_VALUE))))     ; then make it non-negative
                                       ; Is this a good strategy??


;; Note these are lazy:

;; is "make-" correct?
;; This version repeatedly calls nth coll with a new random index each time.
(defn sample-with-repl-1
  [rand-idx num-samples coll]
  (repeatedly num-samples 
              #(nth coll (rand-idx (count coll))))) ; does repeatedly make this get reevaluated every time?

;; is "make-" correct?
;; This version is inspired by Incanter, which does it like this:
;;        (map #(nth x %) (sample-uniform size :min 0 :max max-idx :integers true))
;; You get a series of random ints between 0 and the coll size,
;; and then map nth coll through them.
(defn sample-with-repl-2
  [rand-idx num-samples coll]
  (map #(nth coll %) 
       (repeatedly num-samples 
                   #(rand-idx (count coll)))))

;; lazy if more than one sample
(defn sample-without-repl
  "Derived from Incanter's algorithm from sample-uniform for sampling without replacement."
  [rand-idx num-samples coll]
  (let [size (count coll)
        max-idx size]
    (cond
      (= num-samples 1) (list (nth coll (rand-idx size)))  ; if only one element needed, don't bother with the "with replacement" algorithm
      ;; Rather than creating subseqs of the original coll, we create a seq of indices below,
      ;; and then [in effect] map (partial nth coll) through the indices to get the samples that correspond to them.
      (< num-samples size) (map #(nth coll %) 
                                (loop [samp-indices [] indices-set #{}]    ; loop to create the set of indices
                                  (if (= (count samp-indices) num-samples) ; until we've collected the right number of indices
                                    samp-indices
                                    (let [i (rand-idx size)]             ; get a random index
                                      (if (contains? indices-set i)      ; if we've already seen that index,
                                        (recur samp-indices indices-set) ;  then try again
                                        (recur (conj samp-indices i) (conj indices-set i))))))) ; otherwise add it to our indices
      :else (throw (Exception. "num-samples can't be larger than (count coll).")))))

;; example:
(def sample-with-repl sample-with-repl-1)
(def my-rand-idx (partial make-rand-idx-from-next-double (MersenneTwisterFast. 325117)))
;(sample-with-repl my-rand-idx 5 (range 25))
