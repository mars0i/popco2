(ns popco.core.acme
  (:use popco.core.lot
        [clojure.pprint :only [cl-format]])
  (:import [popco.core.lot Propn Pred Obj]
           [popco.core.nn AnalogyNetStru])
  (:require [utils.general :as ug]
            [popco.core.nn :as nn]
            [clojure.core.matrix :as mx]
            [clojure.string :as string])
  (:gen-class))

;; SEE acme.md for an overview of what's going on in this file.

;;; NOTE for the analogy net we probably don't really need a link
;;; matrix that just represents the existence of links, strictly speaking, 
;;; because there are no zero-weight links.
;;; For the belief network, however, we need to allow zero-weight links,
;;; so a link matrix will be needed.

(def pos-link-increment 0.1)
(def neg-link-value -0.2)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS TO BE MOVED TO ONE OR MORE SEPARATE FILES/NSes

;; Moved to my personal versions of core.matrix, vectorz-clj:
;(defn comp-sym?
;  "Returns true if matrix is symmetric, false otherwise."
;  [m]
;  (and (mx/square? m)
;       (every? (fn [[i j]] (= (mx/mget m i j) (mx/mget m j i)))
;               (let [dim (first (mx/shape m))]
;                 (for [i (range dim)
;                       j (range dim)
;                       :when (> j i)] ; no need to test both i,j and j,i since we do both at once. always true for (= j i).
;                   [i j])))))
;
;(defn rec-sym?
;  [m]
;  (let [dim (first (mx/shape m))] ; 1 past the last valid index
;    (letfn [(f [m i j]
;              (cond 
;                (>= i dim) true  ; got all the way through--it's symmetric
;                (>= j dim) (recur m (+ 1 i) (+ 2 i)) ; got through j's--start again with new i
;                (= (mx/mget m i j) 
;                   (mx/mget m j i)) (recur m i (inc j)) ; same, so check next pair
;                :else false))] ; not same, not symmetric. we're done.  
;      (f m 0 0))))

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
;; Make weight matrix representing positive and negative link weights

; (defn add-pos-wts-to-mat!
;   "ADD DOCSTRING"
;   [mat fams indexes increment]
;   (doseq [fam fams]
;     (doseq [itm1 fam           ; fam is a list of map-pairs
;             itm2 fam]          ; we want all poss ordered pairs
;       (when-not (= itm1 itm2)  ; except duplicates
;         (let [i (indexes (:id itm1)) 
;               j (indexes (:id itm2))]
;           ;(cl-format true "~%setting link between ~s, ~s at ~s ~s~%" (:id itm1) (:id itm2) i j) ; DEBUG
;           (mx/mset! mat i j (+ increment (mx/mget mat i j)))))))
;   mat)

; ;; IF THIS WORKS, CONSIDER REWRITING ADD-POSS-WTS-TO-MAT! AROUND IT (THIS IS MORE ABSTRACT)
; ;; TODO: Check that there should be no summing.
; ;; Check whether there could be summing, even.
; ;; Revise this as necessary, since at present, it sums.
; (defn add-neg-wts-to-mat!
;   "ADD DOCSTRING"
;   [mat idx-fams wt-val]
;   (doseq [fam idx-fams]
;     (doseq [i fam           ; TODO fam is a list of map-pairs
;             j fam]          ; TODO we want all poss ordered pairs
;       (when-not (= i j)  ; except duplicates TODO IS THIS RIGHT?
;         (mx/mset! mat i j (+ wt-val (mx/mget mat i j))))))
;   mat)

(defn add-wts-to-mat!
  "ADD DOCSTRING"
  [mat idx-fams wt-val op]
  (doseq [fam idx-fams]
    (doseq [i fam
            j fam]
      (when-not (= i j)
        (mx/mset! mat i j (op wt-val (mx/mget mat i j))))))
  mat)

(defn add-pos-wts-to-mat!
  "Add weights of value wt-val to matrix mat between all nodes with indexes in the same
  subseq of idx-fams.  idx-fams should be a seq of seqs of indexes from mapnodes related via
  language-of-thought relationships.  This procedure implements ACME's rule that weights
  on such links sum--i.e. if two nodes are linked multiple times, their link weight will
  be a multiple of wt-val."
  [mat idx-fams wt-val]
  (add-wts-to-mat! mat idx-fams wt-val +))

