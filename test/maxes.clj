(ns test.maxes
  (:use [criterium.core :only [bench benchmark]]
        [utils.general]))

(println "with recur:")
(bench (def _ (maxes #(:b %) [{:a 1 :b 2} {:a 4 :b 5} {:a 1 :b 5} {:a 4 :b 4} {:b 3 :a 5} {:c 7 :b 5} {:a 2 :b 3}])))

;(println "with reduce:")
;(bench (def _ (reduce-maxes #(:b %) [{:a 1 :b 2} {:a 4 :b 5} {:a 1 :b 5} {:a 4 :b 4} {:b 3 :a 5} {:c 7 :b 5} {:a 2 :b 3}])))

(println "lazy without realization:")
(bench (def _ (lazy-maxes #(:b %) [{:a 1 :b 2} {:a 4 :b 5} {:a 1 :b 5} {:a 4 :b 4} {:b 3 :a 5} {:c 7 :b 5} {:a 2 :b 3}])))

(println "lazy with realization:")
(bench (def _ (doall (lazy-maxes #(:b %) [{:a 1 :b 2} {:a 4 :b 5} {:a 1 :b 5} {:a 4 :b 4} {:b 3 :a 5} {:c 7 :b 5} {:a 2 :b 3}]))))
