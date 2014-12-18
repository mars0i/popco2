;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; General-purpose utility functions
(ns utils.general
  (require [clojure.set :as st]
           [clojure.pprint :as pp]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Function combination

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Operations on collections


;; MAXES
;; This version is based on James Sharp's and mfike's revision of my original version, at
;; http://codereview.stackexchange.com/questions/74012/getting-rid-of-extra-test-during-initialization-of-loop-recursion
(defn maxes
  "Returns a sequence of elements from s, each with the maximum value of
  (f element).  If f is not provided where s is a collection of numbers, 
  the identity function is used for f.  (The 4-argument version of maxes
  is primarily intended to be called recursively from maxes.)"
  ([s] (maxes identity s))
  ([f s] (maxes f s Double/NEGATIVE_INFINITY []))
  ([f s best-val collected]
   (if (empty? s)
     collected
     (let [new-elt (first s)
           new-val (f new-elt)]
       (when-not (number? new-val)
         (throw (Exception. (pp/cl-format nil "in maxes: Non-numeric value ~a returned from element ~a by function ~a." new-val new-elt f)))) ; str and format don't format nil as "nil".
       (cond (>  new-val best-val) (recur f (rest s) new-val  [new-elt])
             (== new-val best-val) (recur f (rest s) best-val (conj collected new-elt))
             :else                 (recur f (rest s) best-val collected))))))


(defn rotations
  "Generate all rotations of a sequence."
  [xs]
  (map #(concat (drop % xs) (take % xs))
       (range (count xs))))
;; Notes:
;; You can do the same thing using split-at and reverse, but the speed
;; is the same, and the drop/take version is easier to read.
;; Another version, by Ray Miller in response to my question at
;; https://groups.google.com/forum/#!topic/clojure/VO8V8m6bfEI:
;; (defn rotations
;;   [xs]
;;   (take (count xs) 
;;         (partition (count xs) 1  ; start again 1 past where you last started
;;                    (cycle xs))))
;; My later version above is twice as fast, at least on seqs up to
;; count = 100K, even though my version has to concat.
;; See also https://groups.google.com/forum/#!topic/clojure/SjmevTjZPcQ
;; for other versions.


(defn domap
  ([f coll] (doseq [e coll] (f e)))
  ([f coll1 & colls] (mapv f (cons coll1 colls)) nil))

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
