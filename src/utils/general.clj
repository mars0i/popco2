(ns utils.general ; Utility functions handy for any Clojure program
  (:use [clojure.repl :only [dir-fn]])
  (:require [clojure.pprint :only [*print-right-margin*]]
            [clojure.set :as st]))

;; dir-fn is useful because dir doseq's println, so you get a long column,
;; whereas dir-fn gives you a seq you can format however you want.

(defn domap
  ([f coll] (doseq [e coll] (f e)))
  ([f coll1 & colls] (mapv f (cons coll1 colls)) nil))

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

(defn rotations
  "Generate all rotations of a sequence."
  [xs]
  (map #(concat (drop % xs) (take % xs))
       (range (count xs))))

;; Notes:
;; You can do the same thing using split-at and reverse, but the speed
;; is the same, and the drop/take version is easier to read.
;; Another version, by Ray Miller in response to my question at
;; https://groups.google.com/forum/#!topic/clojure/VO8V8m6bfEI
;; My later version above is twice as fast, at least on seqs up to
;; count = 100K, even though my version has to concat.
;; (defn rotations
;;   [xs]
;;   (take (count xs) 
;;         (partition (count xs) 1  ; start again 1 past where you last started
;;                    (cycle xs))))
;; See also https://groups.google.com/forum/#!topic/clojure/SjmevTjZPcQ
;; for other versions.

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

(defn reverse-elts
  "Return a sequence constructed from the input sequence, with each
  element in the sequence reversed (but in the same overall order)."
  [xs]
  (map reverse xs))

(defn upper-case-keyword
  "Converts a keyword to its uppercase analogue."
  [kw]
  (keyword 
    (clojure.string/upper-case (name kw))))

(defn lower-case-keyword
  "Converts a keyword to its lowercase analogue."
  [kw]
  (keyword 
    (clojure.string/lower-case (name kw))))

(defn third [s] (nth s 2))

(defn fourth [s] (nth s 3))

(defn nths
  "Return a list of applications of nth to s and the i's."
  ([s i] (list (nth s i)))
  ([s i & is]
   (cons (nth s i) 
         (apply nths s is))))

(defn thn
  "Does the same thing as nth, but with argument order swapped."
  [n xs]
  (nth xs n))

(defn erase-chars
  "Erase up to len characters from the console on the current line."
  [len]
  (print (apply str (repeat len \backspace))))

(defn dorun-nl
  "Like dorun, but prints a newline to console before returning."
  [s]
  (dorun s)
  (println))

(defn lized?
  "Utility function to test whether the output of iterate has been realized
  at all.  Iterate returns a Cons around a LazySeq.  realized? throws an
  exception on a Cons, but will work on a LazySeq.  If class of xs is Cons,
  lized? checks whether its rest is realized.  Otherwise, lized? checks
  whether xs is realized."
  [xs]
  (or (and (instance? clojure.lang.Cons xs)
           (realized? (rest xs)))
      (realized? (rest xs))))

;; This is like (nth (iterate f x) n), but doesn't create an intermediate lazy seq.
(defn fn-pow
  "Apply function f to x, then to the result of that, etc., n times.
  If n <= 0, just returns x."
  [f x ^long n]
  (if (> n 0) 
    (recur f (f x) (dec n))
    x))

(defn comp*
  "Like comp, but expects a sequence of functions.  Applies comp to them."
  [fs]
  (apply comp fs))

(defn join-pair-seq
  "Given a map whose values are collections, return a sequence of maps each
  of which has a key from the original map, and one of the members of the 
  collection that was its value.  This is essentially a join table of all
  unique pairs licensed by the original map."
  [m]
  (mapcat 
    (fn [[k vs]]
      (map #(hash-map k %) vs))
    m))

(defn invert-coll-map
  "Given a map whose values are collections, return a map of the same sort,
  but in which each val member is now a key, and the members of their val
  collections are the keys for the current vals' former collections."
  [m]
  (apply merge-with 
         (comp flatten vector)
         (map st/map-invert 
              (join-pair-seq m))))

;(defn invert-coll-map2
;  [m]
;  (let [v-elts (set (apply concat (vals m)))
;        init-maps (map #(hash-map % []) v-elts) ; we need maps with empty colls as keys, so conj will work
;        data-maps (for [[k v] m              ; a coll of maps from elts in val colls, to the keys of those colls
;                        elt v-elts           ; actually this does the same thing as join-pair-seq
;                        :when (contains? (set v) elt)] {elt k})] ; (set v) to make contains? work
;    (apply merge-with conj 
;           (concat init-maps
;                   data-maps))))


;(defn fn-pow2
;  "Apply function f to x, then to the result of that, etc., n times.
;  If n <= 0, just returns x."
;  [f x n]
;  (if (> n 0) 
;    (recur f (f x) (dec n))
;    x))

;(defn fn-pow1
;  "Apply function f to x, then to the result of that, etc., n times.
;  If n <= 0, just returns x."
;  [f x n]
;  (loop [x x 
;         n n]
;    (if (<= n 0) 
;      x
;      (recur (f x) (dec n)))))

;(defn fn-pow2
;  "Apply function f to x, then to the result of that, etc., n times.
;  If n <= 0, just returns x."
;  [f x n]
;  (if (<= n 0) 
;    x
;    (recur f (f x) (dec n))))

;(defn fn-pow0
;  [f x n]
;  (take
;    (iterate f x)
;    n))

;(defn skip-realized?
;  "Tests whether the first LazySeq instance in the sequence xs has been 
;  realized, skipping over e.g. Cons's before that point.  (e.g. 'iterate'
;  returns a Cons followed by a LazySeq.  If you want to know whether it's 
;  realized beyond the Cons, you have to check its rest.)"
;  [xs]
;  (if (instance? clojure.lang.IPending xs)
;    (realized? xs)
;    (if (empty? xs)
;      true
;      (recur (rest xs)))))

;; Compare skip-realized? with the follwoing, which tests whether there's any unrealized part anywhere down the line.
;; By A. Webb in response to a question of mine, at https://groups.google.com/d/msg/clojure/5rwZA-Bzp9A/dgChQhbeF_AJ
;(defn seq-realized?
;  "Returns false if there is an unrealized tail in the sequence,
;  otherwise true."
;  [s]
;  (if (instance? clojure.lang.IPending s)
;    (if (realized? s)
;      (if (seq s)
;        (recur (rest s))
;        true)
;      false)
;    (if (seq s)
;      (recur (rest s))
;      true)))
