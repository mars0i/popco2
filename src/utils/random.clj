;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns utils.random
  ;(:require ;[clojure.data.generators :as gen]
            ;[incanter.stats :as incant]
            ;[bigml.sampling [simple :as simple]])
  (:import [ec.util MersenneTwister MersenneTwisterFast]
           ;[SFMT19937]
	   ;[java.util Random]
           ))

(defn make-long-seed
  [] 
  (- (System/currentTimeMillis)
     (rand-int Integer/MAX_VALUE)))

;(defn make-rng-mt
;  ([] (make-rng-mt (make-long-seed)))
;  ([long-seed] (MersenneTwister. long-seed)))

(defn make-rng-mtf
  ([] (make-rng-mtf (make-long-seed)))
  ([long-seed] (MersenneTwisterFast. long-seed)))

;(defn make-rng-sfmt
;  ([] (make-rng-sfmt (make-long-seed)))
;  ([long-seed] (SFMT19937. long-seed)))

;(defn make-rng-java
;  ([] (make-rng-java (make-long-seed)))
;  ([long-seed] (Random. long-seed)))

(def make-rng make-rng-mtf)

(defn make-rng-print-seed
  "Make a seed, print it to stdout, then pass it to make-rng."
  []
  (let [seed (make-long-seed)]
    (println seed)
    (make-rng seed)))

(defn rand-idx [rng n] (.nextInt rng n))
;(defmulti  rand-idx (fn [rng n] (class rng)))
;(defmethod rand-idx ec.util.MersenneTwister     [rng n] (.nextInt rng n))
;(defmethod rand-idx ec.util.MersenneTwisterFast [rng n] (.nextInt rng n))
;(defmethod rand-idx java.util.Random            [rng n] (.nextInt rng n))

(defn next-long [rng] (.nextLong rng))
;(defmulti  next-long class)
;(defmethod next-long ec.util.MersenneTwister     [rng] (.nextLong rng))
;(defmethod next-long ec.util.MersenneTwisterFast [rng] (.nextLong rng))
;(defmethod next-long java.util.Random            [rng] (.nextLong rng))

;; lazy
;; This version repeatedly calls nth coll with a new random index each time.
;(defn sample-with-repl-1
;  [rng num-samples coll]
;  (let [size (count coll)]
;    (repeatedly num-samples 
;                #(nth coll (rand-idx rng size)))))

;; lazy
;; This version is inspired by Incanter, which does it like this:
;;        (map #(nth x %) (sample-uniform size :min 0 :max max-idx :integers true))
;; You get a series of random ints between 0 and the coll size,
;; and then map nth coll through them.
;(defn sample-with-repl-2
;  [rng num-samples coll]
;  (let [size (count coll)]
;    (map #(nth coll %) 
;         (repeatedly num-samples #(rand-idx rng size)))))

;; lazy
(defn sample-with-repl-3
  [rng num-samples coll]
  (let [size (count coll)]
    (for [_ (range num-samples)]
      (nth coll (rand-idx rng size)))))

;; not lazy
;(defn sample-with-repl-4
;  [rng num-samples coll]
;  (let [size (count coll)]
;    (loop [remaining num-samples result []] 
;      (if (> remaining 0)
;        (recur (dec remaining) (conj result 
;                                     (nth coll (rand-idx rng size))))
;        result))))

(def sample-with-repl sample-with-repl-3) ; see samplingtests2.xlsx


;; lazy if more than one sample
;; (deal with license issues)
(defn sample-without-repl
  "Derived from Incanter's algorithm from sample-uniform for sampling without replacement."
  [rng num-samples coll]
  (let [size (count coll)
        max-idx size]
    (cond
      (= num-samples 1) (list (nth coll (rand-idx rng size)))  ; if only one element needed, don't bother with the "with replacement" algorithm
      ;; Rather than creating subseqs of the original coll, we create a seq of indices below,
      ;; and then [in effect] map (partial nth coll) through the indices to get the samples that correspond to them.
      (< num-samples size) (map #(nth coll %) 
                                (loop [samp-indices [] indices-set #{}]    ; loop to create the set of indices
                                  (if (= (count samp-indices) num-samples) ; until we've collected the right number of indices
                                    samp-indices
                                    (let [i (rand-idx rng size)]             ; get a random index
                                      (if (contains? indices-set i)      ; if we've already seen that index,
                                        (recur samp-indices indices-set) ;  then try again
                                        (recur (conj samp-indices i) (conj indices-set i))))))) ; otherwise add it to our indices
      :else (throw (Exception. "num-samples can't be larger than (count coll).")))))
