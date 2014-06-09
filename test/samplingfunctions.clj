(ns test.sampling
  (:use [criterium.core :only [bench benchmark]]
        [utils.probability]))

;(def stuff (vec (range 25)))
;(def stuffs (repeat 100 stuff))

(defn test-sampling-fns
  [sample-size stuff]
  (print "\n==================================================\nSample size ="
         sample-size ", stuff size =" (count stuff) "\n")
  (let [stuffs (repeat 100 stuff)]
    (doseq [sample-fn [incanter-sample-with-repl incanter-sample-without-repl
                       generators-sample-with-repl generators-reservoir-sample-without-repl
                       hybrid-generators-incanter-sample-without-repl
                       mtf-generators-sample-with-repl mtf-generators-reservoir-sample-without-repl
                       mtf-hybrid-generators-incanter-sample-without-repl
                       bigml-sample-with-repl bigml-sample-without-repl
                       bigml-sample-twister-with-repl bigml-sample-twister-without-repl
                       ]
            map-fn [map pmap]]
      (print sample-fn)
      (println map-fn)
      (print "\n")
      (bench (def _ (doall (map-fn #(sample-fn sample-size %) stuffs)))))))

(defn map-pmap-test
  [sample-fn stuffs sample-size]
  (print sample-fn)
  (print "\t")
  (print (first (:mean (benchmark (def _ (doall (map #(sample-fn sample-size %) stuffs))) '()))))
  (print "\t")
  (print (first (:mean (benchmark (def _ (doall (pmap #(sample-fn sample-size %) stuffs))) '()))))
  (print "\n"))

(defn brief-test-sampling-fns
  "Tries to produce a csv file.  Output data are mean number of seconds per call."
  [sample-size stuff]
  (print "\nSample size =" sample-size ", stuff size =" (count stuff) "\n")
  (let [stuffs (repeat 100 stuff)]
    (doseq [sample-fn [incanter-sample-with-repl incanter-sample-without-repl
                       generators-sample-with-repl generators-reservoir-sample-without-repl
                       hybrid-generators-incanter-sample-without-repl
                       mtf-generators-sample-with-repl mtf-generators-reservoir-sample-without-repl
                       mtf-hybrid-generators-incanter-sample-without-repl
                       bigml-sample-with-repl bigml-sample-without-repl
                       bigml-sample-twister-with-repl bigml-sample-twister-without-repl]]
      (map-pmap-test sample-fn stuffs sample-size))))

;(test-sampling-fns 5 (vec (range 25)))
;(test-sampling-fns 1 (vec (range 25)))
;(test-sampling-fns 25 (vec (range 50)))

(brief-test-sampling-fns 5 (vec (range 25)))
(brief-test-sampling-fns 1 (vec (range 25)))
(brief-test-sampling-fns 25 (vec (range 50)))
