(ns test.rngs
  (:require [criterium.core :as crit]
            [criterium.well :as well]
            [clojure.data.generators :as gen]
            [utils.probability :as prob]))

(def mt (prob/make-mersenne-twister 1203413))
(def mtf (prob/make-mersenne-twister-fast 1203413))
(def sfmt (prob/make-sfmt 1203413))

(def noopi (iterate #(do % 1) 0))
(def mti (iterate #(do % (prob/mersenne-next-int mt)) 0))
(def mtfi (iterate #(do % (prob/mersenne-next-int mtf)) 0))
(def sfmti (iterate #(do % (prob/sfmt-next-int sfmt)) 0))
(def welli (well/well-rng-1024a))
(def ri (iterate #(do % (rand-int Integer/MAX_VALUE)) 0))
(def dgri (iterate #(do % (gen/long)) 0))


(def n 1000000)

(println "\nnoop, just iterate:")
(crit/bench (println (nth noopi n)))
(println "\nmersenne-twister:")
(crit/bench (println (nth mti n)))
(println "\nmersenne-twister-fast:")
(crit/bench (println (nth mtfi n)))
(println "\nsfmt19937:")
(crit/bench (println (nth sfmti n)))
(println "\nwell:")
(crit/bench (println (nth welli n)))
(println "\nclojure rand-int:")
(crit/bench (println (nth ri n)))
(println "\ndata.generators long:")
(crit/bench (println (nth dgri n)))
