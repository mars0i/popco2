(ns popco.core.acme
  (:use popco.core.lot)
  (:import [popco.core.lot Propn Pred Obj])
  (:require [utils.general :as ug]))

;; ACME: an implementation of Holyoak & Thagard's (1989) ACME method
;; of constructing a constraint satisfaction network from two sets
;; of simple representations of entertained propositions.

;;; TODO construct link matrix and weight matrix with positive weights
;;; TODO add negative weights to weight matrix
;;; NOTE for the analogy net we probably don't really need the link
;;; matrix, strictly speaking, because there are no zero-weight links.
;;; For the belief network, however, we need to allow zero-weight links,
;;; so a link matrix is needed.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; First step: Find out which propositions can be paired up because
;; they're isomorphic:

(declare propns-match? args-match?)

;; Note that order within pairs matters:  It preserves the distinction
;; between the two analogue structures.
(defn match-propns
  "Returns a (lazy) sequence of 2-element sequences, each containing two 
  propositions that match according to propns-match?.  These are propositions 
  that are isomorphic and can be used to construct map nodes."
  [pset1 pset2]
  (for [p1 pset1
        p2 pset2
        :when (propns-match? p1 p2)]
    [p1 p2]))

;; similar to deep-isomorphic-arglists in popco 1
(defn propns-match?
  "Tests whether two propositions are isomorphic in the ACME sense.
  Recursively checks propositions given as arguments of propositions.
  Returns true if they're isomorphic, false otherwise."
  [p1 p2]
  (let [args1 (:args p1)   ; predicates always match, so we only check args
        args2 (:args p2)]
  (and (= (count args1) (count args2))
       (every? identity (map args-match? args1 args2)))))

;; similar to isomorphic-args in popco 1
(defmulti  args-match? (fn [x y] [(class x) (class y)]) )
(defmethod args-match? [Obj Propn] [_ _] false)
(defmethod args-match? [Propn Obj] [_ _] false)
(defmethod args-match? [Obj Obj] [_ _] true)   ; objects always match
(defmethod args-match? [Propn Propn] [p1 p2] (propns-match? p1 p2))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Second step: For all isomporphic propositions, pair up their components
;; By separating step 1 and step 2, we do some redundant work, but it makes
;; the logic a bit simpler, for now, and we'll only be doing this once for the
;; whole population at the beginning of each simulation.

;; i.e. we feed the return value of match-propns, above, to 
;; match-propn-components-too, below.

(declare match-args match-components-of-propn-pair match-propn-components)

;; Note that order within pairs matters.  It preserves the distinction
;; between the two analogue structures, and allows predicates and objects
;; to have the same names in different analogue structures (sets don't allow that).
(defn match-propn-components
  "Returns a (lazy) sequence of vectors (families) of mapped-pairs of matched 
  Propns, Preds, or Objs from a sequence of of pairs of Propns.  Each pair is 
  a map with keys :alog1 and :alog2 (analog 1 & 2).  The resulting pairs 
  represent the 'sides' of map nodes.  Each subsequence contains the pairs from
  one proposition.  If a Propn has one or more Propns as arguments, then there
  will be similar vectors embedded.  Each family vector consists of a Clojure
  map representing a pair of Propns, a clojure map representing a pair of
  Preds, and a vector containing representations of paired arguments.  The
  contents of this vector are Clojure maps where the corresponding arguments
  are Objs, and family-vectors where the corresponding args are Propns."
  [pairs]
  (map match-components-of-propn-pair pairs))

(defn match-components-of-propn-pair
  [[p1 p2]]
  ;; return a vector of matched pairs:
  [{:alog1 p1 :alog2 p2}                 ; we already know the propns match
   {:alog1 (:pred p1) :alog2 (:pred p2)} ; predicates always match if the propns matched
   (vec (map match-args (:args p1) (:args p2)))]) ; args match if objs, propns need more work

(defmulti  match-args (fn [x y] [(class x) (class y)]))
(defmethod match-args [Obj Obj] [o1 o2] {:alog1 o1 :alog2 o2})
(defmethod match-args [Propn Propn] [p1 p2] (match-components-of-propn-pair [p1 p2]))


;;; utilities for displaying above pair-map trees
(declare fmt-pair-map-families fmt-pair-map-vec fmt-pair-map)

(defn fmt-pair-map-families
  "Format a sequence of pair-map families into a tree of vector pairs of :id's."
  [fams]
  (map fmt-pair-map-vec fams))

(defn fmt-pair-map-vec
  "Format a vector of pair-maps into a tree of vector pairs of :id's.
  The vector might represent the family of pair-maps from a proposition,
  or it might represent the vector of mapped arguments of the proposition."
  [pairvec] ; could be family, or could be mapped args
  (vec (map fmt-pair-map pairvec)))

(defn fmt-pair-map
  "Format a pair-map represented by a Clojure map, after testing to see whether
  instead we we passed a vector.  If it's a vector, then it contains pairings
  of args from two Propns.  In that case call fmt-pair-map-vec on it."
  [pairmap]
  (if (vector? pairmap)
    (fmt-pair-map-vec pairmap) ; it's an arglist
    [(:id (:alog1 pairmap)) (:id (:alog2 pairmap))])) ; it's a pairmap

;; NEXT TWO WILL WORK FOR THE PROPN NET, TOO, SO THEY MIGHT BE MOVED LATER.

(defn index-to-node-vec
  "Given a sequence of node info entries (e.g. Propns, pairs of Propns or 
  Objs, etc.), returns a vector allowing indexing node info entries."
  [nodes-info]
  (vec nodes-info))

;; match-propn-components gives us a sequence of node info pairs which can be turned into
;; a vector of some kind, and then we can index into it.  It's also useful
;; to be able to look up the pairs from their indexes.  This function generates
;; a hashmap that gives us that.  We can also use it with the belief network.

(defn node-to-index-map
  "Given a sequence of node info entries (e.g. Propns, pairs of Propns or 
  Objs, etc.), returns a hashmap from node info entries to indexes.  Allows
  reverse lookup of the node entries' indexes."
  [nodes-info]
  (zipmap nodes-info (range (count nodes-info))))

;; Handy for displaying output of match-propns:
(defn pair-ids
  "Return sequence of pairs of :id fields of objects from sequence prs of pairs."
  [prs]
  (sort  ; does the right thing with pairs of keywords
    (map (fn [[p1 p2]] 
           [(:id p1) (:id p2)])
         prs)))
