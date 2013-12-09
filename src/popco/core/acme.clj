(ns popco.core.acme
  (:use popco.core.lot
        [clojure.pprint :only [cl-format]])
  (:import [popco.core.lot Propn Pred Obj])
  (:require [utils.general :as ug]
            [clojure.core.matrix :as mx]
            [clojure.string :as string])
  (:gen-class))

;; SEE acme.md for an overview of what's going on in this file.

;;; NOTE for the analogy net we probably don't really need the link
;;; matrix, strictly speaking, because there are no zero-weight links.
;;; For the belief network, however, we need to allow zero-weight links,
;;; so a link matrix will be needed.

;; TODO
;; - Hand-spot-check whether the new neg weights are coming out right
;; - Decide how to revise (or not) code to handle fact that neg weights
;;   don't sum.  possibly just throw an exception, for the analogy net,
;;   if the previous weight isn't zero.
;; - Reorganize the add-weights-* functions to abstract out what's common 
;;   to them, and give them the same syntax.
;; - Clean up/abstractout/whatever make-acme-nn-stru.  The let has gotten 
;;   out of hand.

(def pos-link-increment 0.1)
(def neg-link-value -0.2)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS TO BE MOVED TO ONE OR MORE SEPARATE FILES/NSes

(defn make-id-to-idx-map
  "Given a sequence of things, returns a map from things to indexes.  
  Allows reverse lookup of indexes from the things."
  [ids]
  (zipmap ids (range (count ids))))

(defn make-activn-vec
  "Returns a core.matrix vector of length len, filled with zeros,
  to represent activation values of nodes.  Each person has its own 
  activation vector."
  [len]
  (mx/new-vector len))

(defn make-wt-mat
  "Returns a core.matrix square matrix with dimension dim, filled with zeros."
  [dim]
  (mx/new-matrix dim dim))

;; Both analogy nets and proposition nets have nodes and links between them.
;; That means that both have (1) information on the meaning of each node,
;; (2) a matrix representing link weights (actually this will probably turn
;; into two or three matrices later), and (3) a mapping from nodes to row or
;; column indexes, and back, to keep track of the relationship between nodes 
;; and their links.  This function initializes (A) a vector of node info, 
;; which are maps containing, at least, an :id, so that node info can be looked 
;; up from indexes; (B) a map from node ids to indexes, so that indexes can
;; be looked up from nodes, and (C), a matrix which will contain link weights.
;; The matrix is initialized by this function to contain all zeros, since setting
;; the link weights is a more complicated operation that depends on the purpose
;; of the constrain network.
(defn make-nn-stru
  "Given a sequence of data on individual nodes, returns a clojure map with 
  three entries:
  :node-vec -   A Clojure vector of data providing information about the meaning
             of particular neural net nodes.  The indexes of the data items
             correspond to indexes into activation vectors and rows/columns
             of weight matrices.  This vector may be identical to the sequence
             of nodes passed in.
  :id-to-idx - A Clojure map from ids of the same data items to integers, 
             allowing lookup of a node's index from its id.
  :pos-wt-mat -  A core.matrix square matrix with dimensions equal to the number of
             nodes, with all elements initialized to 0.0."
  [node-seq]
  {:node-vec (vec node-seq)
   :id-to-idx (make-id-to-idx-map (map :id node-seq)) ; index order will be same as node-seq's order
   :pos-wt-mat (make-wt-mat (count node-seq))
   :neg-wt-mat (make-wt-mat (count node-seq))})

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

(declare make-mapnode-map match-components-of-propn-pair match-propn-components ids-to-mapnode-id)

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

;; NOTE we use sorted-maps here, returned by make-mapnode-map because when we construct 
;; mapnode ids, we need it to be the case that (vals clojure-map) always returns these
;; vals in the same order :alog1, :alog2:
(defn match-components-of-propn-pair
  "ADD DOCSTRING"
  [[p1 p2]]
  ;; that the following is a seq, not vec, flags that it's a family of map-pairs from the same propn
  (cons (make-mapnode-map p1 p2)                 ; we already know the propns match
        (cons (make-mapnode-map (:pred p1) (:pred p2)) ; predicates always match if the propns matched
              (map make-mapnode-map (:args p1) (:args p2)))))

