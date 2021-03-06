;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;;;; FUNCTIONS FOR DISPLAYING MATRICES, NN-STRUS, ETC.
(ns popco.nn.pprint
  (:use [popco.nn.nets :as nn]
        [utils.general :as ug]
        [clojure.pprint :only [cl-format]])
  (:require [clojure.core.matrix :as mx]
            [clojure.string :as string]))

;; TODO: Note that core.matrix/pm does some pretty-printing of matrices.
;;       Consider using it.  Or adding to it.

(declare list-analogy-links list-propn-links list-links compare-links
         normalize-link list-nodes list-propn-nodes list-analogy-nodes
         list-both-nodes max-strlen collcolls-to-vecvecs format-vertical-top-labels
         format-mat-with-row-labels format-matrix-with-labels format-nn
         pprint-nn dotformat dotformat-nn dotprint-nn)

(defn pprint-activns
  "pretty-prints an alphabetically sorted sequence of node id/activn pairs."
  [net]
  (clojure.pprint/pprint (sort (vec (nn/id-activn-map net)))))

(defn list-analogy-links
  "ADD DOCSTRING"
  [pers]
  (list-links (:analogy-net pers)))

(defn list-propn-links
  "ADD DOCSTRING"
  [pers]
  (list-links (:propn-net pers)))

(defn list-links
  "Return a seq of all node pairs that are linked in nnstru's matrix.
  Each link is represented in the output as a seq [id1 id2 wt1 wt2],
  where id* are node ids, and wt* are link weights in either direction,
  i.e. wt1 = wt2 for bidirectional links (the usual case).
  BUGS: Actually just checks whether weights are nonzero; zero-weight
  links will be ignored."
  [nnstru]
  (let [mat (nn/wt-mat nnstru)
        mask (:mask nnstru)
        id-vec (:id-vec nnstru)
        num-nodes (count id-vec)]
    (for [i (range num-nodes)
          j (range i num-nodes)
          :when (and (<= 1.0 (mx/mget mask i)) ; <= rather than == because mask value for special nodes may be > 1
                     (<= 1.0 (mx/mget mask j))
                     (or (not (== (mx/mget mat i j) 0.0))   ; there is no not== function
                         (not (== (mx/mget mat j i) 0.0))))]
        [(id-vec i) (id-vec j) (mx/mget mat i j) (mx/mget mat j i)])))

(defn compare-links
  "A comparator function for use with sort on output of list-links. 
  Sorts by first node id from each argument, then by second node id within
  those that have the same first id. e.g.: (sort compare-links (list-links a))"
  [[id1a id1b] [id2a id2b]]  ; ignore args after first two
  (let [result (compare id1a id2a)]
    (if-not (= result 0)
      result
      (compare id1b id2b))))

(defn normalize-link
  "Given a representation of a link as [id1 id2 wt1 wt2], returns the same
  information in the same format, but with id1 and id2 in alphabetical order."
  [[id1 id2 wt1 wt2]]
  (if (> 0 (compare (string/upper-case id1) (string/upper-case id2)))
    [id1 id2 wt1 wt2]
    [id2 id1 wt1 wt2]))

(defn list-nodes
  [nnstru]
  (let [mat (nn/wt-mat nnstru)
        mask (:mask nnstru)
        id-vec (:id-vec nnstru)
        num-nodes (count id-vec)]
    (for [i (range num-nodes)
          :when (nn/node-unmasked? mask i)]
      (id-vec i))))

(defn list-propn-nodes
  [pers]
  (list-nodes (:propn-net pers)))

(defn list-analogy-nodes
  [pers]
  (list-nodes (:analogy-net pers)))

(defn list-both-nodes
  [pers]
  {:propn   (list-propn-nodes pers)
   :analogy (list-analogy-nodes pers)})

(defn max-strlen
  "Returns the maximum length of the strings contained in its argument."
  [strings]
  (apply max (map count strings)))

