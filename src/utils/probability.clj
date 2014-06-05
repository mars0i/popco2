(ns utils.probability
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]]))

;; INCANTER

;; Wrapper around incanter.stats/sample
(defn incanter-sample
  "Just like incanter.stats/sample (q.v.), but if :size is 1, returns
  a collection rather than the sampled element."
  [& args]
  (let [res (apply incant/sample args)]
    (if (coll? res)
      res
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
  (repeatedly num-samples #(gen/rand-nth coll)))

(def generators-sample-without-repl gen/reservoir-sample)

;(defn alt-sample-without-repl
;  [size x]
;  ;; rest of this is copied from Incanter's stats.clj
;  (if (> size (count x))
;    (throw (Exception. "'size' can't be larger than (count x) without replacement!"))
;    (map #(nth x %)
;         (loop [samp-indices [] indices-set #{}]
;           (if (= (count samp-indices) size)
;             samp-indices
;             (let [i (first (rand-int max-idx))]
;               (if (contains? indices-set i)
;                 (recur samp-indices indices-set)
;                 (recur (conj samp-indices i) (conj indices-set i)))))))))


;; BIGML/SAMPLING

(defn bigml-sample-with-repl
  [num-samples coll]
  (take num-samples (simple/sample coll :replace true)))

(defn bigml-sample-without-repl
  [num-samples coll]
  (take num-samples (simple/sample coll :replace false)))


;; CHOOSE YOUR VERSION

;; Specify Incanter versions
(defn choose-incanter []
  (def sample-without-repl incanter-sample-without-repl)
  (def sample-with-repl incanter-sample-with-repl))

;; Specify data.generators versions:
(defn choose-generators []
  (def sample-without-repl generators-sample-without-repl)
  (def sample-with-repl generators-sample-with-repl))

;; Specify bigml/sampling versions:
(defn choose-bigml []
  (def sample-without-repl bigml-sample-without-repl)
  (def sample-with-repl bigml-sample-with-repl))

;(choose-bigml)
(choose-generators)
;(choose-incanter)
