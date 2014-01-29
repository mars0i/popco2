(ns utils.general ; Utility functions handy for any Clojure program
  (:require [clojure.pprint :only [*print-right-margin*]]))

;; Simple Clojure version of mapc. Allows only one list arg.
(defn domap [f coll]
  [f coll]
  (doseq [e coll] 
    (f e)))

;; Clojure mapc with multiple list args
;; Is this any more efficient than Clojure map ?
;; After all, it constructs a new seq combining the arg seqs
;; How is this better than constructing a seq of results?
;; It does enforce that only side effects will matter--only nil is returned.
(defmacro domapmany
  [f & colls]
  (let [params (vec (repeatedly (count colls) gensym))] ; one param name for each seq arg
    `(let [argvecs# (map vector ~@colls)]               ; seq of vecs of interleaved elements
       (doseq [~params argvecs#]
         (~f ~@params)))))

(defn domapidx
  [f & colls]
  (let [vecs (map vec colls)]
    (dotimes [i (apply max (map count vecs))]
      (apply f (map #(get % i) vecs)))))

;(defmacro domapidxmac
;  [f & colls]
;  `(let [vecs# (map vec ~colls)]
;     (dotimes [i# (max ~@(map count vecs#))]
;       (apply f (map #(get % i#) vecs#)))))

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
(defn domapcloj
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
     (domapcloj #(apply f %) (step (conj colls c3 c2 c1))))))


(defn unlocknload 
  "Given a symbol representing a namespace, converts the symbol
  into the corresponding path + clojure source fileanem, tries to 
  load the file, and then uses (\"unlocks\") the namespace."
  [nssym]
  (load-file 
    (str "src/" 
         (clojure.string/replace (clojure.string/replace nssym \. \/) \- \_) 
         ".clj"))
    (use nssym))

(defn set-pprint-width 
  "Sets width for pretty-printing with pprint and pp."
  [cols] 
  (alter-var-root 
    #'clojure.pprint/*print-right-margin* 
    (constantly cols)))

(defmacro add-to-docstr
  "Appends string addlstr onto end of existing docstring for symbol sym.
  (Tip: Consider beginning addlstr with \"\\n  \".)"
  [sym addlstr] 
  `(alter-meta! #'~sym update-in [:doc] str ~addlstr))

(defn println-and-ret
  "Print a single argument with println, then return that argument.
  Useful for debugging."
  [arg]
  (println arg)
  arg)

(defn partition-sort-by
  "Return a function that will sort a collection by keyfn and then 
  partition by the same function."
  [keyfn coll]
  (partition-by keyfn (sort-by keyfn coll)))

(defn seq-to-first-rest-map
  "Given a sequence, creates a 1-element map with the first element as key,
  and a seq containing the remaining elements as value."
  [s]
  {(first s) (rest s)})

(defn seq-to-first-all-map
  "Given a sequence, creates a 1-element map with the first element as key,
  and a seq containing all of the elements, including the first, as value."
  [s]
  {(first s) s})
