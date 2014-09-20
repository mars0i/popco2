;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns utils.general ; Utility functions handy for any Clojure program
  (:use [clojure.repl :only [dir-fn]])
  (:require [clojure.pprint :only [*print-right-margin*]]
            [clojure.set :as st]))


(defn remove=
  "Remove items from coll that are = to x."
  [x coll]
  (remove #(= % x) coll))

(defn got
  "Like 'get', but throws an exception if key is not found."
  [m k]
  (get m k 
       (throw (Exception. (str "No key " k)))))

;; An alternative is (defn assoc-if-new [coll k v] (merge {k v} coll)),
;; from optevo at http://stackoverflow.com/questions/25035535/clojure-assoc-if-and-assoc-if-new
(defn assoc-if-new
  [m k v]
  (if (contains? m k)
    m
    (assoc m k v)))

(defn assoc-if-new-throw-if-old
  [m k v]
  (if (contains? m k)
    (throw (Exception. (str "Trying to set value for key " k "a second time, with new value " v)))
    (assoc m k v)))

(defn add-quotes
  "Append initial and terminal double-quote characters to string."
  [string]
  (str "\"" string "\""))

(defn add-quotes-if-str
  [x]
  (if (string? x)
    (add-quotes x)
    x))

(defn seq-to-csv-row-str
  "Given a sequence, create a string representing a row in csv format, with
  each element in the sequence as an element in the csv row.  Strings will
  be surrounded by quote characters.  Comma is used as the delimiter.  A
  terminating newline is not added."
  [s]
  (apply str 
         (interpose ", " (map add-quotes-if-str s))))

;; This is a slightly modified version of an example at clojure-doc.org:
(defn round2
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn file-exists?
  [f]
  (.exists (clojure.java.io/as-file f)))

(defn make-dir
  [f]
  (.mkdir (java.io.File. f)))

(defn make-dir-if-none
  [f]
  (when-not (file-exists? f)
    (make-dir f)))

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

(defn extract-fn-name
  "Given a function, extracts the original function name from Clojure's
  internal string identifier associated with the function, returning this
  name as a string.  Note that the internal string indentifier uses underlines,
  where the original name used dashes, but this function replaces them with 
  dashes to get back the original name.  (If there were underlines in the
  original function name this will replace them with dashes anyway.)"
  [f]
  (clojure.string/replace 
    (clojure.string/replace (str f) #".*\$(.*)@.*" "$1") ; strip off initial and trailing parts of the identifier
    #"_" "-")) ; replace underlines with dashes

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

;(defn collect
;  "If args are collections, concats them; if neither is, creates a collection 
;  containing the two args; otherwise conjs the non-coll onto the coll."
;  [x y]
;  (cond
;    (and (coll? x) (coll? y)) (concat x y)
;    (coll? x) (conj x y)
;    (coll? y) (conj y x)
;    :else [x y]))

(defn collectivize
  "If x is a collection, returns it unchanged.  Otherwise returns a collection
  containing only x."
  [x]
  (if (coll? x)
    x
    (vector x)))

(defn map-keys
  "Returns a map that's like the argument m, but with each key replaced by
  the result of applying f to the original key."
  [f m]
  (zipmap (map f (keys m))
          (vals m)))

(defn map-vals
  "Returns a map that's like the argument m, but with each val replaced by
  the result of applying f to the original val"
  [f m]
  (zipmap (keys m) 
          (map f (vals m))))

(defn map-keys-vals
  "Returns a map that's like the argument m, but with each key replaced by
  the result of applying fk to the original key, and with each val replaced
  by apply fv to the original val."
  [fk fv m]
  (zipmap (map fk (keys m))
          (map fv (vals m))))


(defn coll-map-to-join-pairs
  "Given a map whose values are collections, return a sequence of maps each
  of which has a key from the original map with val one of the members of the 
  collection that was that keys' value.  This is essentially a join table of 
  all unique pairs licensed by the original map."
  [coll-map]
  (mapcat 
    (fn [[k vs]]
      (map #(hash-map k %) vs))
    coll-map))

(defn join-pairs-to-coll-map
  "Given a sequence of maps each of which has a single key/val pair, and in
  which keys may be duplicates, return a map in which values are sequences 
  that collect vals that had the same key in the original sequence of maps."
  [join-pair-coll]
  (map-vals collectivize        ; kludge: merge-with does nothing when only one, so need to wrap
            (apply merge-with 
                   (comp vec flatten vector)
                   join-pair-coll)))

;; Consider using http://stackoverflow.com/questions/23745440/map-of-vectors-to-vector-of-maps instead:
(defn invert-coll-map
  "Given a map whose values are collections, return a map of the same sort,
  but in which each val member is now a key, and the members of their val
  collections are the keys for the current vals' former collections."
  [coll-map]
  (join-pairs-to-coll-map
    (map st/map-invert 
         (coll-map-to-join-pairs coll-map))))

(defn sign-of [x] (if (neg? x) -1 1))

;; Quick tests show suggest that this may be only slightly slower than the non-lazy version,
;; even though it has to traverse the input twice--and even if you map doall over the result.
;; Doing something like the recur version with lazy-seq embedded in it seems slower than this one.
;(defn lazy-split-elements
;  "Given a collection of pairs, returns a pair of two sequences, one containing
;  the first elements of the pairs, in order, and the other containing the
;  second elements of the pairs, in order.  Note that if the input collection
;  is empty, split-elements returns a pair containing two empty sequences."
;  [pairs]
;  (list (map first pairs) (map second pairs)))

;; Version with recur and lazy split
;(defn lazy-split-elements2
;  "Given a collection of pairs, returns a pair of two sequences, one containing
;  the first elements of the pairs, in order, and the other containing the
;  second elements of the pairs, in order.  Note that if the input collection
;  is empty, split-elements returns a pair containing two empty sequences."
;  [pairs]
;  (loop [prs pairs
;         firsts []
;         seconds []]
;    (if (empty? prs)
;      (list firsts seconds)
;      (let [[fst snd] (first prs)]
;        (recur (rest prs)
;               (cons fst (lazy-seq firsts))
;               (cons snd (lazy-seq seconds)) )))))

