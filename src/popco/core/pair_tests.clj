(ns popco.core.pair-tests
  [:use criterium.core])

;; based on Arthur Ulfedt's answer:
;; http://stackoverflow.com/questions/:a9795604/good-clojure-representation-for-unordered-pairs/:a98004:a7?noredirect=:a#comment:b94397:a9_:a98004:a7

(defn pairlist-contains? [key pair] 
  (condp = key 
    (first pair) true 
    (second pair) true 
    false))

(defn pairvec-contains? [key pair] 
  (condp = key 
    (pair 0) true 
    (pair 1) true
    false))

(def ntimes 10000000)

(defn test-list []
  (let [data '(:a :b)] 
    (print "=============================\ntest-list:\n")
    (bench
      (dotimes [_ ntimes]
        (def _ (pairlist-contains? (rand-nth [:a :b]) data))))))

(defn test-vec []
  (let [data [:a :b]] 
    (print "=============================\ntest-vec:\n")
    (bench
      (dotimes [_ ntimes]
        (def _ (pairvec-contains? (rand-nth [:a :b]) data))))))

(defn test-hashset []
  (let [data (hash-set :a :b)]
    (print "=============================\ntest-hashset:\n")
    (bench
      (dotimes [_ ntimes]
        (def _ (contains? data (rand-nth [:a :b])))))))

(defn test-sortedset []
  (let [data (sorted-set :a :b)]
    (print "=============================\ntest-sortedset:\n")
    (bench
      (dotimes [_ ntimes]
        (def _ (contains? data (rand-nth [:a :b])))))))

(defn test-hashmap []
  (let [data (hash-map :a :a :b :b)]
    (print "=============================\ntest-hashmap:\n")
    (bench
      (dotimes [_ ntimes]
        (def _ (contains? data (rand-nth [:a :b])))))))

(defn test-arraymap []
  (let [data (array-map :a :a :b :b)]
    (print "=============================\ntest-arraymap:\n")
    (bench
      (dotimes [_ ntimes]
        (def _ (contains? data (rand-nth [:a :b])))))))

(defn test-all []
  (test-list)
  (test-vec)
  (test-hashset)
  (test-sortedset)
  (test-hashmap)
  (test-arraymap))
