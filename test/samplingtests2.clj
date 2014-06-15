(ns test.samplingtests2
  (:use [criterium.core :only [bench benchmark]]
        [popco.core.main]
        [utils.random]))

(def ticks 5)

(doseq [rng-fn [make-rng-mt make-rng-mtf make-rng-java]
        sample-fn [sample-with-repl-1]; sample-with-repl-2 sample-with-repl-3 sample-with-repl-4]
        many-times-fn [many-times unparalleled-many-times]]

  ;; set up rng's etc. for this run
  (intern 'utils.random 'make-rng rng-fn)
  (intern 'utils.random 'sample-with-repl sample-fn)
  (intern 'popco.core.person 'initial-rng (utils.random/make-rng 1400811420068))

  ;; cause the population to be made using the rng and sample fns
  ;(load-file "src/sims/crime3/groups2.clj")
  (unlocknload 'src.sims.crime3.groups2)

  ;; generate strings to function as keys in a hash, and then run the test
  (let [rng-name (utils.general/extract-fn-name rng-fn)
        sample-repl-name (utils.general/extract-fn-name sample-fn)
        many-times-name (utils.general/extract-fn-name many-times-fn)
        result (first
                 (:mean
                   (benchmark 
                     (def _ (nth (many-times sims.crime3.groups2/popn) ticks))
                     '())))]

    ;; store the new result into the hash
    (def test-results 
      (assoc-in test-results 
                [rng-name sample-repl-name many-times-name]
                result))))

(pprint test-results)
