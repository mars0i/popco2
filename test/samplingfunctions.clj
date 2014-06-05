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
    (doseq [sample-fn [incanter-sample-with-repl incanter-sample-without-repl
                       generators-sample-with-repl generators-sample-without-repl
                       bigml-sample-with-repl bigml-sample-without-repl]
            map-fn [map pmap]]
      (println "\n--------------------------------------------")
      (println sample-fn)
      (println map-fn)
      (print "\n")
      (bench (def _ (doall (map-fn #(sample-fn sample-size %) stuffs)))))))

(test-sampling-fns 5 (vec (range 25)))
(test-sampling-fns 1 (vec (range 25)))
(test-sampling-fns 25 (vec (range 50)))