(defn collcolls-to-vecvecs
  "Converts a collection of collections to a vector of vectors."
  [coll]
  (vec (map #(vec %) coll)))

(defn pprint-nn
  "Pretty-print the matrix in nnstru with associated row, col info."
  [nnstru mat-key]
  (print (format-nn nnstru mat-key)))

(defn format-nn
  "Format the matrix in nnstru with associated row, col info into a string
  that would be printed prettily.  Display fields are fixed width, so this
  can also be used to output a matrix to a file for use in other programs."
  ([nnstru mat-key] (format-nn nnstru mat-key ""))
  ([nnstru mat-key sep]
   (let [labels (map name (map :id (:node-vec nnstru))) ; get ids in index order, convert to strings.  [or: (sort-by val < (:id-to-idx nnstru))]
         mat (mat-key nnstru)]
     (format-matrix-with-labels mat labels labels sep))))

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
              (format-vertical-top-labels col-labels nums-width left-pad-width sep)
              (format-mat-with-row-labels pv-mat row-labels nums-width sep))))))

(defn format-mat-with-row-labels
  "Format a matrix with labels on each row.  pv-mat is a core.matrix matrix
  of the persistent-vector type (i.e. a Clojure vector of [row] vectors).  Labels is
  a sequence of label strings, in order, one for each row.  nums-width is the desired
  maximum width to round number strings to.  sep is a string that can contain extra
  padding to put between columns.  This function is normally called from
  format-matrix-with-labels."
  ([pv-mat labels nums-width sep] (format-mat-with-row-labels pv-mat labels nums-width sep (max-strlen labels)))
  ([pv-mat labels nums-width sep labels-width]
   (let [num-labels (count labels)
         nums-widths (repeat num-labels (+ 1 nums-width)) ; we'll need a list of repeated instances of nums-width
         seps (repeat num-labels sep)]                    ; and separators
     (map (fn [row label]
            (cl-format nil "~v@a~a ~{~vf~a~}~%" 
                       labels-width label sep
                       (interleave nums-widths row seps))) ; Using v to set width inside iteration directive ~{~} requires repeating the v arg
          pv-mat labels))))

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
(defn format-vertical-top-labels
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

(defn remove-non-links 
  "Replace zero-weight links in a persistent-vector matrix with empty strings 
  (for Gephi).  Temporary: The right way to do it is to have a separate matrix 
  of good links."
  [pv-mat]
  (map (fn [row] 
         (map (fn [elem]
                (if (== elem 0) "" elem))
              row))
       pv-mat))

;; Delimiter must be ";" and there must be no whitespace after it.
;; Node names should not be quoted.
(defn format-nn-gephi-csv-mat
  "Format the matrix in nnstru (a proposition net or analogy net) with 
  associated row, col info into a string formatted as a Gephi CSV matrix--
  i.e. with delimited node label strings in the top row and left column, 
  and delimited edge weight numbers at intersecting rows and columns.  
  mat-key, if present is either function (or keyword) that selects the 
  appropriate matrix from the nnstru.  This would usually be one of the 
  functions wt-mat, pos-wt-mat, or neg-wt-mat from popco.nn.nets."
  ([nnstru] (format-nn-gephi-csv-mat nnstru nn/wt-mat))
  ([nnstru mat-key]
   (let [pv-mat (remove-non-links (mx/matrix :persistent-vector (mat-key nnstru))) ; "coerce" to Clojure vector of Clojure (row) vectors
         labels (map name (map :id (:node-vec nnstru))) ; get ids in index order, convert to strings.  [or: (sort-by val < (:id-to-idx nnstru))]
         sep ";"]
     (apply str
            (apply concat                     ; merge the rows into one long seq
                   (map #(conj (vec %) "\n")  ; add newline to the end of each row seq
                        (cons                 ; add top label row to data rows
                              (cons sep       ; upper left corner must be empty
                                    (interpose sep labels))
                              (map (fn [label row] (interpose sep (cons label row)))
                                   labels pv-mat))))))))

(defn dotformat
  "Given a string for display of a matrix (or anything), replaces
  '0.0's with ' . 's."
  [matstring]
  (string/replace matstring #"\b0\.0\b"  " . ")) ; \b matches word border. Dot escaped so only matches dots.

(defn dotformat-nn
  "Format matrix in nnstru with associated row, col info, like
  the output of format-nn, but with '0.0' replaced by dot.
  Display fields are fixed width, so this can also be used to output
  a matrix to a file for use in other programs."
  [nnstru mat-key]
  (dotformat (format-nn nnstru mat-key)))

(defn dotprint-nn
  "Pretty-print the matrix in nnstru with associated row, col info,
  replacing zeros with dots, so that it's easy to distinguish zeros
  from other values."
  [nnstru mat-key]
  (print (dotformat-nn nnstru mat-key)))

