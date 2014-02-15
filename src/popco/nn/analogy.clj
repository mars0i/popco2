;; analogy.clj
;; Functions for creating and working with AnalogyNets.
;; Further documentation on AnalogyNets can be read and maintained in net.clj
;; See lot.clj for documentation on Propns and their components.

(ns popco.nn.analogy
  (:use popco.core.lot
        [clojure.set :only [union]])
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]
            [clojure.core.matrix :as mx]
            [clojure.algo.generic.functor :as gf])
  (:import [popco.core.lot Propn Pred Obj]
           [popco.nn.nets AnalogyNet]))

;; SEE analogy.md for an overview of what's going on in this file.

;;; NOTE for the analogy net we probably don't really need a link
;;; matrix that just represents the existence of links, strictly speaking, 
;;; because there are no zero-weight links.
;;; For the belief network, however, we need to allow zero-weight links,
;;; so a link matrix will be needed.

(def pos-link-increment 0.1)
(def neg-link-value -0.2)

(declare make-analogy-net assoc-ids-to-idx-nn-map make-activn-vec make-wt-mat 
         match-propns propns-match?  match-propn-components match-propn-components-deeply
         make-mapnode-map make-propn-mn-to-mns make-propn-mn-to-fam-idxs alog-ids 
         make-two-ids-to-idx-map ids-to-mapnode-id ids-to-poss-mapnode-id add-wts-to-mat! add-pos-wts-to-mat! 
         add-neg-wts-to-mat!  matched-idx-fams competing-mapnode-fams 
         competing-mapnode-idx-fams args-match? identity-if-zero make-propn-to-analogues)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ALL STEPS - put it all together
;; ...