;; NOTE we use sorted-maps here because when we construct 
;; mapnode ids, we need it to be the case that (vals clojure-map) always returns these
;; vals in the same order :alog1, :alog2:
(defn make-mapnode-map
  "Given two lot-items, return a map representing an ACME map-node between them,
  with the first and second lot-items as :alog1, :alog2, respectively, and with
  an id constructed by id-pair-to-mapnode-id from the id's of the two lot-items."
  [alog1 alog2] 
  (sorted-map :alog1 alog1 :alog2 alog2 
              :id (ids-to-mapnode-id (:id alog1) (:id alog2))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 3
;; Make a flat sequence of unique pairs.  Define vectors and matrices with it.
;; Also define ids for map nodes.
;; The sequence specifies what the analogy network nodes are, i.e. specifies 
;; the meaning of elements in activation vectors and meaning of matrix rows and cols.

(defn alog-ids
  "Given a mapnode, return a sequence of the two analogs' ids, in order."
  [mapnode] 
  [(:id (:alog1 mapnode)) 
   (:id (:alog2 mapnode))])

;; (cf. notes/ShouldReversedPredMapNodesBeTheSame)
(defn make-two-ids-to-idx-map 
  "Given a seq of mapnodes and a map from mapnode ids to indexes, returns a map from
  seq pairs of ids of the 'sides' from two analogs, to the corresponding indexes.
  i.e. generates a map that does the same thing as the indexes parameter, using 
  what's a seq of what's on either side of the '=' character in the mapnode id, rather
  than the mapnode id.  This makes it easier to look up indexes directly from a pair
  of proposition, predicate, or object ids.  Note the key is a seq, so [:A :B] is
  not the same as [:B :A] (preserving distinct within-analog relations).  (You can
  use any kind of pair seq as a key, it appears.)"
  [mapnodes indexes]
  (zipmap
    (map alog-ids mapnodes)
    (map #(indexes (:id %)) mapnodes))) ; get indexes corresponding to each pair

;; functions for constructing ids of mapnodes:

(defn ids-to-mapnode-id
  "Given two id keywords, constructs and returns a corresponding mapnode id."
  [id1 id2]
  (keyword 
    (str (name id1) "=" (name id2))))

; (defn id-pair-to-mapnode-id
;   "Given a 2-element sequence of id keywords, constructs and returns 
;   a corresponding mapnode id."
;   [[id1 id2]]
;   (ids-to-mapnode-id id1 id2))
; 
; ;; See note below on order of keys and vals
; (defn pair-map-to-id-pair
;   "Given a map containing two LOT items, returns a 2-element sequence of their ids,
;   in order of keys, i.e. in :alog1, :alog2 order if the map is a sorted-map."
;   [pairmap]
;   (map :id (vals pairmap))) ; See note above about order of vals.
; 
; (def pair-map-to-mapnode-id
;   (comp id-pair-to-mapnode-id pair-map-to-id-pair))
; (ug/add-to-docstr pair-map-to-mapnode-id
;   "Given a map containing two LOT items, constructs and returns a corresponding
;   mapnode id.")
; 
; (defn add-id-to-pair-map
;   "Given a map containing two LOT items, adds an id field with a mapnode id."
;   [pairmap]
;   (assoc pairmap :id (pair-map-to-mapnode-id pairmap)))

;; NOTE re pair-map-to-id-pair, etc.:
;; According to several remarks on the Internet from 2010 into 2013, (keys x)
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 4
;; Make weight matrix representing positive link weights

(defn add-pos-wts-to-mat!
  "ADD DOCSTRING"
  [mat fams indexes increment]
  (doseq [fam fams]
    (doseq [itm1 fam           ; fam is a list of map-pairs
            itm2 fam]          ; we want all poss ordered pairs
      (when-not (= itm1 itm2)  ; except duplicates
        (let [i (indexes (:id itm1)) 
              j (indexes (:id itm2))]
          ;(cl-format true "~%setting link between ~s, ~s at ~s ~s~%" (:id itm1) (:id itm2) i j) ; DEBUG
          (mx/mset! mat i j (+ increment (mx/mget mat i j)))))))
  mat)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 5
;; Make weight matrix representing negative link weights

;; IF THIS WORKS, CONSIDER REWRITING ADD-POSS-WTS-TO-MAT! AROUND IT (THIS IS MORE ABSTRACT)
;; TODO: Check that there should be no summing.
;; Check whether there could be summing, even.
;; Revise this as necessary, since at present, it sums.
(defn add-neg-wts-to-mat!
  "ADD DOCSTRING"
  [mat idx-fams wt-val]
  (doseq [fam idx-fams]
    (doseq [i fam           ; TODO fam is a list of map-pairs
            j fam]          ; TODO we want all poss ordered pairs
      (when-not (= i j)  ; except duplicates TODO IS THIS RIGHT?
        (mx/mset! mat i j (+ wt-val (mx/mget mat i j))))))
  mat)

;; TODO TEST ME
;; generic version
(defn add-wts-to-mat!
  [mat idx-fams wt-val op]
  (doseq [fam idx-fams]
    (doseq [i fam
            j fam]
      (when-not (= i j)
        (mx/mset! mat i j (op wt-val (mx/mget mat i j))))))
  mat)

;; TODO TEST ME
(defn set-wt
  [new-val old-val]
  (if (zero? old-val)
    (throw (Exception. 
             (format "Trying to overwrite nonzero weight at indexes %s %s" i j)))
    new-val))

;; TODO TEST ME
;; new replacement using generic version
(defn new-add-neg-wts-to-mat!
  [mat idx-fams wt-val]
  (add-wts-to-mat! mat idx-fams set-wt))

;; TODO TEST ME
;; new replacement using generic version - note needs to be passed idx fams
(defn new-add-pos-wts-to-mat!
  [mat idx-fams wt-val]
  (add-wts-to-mat! mat idx-fams +))

(defn competing-mapnode-fams
  "Return a seq of seqs, where each inner seq contains paired lot-elts
  corresponding to mapnodes that are in competion with others in the
  same inner seq."
  [mapnode-pairs]
  (concat
    (ug/partition-sort-by first mapnode-pairs)
    (ug/partition-sort-by second mapnode-pairs)))

(defn competing-mapnode-idx-fams
  "Return a seq of seqs, where each inner seq contains paired indexes
  corresponding to mapnodes that are in competion with others in the
  same inner seq."
  [ids-to-idx]
  (map #(map ids-to-idx %)
       (competing-mapnode-fams (keys ids-to-idx))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ALL STEPS - put it all together
;; ...

(defn make-acme-nn-stru
  ;; ADD DOCSTRING
  [pset1 pset2 pos-increment neg-increment]
  (let [fams (match-propn-components (match-propns pset1 pset2))
        mapnodes (distinct (flatten fams)) ; use of flatten here assumes map-pairs aren't seqs
        node-vec (vec mapnodes) ; IDs already added by m-p-c: (vec (map add-id-to-pair-map mapnodes))
        temp-nn-stru (make-nn-stru node-vec)
        id-to-idx (:id-to-idx temp-nn-stru) ; index order will be same as node-vec order
        nn-stru (assoc temp-nn-stru 
                       :ids-to-idx (make-two-ids-to-idx-map mapnodes id-to-idx))]
    (add-pos-wts-to-mat! (:pos-wt-mat nn-stru) fams id-to-idx pos-increment)
    (add-neg-wts-to-mat! (:neg-wt-mat nn-stru)
                         (competing-mapnode-idx-fams (:ids-to-idx nn-stru))
                         neg-increment)
    nn-stru))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS FOR DISPLAYING MATRICES, NN-STRUS WITH LABELS

(defn max-strlen
  "Returns the maximum length of the strings contained in its argument."
  [strings]
  (apply max (map count strings)))

(defn collcolls-to-vecvecs
  "Converts a collection of collections to a vector of vectors."
  [coll]
  (vec (map #(vec %) coll)))

;; Code notes:
;; This function does the following:
;; - Add spaces to beginning of labels so they're all the same length.
;; - Convert each string to a vector of characters, and then use a matrix
;;   transpose operation to produce inner vectors of characters each of which
;;   is from the same position in different label.
;; - Interpose spaces between these characters, so label columns will line up with
;;   numeric matrix columns in the end.
;; - Convert each inner vector to a single string--a row of characters in the output.
;; - Interpose newlines and left padding on each such string, because the matrix output
;;   may be preceded by row labels.
;; - Add the same padding to the first line, without newline, and add a final newline.
;; - Convert the whole outer vector of strings to one large string.
;; (Maybe there's a way to do more of this with cl-format.)
;; Note: In cl-format, @ says to insert padding on left, v says to replace
;; the v with the next argument before processing the one after it.
(defn format-top-labels
  "ADD DOCSTRING"
  [labels intercolumn-width left-pad-width]
  (let [label-height (max-strlen labels)
        initial-pad (cl-format nil "~V@a" left-pad-width "")
        interline-pad (cl-format nil "~%~a" initial-pad)
        intercolumn-pad (cl-format nil "~va" intercolumn-width "")]
    (apply str                                           ; make the whole thing into a big string
           (conj (vec                                    ; add final newline
                   (cons initial-pad                     ; add initial spaces to first line
                         (interpose interline-pad        ; add newlines and initial spaces to each line except the first
                                    (map #(apply str %)  ; concatenate each inner vector to a string
                                         (map #(interpose intercolumn-pad %)  ; add spaces between chars from each column
                                              (mx/transpose                   ; (cheating by using a numeric matrix op, but convenient)
                                                            (collcolls-to-vecvecs    ; convert to Clojure vector of vector, which will be understood by core.matrix
                                                                                  (map #(cl-format nil "~v@a" label-height %) ; make labels same width (transposed: height)
                                                                                       labels)))))))) 
                 "\n"))))

(defn format-mat-with-row-labels
  "ADD DOCSTRING"
  [pv-mat labels nums-width]
  (let [labels-width (max-strlen labels)
        nums-widths (repeat (count labels) (+ 1 nums-width))] ; we'll need a list of repeated instances of nums-width
    (map (fn [row label]
           (cl-format nil "~v@a ~{~vf~}~%" 
                      labels-width label 
                      (interleave nums-widths row))) ; Using v to set width inside iteration directive ~{~} requires repeating the v arg
         pv-mat labels)))

(defn format-matrix-with-labels
  "Format a matrix mat with associated row and column labels into a string
  that could be printed prettily.  row-labels and col-labels must be sequences
  of strings in index order, corresponding to indexes from 0 to n."
  [mat row-labels col-labels]
  (let [pv-mat (mx/matrix :persistent-vector mat)
        nums-width (+ 0 (max-strlen 
                          (map #(cl-format nil "~f" %) 
                               (apply concat pv-mat))))
        left-pad-width (+ nums-width (max-strlen row-labels))]
      (apply str
             (concat
               (format-top-labels col-labels nums-width left-pad-width)
               (format-mat-with-row-labels pv-mat row-labels nums-width)))))

(defn format-nn-stru
  "Format the matrix in nn-stru with associated row, col info into a string
  that would be printed prettily."
  [nn-stru mat-key]
  (let [labels (map name (map :id (:node-vec nn-stru))) ; get ids in index order, convert to strings.  [or: (sort-by val < (:id-to-idx nn-stru))]
        pv-mat (mx/matrix :persistent-vector (mat-key nn-stru))] ; "coerce" to Clojure vector of Clojure (row) vectors
    (format-matrix-with-labels pv-mat labels labels)))

(defn pprint-nn-stru
  "Pretty-print the matrix in nn-stru with associated row, col info."
  [nn-stru mat-key]
  (print (format-nn-stru nn-stru mat-key)))

(defn dotformat-nn-stru
  "Format matrix in nn-stru with associated row, col info, like
  the output of format-nn-stru, but with '0.0' replaced by dot."
  [nn-stru mat-key]
  (clojure.string/replace (format-nn-stru nn-stru mat-key) 
                          #"\b0\.0\b"  " . ")) ; \b matches word border. Dot escaped so only matches dots.

(defn dotprint-nn-stru
  "Pretty-print the matrix in nn-stru with associated row, col info,
  replacing zeros with dots, so that it's easy to distinguish zeros
  from other values."
  [nn-stru mat-key]
  (print (dotformat-nn-stru nn-stru mat-key)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; OTHER UTILITIES FOR DATA STRUCTURES DEFINED ABOVE

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
;(defn alt-pair-ids
;  "Return sequence of pairs of :id fields of objects from sequence prs of pairs."
;  [prs]
;  (sort  ; does the right thing with pairs of keywords
;    (map (fn [[p1 p2]] 
;           [(:id p1) (:id p2)])
;         prs)))

;; MOVE TO SEPARATE FILE/NS
(defn symmetric?
  "Returns true if matrix is symmetric, false otherwise."
  [mat]
  (and (mx/square? mat)
       (every?  (fn [[i j]] (= (mx/mget mat i j) (mx/mget mat j i)))
         (let [dim (first (mx/shape mat))]
           (for [i (range dim)
                 j (range dim)
                 :when (> j i)] ; no need to test both i,j and j,i since we do both at once. always true for (= j i).
             [i j])))))
