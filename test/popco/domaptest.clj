(ns tst
  (:require [popco.core.communic :as pc])
  (:use criterium.core
        clojure.core.matrix
        utils.general))

(def howmany 1000)
(def nums (vec (range howmany)))
(def veczvec (zero-vector howmany))

(defn unmaskit!
  [idx]
  (pc/unmask! veczvec idx))

;; Simple Clojure version of mapc. Allows only one list arg.
(defn domapseq
  [f coll]
  (doseq [e coll] 
    (f e)))

;; Clojure mapc with multiple list args
;; Is this any more efficient than Clojure map ?
;; After all, it constructs a new seq combining the arg seqs
;; How is this better than constructing a seq of results?
;; It does enforce that only side effects will matter--only nil is returned.
(defmacro domapseqmac
  [f & colls]
  (let [params (vec (repeatedly (count colls) gensym))] ; one param name for each seq arg
    `(let [argvecs# (map vector ~@colls)]               ; seq of vecs of interleaved elements
       (doseq [~params argvecs#]
         (~f ~@params)))))

(defn domaptimes
  [f coll]
    (dotimes [i (count coll)]
      (f (nth coll i))))

(defmacro domaptimesmac
  [f & colls]
  (let [i (gensym "i")]
    `(dotimes [~i (min ~@(map count colls))]
       (~f ~@(map #(list 'nth % i) colls)))))

(defn domaptimesv
  [f coll]
  (let [v (vec coll)]
    (dotimes [i (count v)]
      (f (get v i)))))

(defn domaptimesvs
  [f & colls]
  (let [vecs (map vec colls)]
    (dotimes [i (apply min (map count vecs))]
      (apply f (map #(get % i) vecs)))))

(defn domaprecur
  [f coll] 
  (when (seq coll)
    (f (first coll))
    (recur f (rest coll))))

;; version of domap implemented with map
(defmacro domaprun
  [f & colls]
  `(dorun (map ~f ~@colls)))

;; Hacked version of map from clojure/core.clj:
(defn domapclojmap
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
     (domapclojmap #(apply f %) (step (conj colls c3 c2 c1))))))

(println "loaded.")

(print "\ndomapseq:\n") (bench (def _ (domapseq unmaskit! nums)))
(print "\ndomaptimes:\n") (bench (def _ (domaptimes unmaskit! nums)))
(print "\ndomaptimesmac:\n") (bench (def _ (domaptimesmac unmaskit! nums)))
(print "\ndomaptimesv:\n") (bench (def _ (domaptimesv unmaskit! nums)))
(print "\ndomaprecur:\n") (bench (def _ (domaprecur unmaskit! nums)))
(print "\ndomaptimesvs:\n") (bench (def _ (domaptimesvs unmaskit! nums)))
(print "\ndomapclojmap:\n") (bench (def _ (domapclojmap unmaskit! nums)))
(print "\ndomapseqmac:\n") (bench (def _ (domapseqmac unmaskit! nums)))
(print "\ndomaprun:\n") (bench (def _ (domaprun unmaskit! nums)))
