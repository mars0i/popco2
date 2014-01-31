(ns tst
  (:use criterium.core
        [clojure.core.matrix :as mx]))

;;; Benchmarking setup

(def howmany 1000)
;(def a-coll (range howmany))
;(def a-coll (into () (range howmany)))
(def a-coll (vec (range howmany)))
(def maskvec (zero-vector :vectorz howmany))

(defn unmaskit!
  [idx]
  (mx/mset! maskvec idx 1.0)) ; sets element idx of maskvec to 1.0

(defn runbench
  [domapfn label]
  (print (str "\n" label ":\n"))
  (bench (def _ (domapfn unmaskit! a-coll))))


;;; domap versions

;; Note the name domap was suggested by Jozef Wagner in the Clojure Google group: https://groups.google.com/forum/#!topic/clojure/bn5QmxQF7vI

(defn domap1
  [f coll]
  (doseq [e coll] 
    (f e)))

(defn domap2
  [f coll]
    (dotimes [i (count coll)]
      (f (nth coll i))))

(defn domap3
  [f & colls]
  (dotimes [i (apply min (map count colls))]
    (apply f (map #(nth % i) colls))))

(defmacro domap4
  [f & colls]
  `(dotimes [i# (apply min (map count '~colls))]
    (apply ~f (map #(nth % i#) '~colls))))

;; by Ankur in SO:
(defn domap5
  [f & colls]
  (doall (for [coll colls
               x coll]
           (f x))))

;; with dorun vs doall
(defn domap6
  [f & colls]
  (dorun (for [coll colls
               x coll]
           (f x))))

;; version of domap implemented with map
(defn domap7
  [f coll]
  (dorun (map f coll)))

;; see domap18 below for alternate version:
(defn domap8
  [f & colls]
  (dorun (apply (partial map f) colls)))

;; might be broken
(defmacro domap9
  [f & colls]
  `(dorun (map ~f ~@colls)))

;; see domap17 below for function version
(defmacro domap10
  [f & colls]
  (let [params (vec (repeatedly (count colls) gensym))] ; one param name for each seq arg
    `(let [argvecs# (map vector ~@colls)]               ; seq of vecs of interleaved elements
       (doseq [~params argvecs#]
         (~f ~@params)))))

(defmacro domap11
  [f & colls]
  (let [i (gensym "i")]
    `(dotimes [~i ~(apply min (map count colls))]
       (~f ~@(map #(list 'nth % i) colls)))))

(defmacro domap12
  [f & colls]
  (let [i (gensym "i")]
    `(dotimes [~i (min ~@(map count colls))]
       (~f ~@(map #(list 'nth % i) colls)))))

(defn domap13
  [f coll]
  (let [v (vec coll)]
    (dotimes [i (count v)]
      (f (get v i)))))

(defn domap14
  [f & colls]
  (let [vecs (map vec colls)]
    (dotimes [i (apply min (map count vecs))]
      (apply f (map #(get % i) vecs)))))

;; cf. domap23
(defn domap15
  [f coll] 
  (when (seq coll)
    (f (first coll))
    (recur f (rest coll))))

(defn domap15a
  [f coll] 
  (loop [c coll]
    (when (seq c)
      (f (first c))
      (recur (rest c)))))

;; Hacked version of map from clojure/core.clj.
;; Turns out this is slow.  Maybe because I deleted the chunking code
;; that I didn't understand, which may or may not depend on lazy output.
(defn domap16
  ([f coll]
    (when-let [s (seq coll)]
      (f (first coll))
      (recur f (rest coll))))
  ([f c1 c2]
   (let [s1 (seq c1) s2 (seq c2)]
     (when (and s1 s2)
       (f (first s1) (first s2))
       (recur f (rest s1) (rest s2)))))
  ([f c1 c2 c3]
   (let [s1 (seq c1) s2 (seq c2) s3 (seq c3)]
     (when (and s1 s2 s3)
       (f (first s1) (first s2) (first s3))
       (recur f (rest s1) (rest s2) (rest s3)))))
  ([f c1 c2 c3 & colls]
   (let [step (fn step [cs]
                  (let [ss (map seq cs)]
                    (when (every? identity ss)
                      (cons (map first ss) (step (map rest ss))))))]
     (domap16 #(apply f %) (step (conj colls c3 c2 c1))))))

;; function version of domap10
(defn domap17
  [f & colls]
  (let [argvecs (apply (partial map vector) colls)] ; seq of ntuples of interleaved vals
    (doseq [args argvecs]
      (apply f args))))

;; like domap8
(defn domap18
  [f & colls]
  (dorun (apply map f colls)))

;; from noisesmith on SO:
(defn domap19 
  [f & colls]
  (apply mapv f colls)
  nil)

;; same, but allowed to return the seq
(defn domap20
  [f & colls]
  (apply mapv f colls))

;; with dorun
(defn domap21
  [f & colls]
  (dorun (apply mapv f colls)))

;; I don't think adding nil after dorun should matter
(defn domap22
  [f & colls]
  (dorun (apply mapv f colls))
  nil)

;; also from noisesmith on SO
;; kind of a multi-seq version of domap15
(defn domap23
  [f & colls]
  (loop [heads colls]
    (when (every? seq heads)
      (apply f (map first heads))
      (recur (map rest heads)))))

(println "loaded. yow.")

(runbench domap1 "domap1: simple doseq") ; the standard
(runbench domap2 "domap2: dotimes")
(runbench domap7 "domap7: map + dorun")
(runbench domap19 "domap19: mapv, nil")
(runbench domap20 "domap20: mapv")
;(runbench domap21 "domap21: mapv, dorun")
;(runbench domap22 "domap22: mapv, dorun, nil")
(runbench domap15 "domap15: simple recur")
(runbench domap23 "domap23: multi recur")
