(ns test.samplingtests2
  (:use [criterium.core :only [bench benchmark]]
        [popco.core.main]
        [utils.random]
        [sims.crime3.groups2]))

(def ticks 2)

(def test-results {})

(doseq [rng-fn [make-rng-mt make-rng-mtf make-rng-java]
        sample-repl-fn [sample-with-repl-1 sample-with-repl-2 sample-with-repl-3 sample-with-repl-4]
        many-times-fn [many-times unparalleled-many-times]]
        ;; set up rng's and functions etc. for this run. intern allows redefining in another namespace:
        (intern 'utils.random 'make-rng rng-fn)
        (intern 'utils.random 'sample-with-repl sample-repl-fn)
        (intern 'popco.core.person 'initial-rng (utils.random/make-rng 1400811420068))
        ;; cause the population to be remade using the current rng and sample fns:
        (load-file "src/sims/crime3/groups2.clj")
        ;; generate strings to function as keys in a hash, labels in a spreadsheet:
        (let [rng-name (utils.general/extract-fn-name rng-fn)
              sample-repl-name (utils.general/extract-fn-name sample-repl-fn)
              many-times-name (utils.general/extract-fn-name many-times-fn)]
          ;; tell user how far along we are, using actual function internal names for extra sanity check:
          (print "Testing" rng-fn "in" sample-repl-fn "with" many-times-fn ":")
          ;; run test and store the new result into the results hash:
          (def test-results 
            (assoc-in test-results 
                      [rng-name sample-repl-name many-times-name]
                      (first (:mean (benchmark 
                                      (def _ (nth (many-times-fn popn) ticks))
                                      '())))))
          (println (get-in test-results [rng-name sample-repl-name many-times-name])))) ; let user know latest mean time

        (println "Results: ")
        (pprint test-results)
