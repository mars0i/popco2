(ns utils.probability
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]]))

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
  (incanter-sample coll 
                   :size num-samples 
                   :replacement true))

(defn incanter-sample-without-repl
  [num-samples coll]
  (incanter-sample coll 
                   :size num-samples 
                   :replacement false))

(def sample-without-repl incanter-sample-without-repl)
(def sample-with-repl incanter-sample-with-repl)
