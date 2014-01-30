(ns utils.general ; Utility functions handy for any Clojure program
  (:require [clojure.pprint :only [*print-right-margin*]]))

(defn domap
  [f coll]
  (dotimes [i (count coll)]
    (f (nth coll i))))
;
;(defn domap2
;  ([f c1]
;   (dotimes [i (count c1)] 
;     (f (nth c1 i))))
;  ([f c1 c2]
;   (dotimes [i (count c1)] ; should use min
;     (f (nth c1 i) (nth c2 i)))))
;
;(defn domaps
;  [f & colls]
;  (dotimes [i (apply min (map count colls))]
;    (apply f (map #(nth % i) colls))))

;; doesn't work right?
;; it's OK if you pass it literal vectors of stuff in the colls
;; it's not ok if you pass symbols.  they end up asliterals
(defmacro domap44 [f & colls] 
  `(dotimes [i# (apply min (map count '(~@colls)))]
     (apply ~f (map #(nth % i#) '(~@colls)))))

;; doesn't work right
;(defmacro domapmac
;  [f & colls]
;  (let [i (gensym "i")]
;    `(dotimes [~i (min ~@(map count colls))]
;       (~f ~@(map #(list 'nth % i) colls)))))

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