;; TODO I added the SPECIAL node, which seems to work, but have not caused links to it to be made e.g. for same predicates
;; The fields contain redundant information, but it's all information that can
;; be precomputed at initialization time, and that will only be read during
;; simulation run time.  Having the redundant, precomputed fields makes runtime
;; code simple and efficient.
(defn make-analogy-net
  "Make an ACME analogy neural-net structure, i.e. a structure that represents
  an ACME analogy constraint satisfaction network.  This is a standard 
  neural-net structure produced by make-nn-core (q.v.), with these changes that
  are specific to an analogy network:
  :pos-wt-mat - A core.matrix square matrix with dimensions equal to the number
                of nodes.  This will represent positively weighted links.
  :neg-wt-mat - A core.matrix square matrix with dimensions equal to the number
                of nodes.  This will represent negatively weighted links.
  :id-to-idx -   A Clojure map from ids of the same data items to integers, 
                 allowing lookup of a node's index from its id.
  :ids-to-idx - This does roughly the same thing as :id-to-idx. The latter
                maps mapnode ids to indexes into the node vector (or rows, or 
                columns of the matrices).  :ids-to-idx, by contrast, maps
                vector pairs containing the ids of the two sides (from which
                the mapnode id is constructed).  This is redundant information,
                but convenient.
  :propn-mn-to-fam-idxs - A map from ids of propn-mapnodes to sets of indexes of the
                associated component mapnodes, i.e. of the propn mapnode's 'family'.
  :propn-mn-to-ext-fam-idxs - A map from ids of propn-mapnodes to sets of indexes of the
                associated component mapnodes, components of argument propn-mapnodes, etc.
                all the say down--i.e. of the propn-mapnode's 'extended family'.
  :propn-to-analogues -  A map from ids of propns to ids of their analogues--i.e.
                of the propns that are the other sides of mapnodes."
  [propnseq1 propnseq2 pos-increment neg-increment]
  (let [propn-pairs (match-propns propnseq1 propnseq2)
        propn-pair-ids (map #(map :id %) propn-pairs)
        fams (match-propn-components propn-pairs)
        ext-fams (match-propn-components-deeply propn-pairs)
        node-seq (cons {:id :SPECIAL} (distinct (flatten fams))) ; flatten here assumes map-pairs aren't seqs
        num-nodes (count node-seq)
        nn-map (assoc-ids-to-idx-nn-map (nn/make-nn-core node-seq)) ; make node/indexes mappings
        id-to-idx (:id-to-idx nn-map)
        analogy-map (assoc nn-map
                           :pos-wt-mat (make-wt-mat num-nodes)  ; add zero matrices
                           :neg-wt-mat (make-wt-mat num-nodes)  ; ... to be filled below
                           :propn-mn-to-fam-idxs (make-propn-mn-to-fam-idxs id-to-idx fams)  ; TODO UNTESTED
                           :propn-mn-to-ext-fam-idxs (make-propn-mn-to-fam-idxs id-to-idx ext-fams) ; TODO UNTESTED
                           :propn-to-analogues (make-propn-to-analogues propn-pair-ids)) ] ; TODO UNTESTED
    (add-pos-wts-to-mat! (:pos-wt-mat analogy-map) 
                         (matched-idx-fams fams (:id-to-idx analogy-map)) 
                         pos-increment)
    (add-neg-wts-to-mat! (:neg-wt-mat analogy-map) 
                         (competing-mapnode-idx-fams (:ids-to-idx analogy-map)) 
                         neg-increment)
    (nn/map->AnalogyNet analogy-map)))


(defn assoc-ids-to-idx-nn-map
  [nn-map]
  (assoc nn-map 
         :ids-to-idx (make-two-ids-to-idx-map (:node-vec nn-map) 
                                              (:id-to-idx nn-map))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FUNCTIONS TO BE MOVED TO ONE OR MORE SEPARATE FILES/NSes

(defn make-activn-vec
  "Returns a core.matrix vector of length len, filled with zeros,
  to represent activation values of nodes.  Each person has its own 
  activation vector."
  [len]
  (mx/zero-vector len))

(defn make-wt-mat
  "Returns a core.matrix square matrix with dimension dim, filled with zeros."
  [dim]
  (mx/zero-matrix dim dim))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 1
;; Find out which propositions can be paired up, i.e. the isomorphic ones.

;; Note that order within pairs matters:  It preserves the distinction
;; between the two analogue structures.
(defn match-propns
  "Returns a (lazy) sequence of 2-element sequences, each containing two 
  propositions that match according to propns-match?.  These are propositions 
  that are isomorphic and can be used to construct map nodes."
  [propnseq1 propnseq2]
  (for [p1 propnseq1
        p2 propnseq2
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

;; NOTE The functions match-propn-components (with the functions it causes
;; to be called: match-componens-of-propn-pair and make-mapnode-map)
;; doesn't try to perform matching on args of propns that appear as args
;; of propns.  Rather, we *just* match the propns themselves when they appear
;; in argument place.  (Note that we *did* need to recurse on args to decide which
;; propns matched which propns [that was the desired behavior in H&T1989, though not the
;; actual behavior in ACME, nor what was described in the p. 314 specification--
;; but it was what happened in POPCO 1 and was what "Moderate Role" described].  This
;; recursive proposition matching operation is performed by match-propns.) However, at 
;; this stage, once match-propns has generated a seq of matched propns, we should assume 
;; that we are only looking at propn pairs that are known to match in this deep way.
;; That doesn't mean that weights get calculated from matchings that have to do with 
;; what's inside the propns that are args.  The H&T1989 algorithm does *not* do this, 
;; nor did POPCO 1.

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
  (letfn [(match-components-of-propn-pair [[p1 p2]]
            (cons (make-mapnode-map p1 p2)                       ; we already know the propns match
                  (cons (make-mapnode-map (:pred p1) (:pred p2)) ; predicates always match if the propns matched
                        (map make-mapnode-map (:args p1) (:args p2)))))]
          (map match-components-of-propn-pair pairs)))

(defn match-propn-components-deeply
  "Exactly like match-propn-components (q.v.), but recurses into propn arguments,
  returning a sequence of extended families of mapped-pairs, i.e. each element of
  the top-level sequence is a flat sequence containing mapnodes made of propns, preds, 
  objs of both toplevel propns and argument propns (recursively)."
  [pairs]
  (letfn [(match-components-of-propn-pair-deeply [[p1 p2]]
            (cons (make-mapnode-map p1 p2)                       ; we already know the propns match
                  (cons (make-mapnode-map (:pred p1) (:pred p2)) ; predicates always match if the propns matched
                        ;; chose which function to apply to arg pair depending on whether propns:
                        (map (fn [arg1 arg2]
                               (if (propn? arg1)  ; assume already matched, so only need to check one arg
                                 (match-components-of-propn-pair-deeply [arg1 arg2])
                                 (make-mapnode-map arg1 arg2)))
                             (:args p1) (:args p2)))))]
    (map flatten
      (map match-components-of-propn-pair-deeply pairs))))

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

(defn make-propn-mn-to-mns
  "Create a Clojure map from propn-mapnode ids to sets containing ids of the 
  associated propn and component mapnodes."
  [fams]
  (into {} 
        (map #(ug/seq-to-first-all-map (map :id %)) 
             fams)))

(defn make-propn-mn-to-fam-idxs
  "Create a Clojure map from propn-mapnode ids to sets containing ids of the 
  associated propn and component indexes."
  [id-to-idx fams]
  (gf/fmap #(map id-to-idx %) (make-propn-mn-to-mns fams)))

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

(defn ids-to-poss-mapnode-id
  "Given two id keywords and a map from ids to indexes, constructs and returns 
  a corresponding mapnode id, or nil if the id has no index."
  [id1 id2 id-to-idx]
  (let [mn-id (ids-to-mapnode-id id1 id2)]
    (if (id-to-idx mn-id)
      mn-id
      nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 4
;; Make weight matrix representing positive and negative link weights

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

(defn add-neg-wts-to-mat!
  "Add weights of value wt-val to matrix mat between all nodes with indexes in the same
  subseq of idx-fams.  idx-fams should be a seq of seqs of indexes from mapnodes that are 
  competing because they all share a 'side'.  This procedure implements ACME's rule that negative
  on such links do not sum:  This function will not set the value of a weight that has already 
  been set to something other than zero (an exception will be thrown)."
  [mat idx-fams wt-val]
  (add-wts-to-mat! mat idx-fams wt-val identity-if-zero))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; STEP 5 - Make other useful data structures

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

(defn make-propn-to-analogues
  "Given pairs of ids of matched propns, creates a map from ids of individual
  propns, to seq of ids of propns to which they match."
  [propn-pair-ids]
  (merge
    (gf/fmap #(vec (map second %)) (group-by first propn-pair-ids))
    (gf/fmap #(vec (map first %)) (group-by second propn-pair-ids))))