(defn- identity-if-zero
  "Returns new-val unchanged if old-val is zero.  If old-val is non-zero, throws an exception."
  [new-val old-val]
  (if (zero? old-val)
    new-val
    (throw (Exception. (format "Trying to overwrite nonzero weight.")))))

;; TODO: TEST ME - is result correct?
(defn add-neg-wts-to-mat!
  "Add weights of value wt-val to matrix mat between all nodes with indexes in the same
  subseq of idx-fams.  idx-fams should be a seq of seqs of indexes from mapnodes that are 
  competing because they all share a 'side'.  This procedure implements ACME's rule that negative
  on such links do not sum:  This function will not set the value of a weight that has already 
  been set to something other than zero (an exception will be thrown)."
  [mat idx-fams wt-val]
  (add-wts-to-mat! mat idx-fams wt-val identity-if-zero))

(defn matched-idx-fams
  [fams id-to-idx]
  (let [f (comp id-to-idx :id)]
    (map #(map f %) fams)))

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

(defn assoc-ids-to-idx-nnstru-map
  [nnstru]
  (assoc nnstru 
         :ids-to-idx (make-two-ids-to-idx-map (:node-vec nnstru) 
                                              (:id-to-idx nnstru))))

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
(defn make-analogy-net-core
  "Given a sequence of data on individual nodes, returns a clojure map with 
  these entries:
  :node-vec -    A Clojure vector of data providing information about the meaning
                 of particular neural net nodes.  The indexes of the data items
                 correspond to indexes into activation vectors and rows/columns
                 of weight matrices.  This vector may be identical to the sequence
                 of nodes passed in.
  :id-to-idx -   A Clojure map from ids of the same data items to integers, 
                 allowing lookup of a node's index from its id.
  :pos-wt-mat -  A core.matrix square matrix with dimensions equal to the number of
                 nodes, with all elements initialized to 0.0.  This will represent
                 positively weighted links.
  :neg-wt-mat -  A core.matrix square matrix with dimensions equal to the number of
                 nodes, with all elements initialized to 0.0.  This will represent
                 negatively weighted links."
  [node-seq]
  {:node-vec (vec node-seq)
   :id-to-idx (make-id-to-idx-map (map :id node-seq)) ; index order will be same as node-seq's order
   :pos-wt-mat (make-wt-mat (count node-seq))
   :neg-wt-mat (make-wt-mat (count node-seq))})

(defn make-analogy-net-stru
  "Make an ACME analogy neural-net structure, i.e. a structure that represents an ACME analogy constraint
  satisfaction network.  This is a standard neural-net structure produced by make-analogy-net-core (q.v.)
  with these changes that are specific to an analogy network:
  - Field :ids-to-idx is added.  This does roughly the same thing as :id-to-idx. The latter
    maps mapnode ids to indexes into the node vector (or rows, or columns of the matrices).
    :ids-to-idx, by contrast, maps vector pairs containing the ids of the two sides (from
    which the mapnode id is constructed).  This is redundant information, but convenient.
  - The :pos-wt-mat and :neg-wt-mat matrices, which are initialized with zeros by make-analogy-net-core, 
    are now given some nonzero weights--positive weights in the first, negative weights in the 
    second.  These weights follow ACME's rules for analogy networks.  Note that this is an 
    imperative operation using core.matrix functions; no new matrices are created."
  [pset1 pset2 pos-increment neg-increment]
  (let [fams (match-propn-components (match-propns pset1 pset2))
        nnstru-map (assoc-ids-to-idx-nnstru-map 
                  (make-analogy-net-core 
                    (distinct (flatten fams)))) ] ; flatten here assumes map-pairs aren't seqs
    (add-pos-wts-to-mat! (:pos-wt-mat nnstru-map) 
                         (matched-idx-fams fams (:id-to-idx nnstru-map)) 
                         pos-increment)
    (add-neg-wts-to-mat! (:neg-wt-mat nnstru-map) 
                         (competing-mapnode-idx-fams (:ids-to-idx nnstru-map)) 
                         neg-increment)
    (nn/map->AnalogyNetStru nnstru-map)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS FOR DISPLAYING MATRICES, NN-STRUS WITH LABELS

;; TODO: Note that core.matrix/pm does some pretty-printing of matrices.
;;       Consider using it.  Or adding to it.

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
;; NOTE: In cl-format, @ says to insert padding on left, v says to replace
;; the v with the next argument before processing the one after it.
(defn format-top-labels
  "ADD DOCSTRING"
  [labels intercolumn-width left-pad-width sep]
  (let [label-height (max-strlen labels)
        initial-pad (cl-format nil "~v@a~a~v@a" left-pad-width "" sep intercolumn-width "") ; @ causes pad spaces on left, vs default on right
        interline-pad (cl-format nil "~a~%~a" sep initial-pad)
        intercolumn-pad (cl-format nil "~a~va" sep intercolumn-width "")]
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
                 sep
                 "\n"))))

(defn format-mat-with-row-labels
  "ADD DOCSTRING"
  [pv-mat labels nums-width sep]
  (let [num-labels (count labels)
        labels-width (max-strlen labels)
        nums-widths (repeat num-labels (+ 1 nums-width)) ; we'll need a list of repeated instances of nums-width
        seps (repeat num-labels sep)]                    ; and separators
    (map (fn [row label]
           (cl-format nil "~v@a~a ~{~vf~a~}~%" 
                      labels-width label sep
                      (interleave nums-widths row seps))) ; Using v to set width inside iteration directive ~{~} requires repeating the v arg
         pv-mat labels)))

; This is rather slow, but fine if you don't need to run it very often.
(defn format-matrix-with-labels
  "Format a matrix mat with associated row and column labels into a string
  that could be printed prettily.  row-labels and col-labels must be sequences
  of strings in index order, corresponding to indexes from 0 to n.  If a string
  is provided as an additional, optional sep argument, it will be used to 
  separate columns.  For example, you can use a string containing a comma to 
  generate csv output."
  ([mat row-labels col-labels] (format-matrix-with-labels mat row-labels col-labels "")) ; default to empty string as column separator
  ([mat row-labels col-labels sep]
   (let [pv-mat (mx/matrix :persistent-vector mat) ; "coerce" to Clojure vector of Clojure (row) vectors
         nums-width (+ 0 (max-strlen 
                           (map #(cl-format nil "~f" %)   ; REWRITE WITH mx/longest-nums
                                (apply concat pv-mat))))
         left-pad-width (max-strlen row-labels)]
     (apply str
            (concat
              (format-top-labels col-labels nums-width left-pad-width sep)
              (format-mat-with-row-labels pv-mat row-labels nums-width sep))))))

(defn format-nnstru
  "Format the matrix in nnstru with associated row, col info into a string
  that would be printed prettily.  Display fields are fixed width, so this
  can also be used to output a matrix to a file for use in other programs."
  ([nnstru mat-key] (format-nnstru mat-key ""))
  ([nnstru mat-key sep]
   (let [labels (map name (map :id (:node-vec nnstru))) ; get ids in index order, convert to strings.  [or: (sort-by val < (:id-to-idx nnstru))]
         mat (get nnstru mat-key)]
     (format-matrix-with-labels mat labels labels sep))))

(defn pprint-nnstru
  "Pretty-print the matrix in nnstru with associated row, col info."
  [nnstru mat-key]
  (print (format-nnstru nnstru mat-key)))

(defn dotformat
  "Given a string for display of a matrix (or anything), replaces
  '0.0's with ' . 's."
  [matstring]
  (clojure.string/replace matstring #"\b0\.0\b"  " . ")) ; \b matches word border. Dot escaped so only matches dots.

(defn dotformat-nnstru
  "Format matrix in nnstru with associated row, col info, like
  the output of format-nnstru, but with '0.0' replaced by dot.
  Display fields are fixed width, so this can also be used to output
  a matrix to a file for use in other programs."
  [nnstru mat-key]
  (dotformat (format-nnstru nnstru mat-key)))

(defn dotprint-nnstru
  "Pretty-print the matrix in nnstru with associated row, col info,
  replacing zeros with dots, so that it's easy to distinguish zeros
  from other values."
  [nnstru mat-key]
  (print (dotformat-nnstru nnstru mat-key)))

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

