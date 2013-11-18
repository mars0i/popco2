(ns popco.core.acme
  (:use popco.core.lot)
  (:import [popco.core.lot Propn Pred Obj])
  (:require [utils.general :as ug]
            [clojure.math.combinatorics :as comb]
            [clojure.core.matrix :as mx])
  (:gen-class))

;; SEE acme.md for an overview of what's going on in this file.

;;; TODO construct link matrix and weight matrix with positive weights
;;; TODO add negative weights to weight matrix
;;; NOTE for the analogy net we probably don't really need the link
;;; matrix, strictly speaking, because there are no zero-weight links.
;;; For the belief network, however, we need to allow zero-weight links,
;;; so a link matrix is needed.

(def pos-link-increment 0.1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 1
;; Find out which propositions can be paired up, i.e. the isomorphic ones.

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
    `(~p1 ~p2)))

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
;; STEP 2
;; For all isomporphic propositions, pair up their components
;; (By separating step 1 and step 2, we do some redundant work, but it makes
;; the logic a bit simpler, for now, and we'll only be doing this once for the
;; whole population at the beginning of each simulation.  By separating step 2
;; and step 3--which merely flattens what we produce here, we preserve structure
;; of relationships between map nodes for use in step 4.)

;; i.e. we feed the return value of match-propns, above, to 
;; match-propn-components-too, below.

;; NOTE The function match-args differs from an earlier version (now called 
;; deep-match-args) in that it
;; doesn't try to perform matching on args of propns that appear as args
;; of propns.  Rather, we *just* match the propns themselves when they appear
;; in argument place.  (We *did* need to recurse on args to decide what propns
;; match what propns--that was the desired behavior in H&T1989, but not the
;; actual behavior in ACME, nor what was described in the p. 314 specification,
;; but it was what happened in POPCO 1 and what "Moderate Role" described.)
;; However, at this stage we should assume that we are only looking
;; at propn pairs that match in this deep way.  That doesn't mean that weights
;; get calculated from matchings that have to do with what's inside the propns
;; that are args.  The H&T1989 algorithm does *not* do this, nor did POPCO 1.

(declare mapnode-map match-components-of-propn-pair match-propn-components ids-to-mapnode-id)

;; Note that order within pairs matters.  It preserves the distinction
;; between the two analogue structures, and allows predicates and objects
;; to have the same names in different analogue structures (sets don't allow that).
(defn match-propn-components
  "Returns a (lazy) sequence of sequences (families) of mapped-pairs of matched
  Propns, Preds, or Objs from a sequence of of pairs of Propns.  Each pair is a
  map with keys :alog1 and :alog2 (analog 1 & 2).  The resulting pairs
  represent the 'sides' of map nodes.  Each subsequence contains the pairs from
  one proposition.  Each Propn family sequence consists of a Clojure map
  representing a pair of Propns, a clojure map representing a pair of Preds,
  and clojure maps representing paired arguments (whether Objs or Propns).  
  Note that although Propns that are args of Propns are matched, there's
  no deeper matching on the Preds and arguments of these embedded Propns."
  [pairs]
  (map match-components-of-propn-pair pairs))

;; NOTE we use sorted-maps here, returned by mapnode-map because when we construct 
;; mapnode ids, we need it to be the case that (vals clojure-map) always returns these
;; vals in the same order :alog1, :alog2:
(defn match-components-of-propn-pair
  "ADD DOCSTRING"
  [[p1 p2]]
  ;; that the following is a seq, not vec, flags that it's a family of map-pairs from the same propn
  (cons (mapnode-map p1 p2)                 ; we already know the propns match
        (cons (mapnode-map (:pred p1) (:pred p2)) ; predicates always match if the propns matched
              (map mapnode-map (:args p1) (:args p2)))))

;; NOTE we use sorted-maps here because when we construct 
;; mapnode ids, we need it to be the case that (vals clojure-map) always returns these
;; vals in the same order :alog1, :alog2:
(defn mapnode-map
  "Given two lot-items, return a map representing an ACME map-node between them,
  with the first and second lot-items as :alog1, :alog2, respectively, and with
  an id constructed by id-pair-to-mapnode-id from the id's of the two lot-items."
  [alog1 alog2] 
  (sorted-map :alog1 alog1 :alog2 alog2 
              :id (ids-to-mapnode-id (:id alog1) (:id alog2))))

;;;;;;;;;;;;
;; 
;; MAY BE OBSOLETE AND UNUSEFUL (earlier versions of preceding functions):

; (declare deep-match-args deep-match-components-of-propn-pair deep-match-propn-components)
; 
; ;; Note that order within pairs matters.  It preserves the distinction
; ;; between the two analogue structures, and allows predicates and objects
; ;; to have the same names in different analogue structures (sets don't allow that).
; (defn deep-match-propn-components
;   "Returns a (lazy) sequence of sequences (families) of mapped-pairs of matched
;   Propns, Preds, or Objs from a sequence of of pairs of Propns.  Each pair is a
;   map with keys :alog1 and :alog2 (analog 1 & 2).  The resulting pairs
;   represent the 'sides' of map nodes.  Each subsequence contains the pairs from
;   one proposition.  Each Propn family sequence consists of a Clojure map
;   representing a pair of Propns, a clojure map representing a pair of Preds,
;   and a vector containing representations of paired arguments.  The contents of
;   this vector are Clojure maps where the corresponding arguments are Objs, and
;   family sequences where the corresponding args are Propns.  i.e. Propns'
;   arguments are embedded in a vector so you can tell whether you're looking at
;   a collection of pairs from two Propns or pairs from arguments by testing with
;   seq? and vec?."
;   [pairs]
;   (map deep-match-components-of-propn-pair pairs))
; 
; ;; NOTE we use sorted-maps here because when we construct mapnode ids,
; ;; we need it to be the case that (vals clojure-map) always returns these
; ;; vals in the same order :alog1, :alog2:
; ;;
; (defn deep-match-components-of-propn-pair
;   ;; ADD DOCSTRING
;   [[p1 p2]]
;   ;; return a seq of matched pairs:
;   (list    ; that this is a list (not vec) flags that this is a family of map-pairs from the same proposition
;     (sorted-map :alog1 p1 :alog2 p2)                 ; we already know the propns match
;     (sorted-map :alog1 (:pred p1) :alog2 (:pred p2)) ; predicates always match if the propns matched
;     (vec (map deep-match-args (:args p1) (:args p2)))))   ; args deep-match if objs, propns need more work.  vec means these pairs are from two arglists
; 
; ;; ADD DOCSTRING
; (defmulti  deep-match-args (fn [x y] [(class x) (class y)]))
; (defmethod deep-match-args [Obj Obj] [o1 o2] (sorted-map :alog1 o1 :alog2 o2))
; (defmethod deep-match-args [Propn Propn] [p1 p2] (deep-match-components-of-propn-pair [p1 p2]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 3
;; Make a flat sequence of unique pairs.  Define vectors and matrices with it.
;; Also define ids for map nodes.
;; The sequence specifies what the analogy network nodes are, i.e. specifies 
;; the meaning of elements in activation vectors and meaning of matrix rows and cols.

;; NOTE some of these functions will probably be abstracted out into a separate file 
;; and ns later, since they'll be used for the proposition network, too.

(defn ids-to-mapnode-id
  "Given two id keywords, constructs and returns a corresponding mapnode id."
  [id1 id2]
  (keyword 
    (str (name id1) "=" (name id2))))

(defn id-pair-to-mapnode-id
  "Given a 2-element sequence of id keywords, constructs and returns 
  a corresponding mapnode id."
  [[id1 id2]]
  (ids-to-mapnode-id id1 id2))

;; NOTE: According to several remarks on the Internet from 2010 into 2013, (keys x)
;; and (vals x) return keys and vals in the same corresponding order.  Moreover,
;; other remarks say that for sorted-maps, (vals x) always returns values according
;; to the sort-order of keys.  So if pairmap is a sorted map, the ids should always
;; come out in the order of :alog1, :alog2.  This is important, because otherwise
;; the mapnode ids we construct from these pairs might arbitrarily swap the parts of
;; the name on either side of "="; thus id's of map nodes would be unstable.  
;; This point about order seems not to be guaranteed by any explicit documentation,
;; as of 10/2013, but the sentiment on the net seems to be that it's reasonable to
;; assume that this behavior won't change.  The most thorough and authoritative 
;; statement that I've found so far (11/2013) about order of (vals x) for sorted-maps
;; is here: https://groups.google.com/d/msg/clojure/2AyndHfeigk/zaD9T5mT6WkJ 

(defn pair-map-to-id-pair
  "Given a map containing two LOT items, returns a 2-element sequence of their ids,
  in order of keys, i.e. in :alog1, :alog2 order if the map is a sorted-map."
  [pairmap]
  (map :id (vals pairmap))) ; See note above about order of vals.

(def pair-map-to-mapnode-id
  (comp id-pair-to-mapnode-id pair-map-to-id-pair))
(ug/add-to-docstr pair-map-to-mapnode-id
  "Given a map containing two LOT items, constructs and returns a corresponding
  mapnode id.")

(defn add-id-to-pair-map
  "Given a map containing two LOT items, adds an id field with a mapnode id."
  [pairmap]
  (assoc pairmap :id (pair-map-to-mapnode-id pairmap)))

;; The use of flatten in this function depends on the fact that (a) map-pairs 
;; are not sequences, and (b) all larger groupings of data are sequences.
(defn make-acme-node-vec
  "Given a tree of node info entries (e.g. Propns, pairs of Propns or 
  Objs, etc.), returns a Clojure vector of unique node info entries 
  allowing indexing particular node info entries.  This node vector is
  typically shared by all members of a population; it merely provides
  information about nodes that a person might have.."
  [node-tree]
  (vec 
    (map add-id-to-pair-map
         (distinct (flatten node-tree)))))

;; MOVE TO SEPARATE FILE/NS
(defn make-index-map
  "Given a sequence of things, returns a map from things to indexes.  
  Allows reverse lookup of indexes from the things."
  [ids]
  (zipmap ids (range (count ids))))

;; MOVE TO SEPARATE FILE/NS
(defn make-activn-vec
  "Returns a core.matrix vector of length len, filled with zeros,
  to represent activation values of nodes.  Each person has its own 
  activation vector."
  [len]
  (mx/new-vector len))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 4
;; Make weight matrix representing link weights

;(defn propn-families-to-id-families
;  [fams]
;  (map 
;    #(map pair-map-to-mapnode-id %) 
;    fams))

;; MOVE TO SEPARATE FILE/NS
(defn make-wt-mat
  "Returns a core.matrix square matrix with dimension dim, filled with zeros."
  [dim]
  (mx/new-matrix dim dim))

;; NOT SURE if this is the best strategy.
;; means have to divide inc by 2--we only need half as many connections
;; cf.  http://stackoverflow.com/questions/4053845/idomatic-way-to-iterate-through-all-pairs-of-a-collection-in-clojure 
(defn add-families-wts-to-mat!
  "ADD DOCSTRING"
  [mat fams index-map increment]
  (map #(doseq [[itm1 itm2] (comb/combinations %)]
          (let [i (index-map (:id itm1)) 
                j (index-map (:id itm2))]
            (mx/mset! mat i j (+ increment (mx/mget mat i j)))))
       fams)
  mat)

;; REST OF THIS SECTION MAY BE OBSOLETE
;; REST OF THIS SECTION MAY BE OBSOLETE

; ;; NOTE this is returning propn families in arg lists when args are propns.
; ;; This is not really what I need.  I can work with it, but maybe this function
; ;; could be modified to do something different.  Note that what I really
; ;; need, in the end, are only ids.  For each propn family at the top level,
; ;; I need ids for its propn pair, its pred pair, and all its arg pairs, 
; ;; whether they are for objs or propns.  I don't need any info about the
; ;; embedded arg-propn-pair's other family members.  I also don't actually
; ;; need the args to be set off in a vector.  The whole family list here
; ;; could just be completely flat.  (Note that means that at this stage,
; ;; in these lists, there's actually no distinction between the propn
; ;; whose family it is, and the propns that are args to it.  The linking
; ;; behavior is exactly the same.  (Hmm though conceivably that could
; ;; someday change.  But that's just a distant possibility.)
; (defn list-propn-families
;   "Given a pair-tree produced by match-propn-components, return a seq of
;   all propn-families in pair-tree at any level."
;   [pair-tree]
;   (filter seq?   ; get rid of vectors, lot-items, pair maps, since the seqs represent proposition-proposition pair-map families
;           (rest  ; drop first element, the original pair-tree
;             (tree-seq 
;               #(not (or (pred? %) (obj? %))) ; pair-tree contains seqs, vectors, sorted-maps, and lot-items
;               #(cond (seq? %)  %
;                      (vector? %)  %
;                      (propn? %)  (:args %) ; must precede 'map?' since records are maps
;                      (and (sorted? %) (map? %))  (vector (:alog1 %) (:alog2 %)) ; in case the %'s are Propns
;                      :else (throw (Exception. (format "list-propn-families encounted unknown object: %s"))))
;               pair-tree))))
; 
; (defn make-propn-families-shallow
;   "ADD DOCSTRING"
;   [fams]
;   (map (fn [fam] (concat 
;                    (take 2 fam) 
;                    (map (fn [arg-elt] 
;                           (if (seq? arg-elt) 
;                             (first arg-elt) 
;                             arg-elt)) 
;                         (nth fam 2))))
;          fams))
; 
; (def list-shallow-propn-families 
;   (comp make-propn-families-shallow list-propn-families))
; (ug/add-to-docstr list-shallow-propn-families
;    "ADD DOCSTRING")
; 
; ;; TODO NOT RIGHT
; (defn list-ids-in-propn-families 
;   "ADD DOCSTRING"
;   [pair-tree]
;   (map #(map id-pair-to-mapnode-id [(:alog1 %) (:alog2 %)])
;        (list-shallow-propn-families pair-tree)))
; 
; ;; TODO OBSOLETE (?)  DELETE ME
; ;(defn remove-empty-seqs
; ;  [coll]
; ;  (filter #(not (and (seq? %) (empty? %)))  ; can't use seq: needs to work with non-seqs, too
; ;          coll))
; 
; ;; TODO OBSOLETE (?)  DELETE ME
; ;; THIS IS SURELY WRONG.  (And nasty, regardless.)
; ;; Also, we really only need :ids in the result.
; ;(defn funky-butt-raise-propn-families
; ;  "Return a seq of all propn-families in pair-tree."
; ;  [pair-tree]
; ;  (letfn [(f [out in]
; ;            (let [out- (remove-empty-seqs out) ; remove-empty-seqs is just papering over a problem? shouldn't be needed?
; ;                  thisone (first in)
; ;                  therest (rest in)]
; ;              (if (empty? in)
; ;                out-
; ;                (cond (seq? thisone) (f (concat (conj out- thisone) (map (partial f ()) thisone))
; ;                                        therest)
; ;                      (propn? thisone) (f (concat out- 
; ;                                                  (mapcat (partial f ()) 
; ;                                                          (:args thisone))) 
; ;                                          therest)
; ;                      (map? thisone) (f (concat (f () (:alog1 thisone))
; ;                                                (f () (:alog2 thisone))
; ;                                                out-) 
; ;                                        therest)
; ;                      :else (f out- therest)))))]
; ;    (f () (vec pair-tree))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ALL STEPS - put it all together
;; ...

;; MOVE TO SEPARATE FILE/NS
(defn make-nn-strus
  "Given a sequence of data on individual nodes, returns a clojure map with 
  three entries:
  :nodes -   A Clojure vector of data providing information about the meaning
             of particular neural net nodes.  The indexes of the data items
             correspond to indexes into activation vectors and rows/columns
             of weight matrices.  This vector may be identical to the sequence
             of nodes passed in.
  :indexes - A Clojure map from ids of the same data items to integers, 
             allowing lookup of a node's index from its id.
  :wt-mat -  A core.matrix square matrix with dimensions equal to the number of
             nodes, with all elements initialized to 0.0."
  [node-seq]
  {:nodes (vec node-seq)
   :indexes (make-index-map (map :id node-seq))
   :wt-mat (make-wt-mat (count node-seq))})

(defn make-acme-nn-strus
  ;; ADD DOCSTRING
  [pset1 pset2]
  (let [pair-tree (match-propn-components (match-propns pset1 pset2))
        node-vec (make-acme-node-vec pair-tree)]
    (make-nn-strus node-vec)))

;; NOW REARRANGE THE PRECEDING OR ADD TO IT TO USE THE TREE RETURNED
;; BY match-propn-components TO CONSTRUCT POSITIVE WEIGHTS AND FILL
;; THE MATRIX.  THEN AN UN-distinct-ED NODE SEQ TO CONSTRUCT NEGATIVE
;; WEIGHTS AND FILL THOSE INTO THE MATRIX.  See acme.nt4 for more.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; UTILITIES FOR DISPLAYING DATA STRUCTURES DEFINED ABOVE

(declare fmt-pair-map-families fmt-pair-maps fmt-pair-map)

(defn fmt-pair-map-families
  "Format a sequence of pair-map families into a tree of pairs of :id's."
  [fams]
  (map fmt-pair-maps fams))

(defn fmt-pair-maps
  "Format a sequence of pair-maps into a tree of pairs of :id's.
  The sequence might represent the family of pair-maps from a proposition,
  or it might represent the vector of mapped arguments of the proposition."
  [pairs] ; could be family, or could be mapped args
  (map fmt-pair-map pairs))

(defn fmt-pair-map
  "Format a pair-map represented by a Clojure map, or a collection of them.
  Pair-maps are displayed as 1-element Clojure maps.  Propn-families of
  pair-maps are displayed as sequences, and argument lists are displayed as
  vectors.  (Note: Representing pair-maps as Clojure maps from one item to the
  other has no meaning; t's just a convenient way to get curly braces rather
  than parens or square braces.)"
  [pair-or-pairs]
  (cond (seq? pair-or-pairs)         (fmt-pair-maps pair-or-pairs) ; it's a family of pairs from one Propn
        (vector? pair-or-pairs) (vec (fmt-pair-maps pair-or-pairs)) ; it's an arglist--return in vec to flag that
        :else (array-map (:id (:alog1 pair-or-pairs)) (:id (:alog2 pair-or-pairs))))) ; it's a pair

(defn ppause
  "pprint each element of a sequence in turn, pausing until user hits Enter."
  [sq]
  (doseq [x sq]
    (clojure.pprint/pprint x)
    (read-line)))

;; Handy for displaying output of match-propns:
(defn pair-ids
  "Return sequence of pairs of :id fields of objects from sequence prs of pairs."
  [prs]
  (sort  ; does the right thing with pairs of keywords
    (map (fn [[p1 p2]] 
           [(:id p1) (:id p2)])
         prs)))
