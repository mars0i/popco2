(ns utils.probability
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]])
  (:import [ec.util MersenneTwister MersenneTwisterFast] ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE
           [SFMT19937])) ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE

;; INCANTER

(defn make-mersenne-twister
  [seed]
  (MersenneTwister. seed))

(defn make-mersenne-twister-fast
  [seed]
  (MersenneTwisterFast. seed))

(defn mersenne-next-int
  [rng]
  (.nextInt rng))

(defn make-sfmt
  [seed]
  (SFMT19937. seed))

(defn smft-next-int
  [rng]
  (.next rng))

;; Wrapper around incanter.stats/sample
(defn incanter-sample
  "Just like incanter.stats/sample (q.v.), but if :size is 1, returns
  a collection rather than the sampled element."
  [& args]
  (let [res (apply incant/sample args)]
    (if (coll? res)
      (doall res)
      (list res))))

(defn incanter-sample-with-repl
  [num-samples coll]
  (incanter-sample coll :size num-samples :replacement true))

(defn incanter-sample-without-repl
  [num-samples coll]
  (incanter-sample coll :size num-samples :replacement false))


;; DATA.GENERATORS

(defn generators-sample-with-repl
  [num-samples coll]
  (doall (repeatedly num-samples #(gen/rand-nth coll))))

(defn generators-reservoir-sample-without-repl 
  [num-samples coll]
  (doall (gen/reservoir-sample num-samples coll)))

(defn hybrid-generators-incanter-sample-without-repl
  "Uses Incanter's algorithm for sampling without replacement, but with 
  data.generator's random number generator."
  [num-samples coll]
  ;; rest of this is copied from Incanter's stats.clj and edited (with addl comments).
  ;; Main change was replacing Incanter's sample-uniform with data.generator's uniform.
  (let [max-idx (dec (count coll))]
    (if (= num-samples 1)  ; if only one element needed, don't bother with the "with replacement" algorithm
      (list (nth coll (rand-int (inc max-idx)))) ; note this uses built-in Clojure rand-int (why that here, but uniform below?)
      (if (> num-samples (count coll))    ; sanity check on arguments passed
        (throw (Exception. "'num-samples' can't be larger than (count coll) without replacement!"))
        ;; Rather than creating subseqs of the original coll, we create a seq of indices below,
        ;; and then [in effect] map (partial nth coll) through the indices to get the samples that correspond to them.
        (doall 
          (map #(nth coll %) 
               ;; create the set of indices:
               (loop [samp-indices [] indices-set #{}]
                 (if (= (count samp-indices) num-samples) ; loop until we've collected the right number of indices
                   samp-indices
                   (let [i (gen/uniform 0 max-idx)]     ; get a random index using data.generator's RNG (was Incanter's sample-uniform)
                     (if (contains? indices-set i)      ; if we've already seen that index,
                       (recur samp-indices indices-set) ;  then try again
                       (recur (conj samp-indices i) (conj indices-set i)))))))))))) ; otherwise add it to our indices

(defn mtf-generators-sample-with-repl
  [num-samples coll]
  (binding [gen/*rnd* (make-mersenne-twister-fast 1001)] ; FIXME need modifiable seed
    (generators-sample-with-repl num-samples coll)))

(defn mtf-generators-reservoir-sample-without-repl
  [num-samples coll]
  (binding [gen/*rnd* (make-mersenne-twister-fast 1001)] ; FIXME need modifiable seed
    (generators-reservoir-sample-without-repl num-samples coll)))

(defn mtf-hybrid-generators-incanter-sample-without-repl
  [num-samples coll]
  (binding [gen/*rnd* (make-mersenne-twister-fast 1001)] ; FIXME need modifiable seed
    (hybrid-generators-incanter-sample-without-repl num-samples coll)))

;; BIGML/SAMPLING

(defn bigml-sample-with-repl
  [num-samples coll]
  (doall (take num-samples (simple/sample coll :replace true))))

(defn bigml-sample-without-repl
  [num-samples coll]
  (doall (take num-samples (simple/sample coll :replace false))))

(defn bigml-sample-twister-with-repl
  [num-samples coll]
  (doall (take num-samples (simple/sample coll :replace true :generator :twister))))

(defn bigml-sample-twister-without-repl
  [num-samples coll]
  (doall (take num-samples (simple/sample coll :replace false :generator :twister))))


;; CHOOSE YOUR VERSION

;; Specify Incanter versions
(defn choose-incanter []
  (def sample-without-repl incanter-sample-without-repl)
  (def sample-with-repl incanter-sample-with-repl))

;; Specify data.generators versions:
;(defn choose-generators []
;  (def sample-without-repl generators-sample-without-repl)
;  (def sample-with-repl generators-sample-with-repl))

;; Specify bigml/sampling versions:
(defn choose-bigml []
  (def sample-without-repl bigml-sample-without-repl)
  (def sample-with-repl bigml-sample-with-repl))

(choose-bigml)
;(choose-generators)
;(choose-incanter)
