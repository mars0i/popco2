(ns test.samplingtests2
  (:use [criterium.core :only [bench benchmark]]
        [popco.core.main]
        [utils.random]
        [sims.crime3.groups2]
        [popco.io.propncsv :only [spit-csv]]))

(def ticks 500)

(def test-results {})

(def rng-fns [make-rng-mt make-rng-mtf make-rng-java])
(def rng-names (map utils.general/extract-fn-name rng-fns))
(def sample-with-repl-fns [sample-with-repl-1 sample-with-repl-2 sample-with-repl-3 sample-with-repl-4])
(def sample-with-repl-names (map utils.general/extract-fn-name sample-with-repl-fns))
(def many-times-fns [many-times unparalleled-many-times])
(def many-times-names (map utils.general/extract-fn-name many-times-fns))

(doseq [rng-fn rng-fns
        sample-with-repl-fn sample-with-repl-fns
        many-times-fn many-times-fns]
  ;; set up rng's and functions etc. for this run. intern allows redefining in another namespace:
  (intern 'utils.random 'make-rng rng-fn)
  (intern 'utils.random 'sample-with-repl sample-with-repl-fn)
  (intern 'popco.core.person 'initial-rng (utils.random/make-rng 1400811420068))
  ;; cause the population to be remade using the current rng and sample fns:
  (load-file "src/sims/crime3/groups2.clj")
  ;; generate strings to function as keys in a hash, labels in a spreadsheet:
  (let [rng-name (utils.general/extract-fn-name rng-fn)
        sample-with-repl-name (utils.general/extract-fn-name sample-with-repl-fn)
        many-times-name (utils.general/extract-fn-name many-times-fn)]
    ;; tell user how far along we are, using actual function internal names for extra sanity check:
    ;(println "\nTesting with")
    ;(println rng-fn)
    ;(println sample-with-repl-fn)
    ;(println many-times-fn)
    (print rng-name sample-with-repl-name many-times-name ": Mean seconds per call: ")(flush)
    ;; run test:
    (let [result (first (:mean (benchmark (def _ (nth (many-times-fn popn) ticks)) '())))] ; produces mean seconds per call
      ;; let user know what happened:
      ;(println "Mean time for" rng-name sample-with-repl-name many-times-name ":" result)(flush)
      (println result)(flush)
      ;; store the new result into the results hash:
      (def test-results 
        (assoc-in test-results 
                  [rng-name sample-with-repl-name many-times-name]
                  result)))))

;; TODO not right should be a 2-level sequence, not 3
;(spit-csv "samplingtests2.csv"
;          (for [rng-key (keys test-results) :let [sample-fn-map (test-results rng-key)]]
;            (for [sample-fn-key (keys sample-fn-map) :let [many-times-fn-map (sample-fn-map sample-fn-key)]]
;              [rng-key sample-fn-key (many-times-fn-map "many-times") (many-times-fn-map "unparalleled-many-times")])))

(println "\nResults: ")
(clojure.pprint/pprint test-results)
(flush)

(def test-results-seq
  (for [rng-name rng-names
        sample-with-repl-name sample-with-repl-names]
    (let [many-times-fn-map (get-in test-results [rng-name sample-with-repl-name])
          many-times-result (many-times-fn-map "many-times")
          unparalleled-many-times-result (many-times-fn-map "unparalleled-many-times")]
      [rng-name sample-with-repl-name many-times-result unparalleled-many-times-result]))) ; last two elements are mean seconds per call with pmap and without pmap

(spit-csv "samplingtests2.csv" test-results-seq)

(clojure.pprint/pprint test-results-seq)
(flush)
