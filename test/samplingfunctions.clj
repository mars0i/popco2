(ns test.sampling
  (:use [criterium.core :only [bench]]
        [utils.probability]))

;(def stuff (vec (range 25)))
;(def stuffs (repeat 100 stuff))

(defn test-sampling-fns
  [sample-size stuff]
  (print "\n==================================================\nSample size ="
         sample-size ", stuff size =" (count stuff) "\n")
  (let [stuffs (repeat 100 stuff)]
    (doseq [choose-fn [choose-incanter choose-generators choose-bigml]
            sample-fn [sample-with-repl sample-without-repl]
            map-fn [map pmap]]
      (choose-fn)
      (println "\n--------------------------------------------")
      (println choose-fn)
      (println sample-fn)
      (println map-fn)
      (print "\n")
      (bench (def _ (doall (map-fn #(sample-fn sample-size %) stuffs)))))))

(test-sampling-fns 5 (vec (range 25)))
(test-sampling-fns 25 (vec (range 50)))
(test-sampling-fns 1 (vec (range 10)))
