;;;; functions for comparison with output from POPCO 1
(ns popco.test.popco1comp
  (:use ;[popco.nn.nets :as nn]
        ;[clojure.pprint :only [cl-format]]
        ;[clojure.data :as da]
        [clojure.set :as st]
        [popco.nn.pprint :as pp]
        [utils.general :as ug])
  ;(:require [clojure.core.matrix :as mx]
  ;          [clojure.string :as string])
)

(declare person-analogy-net-matches-popco1 sorted-links-for-popco1-comparison 
         links-for-popco1-comparison upper-case-link same-nodes?  same-weights?
         matched-and-unmatched-links semantic-link-matches?  all-links-match?)

(defn person-analogy-net-matches-popco1
  "High-level convenience function that compares an analogy net listing
  from-popco1 generated by popco1's list-constraints-for-popco2-comparison
  with the the analogy net in person pers.  Returns true if the popco 2 and
  popco 1 analogy nets, false otherwise."
  [pers from-popco1]
  (all-links-match? 
    (matched-and-unmatched-links 
      (sorted-links-for-popco1-comparison (:analogy-net pers) (:analogy-mask pers))
      from-popco1)))

(defn sorted-links-for-popco1-comparison
  "Generates an alphabetically sorted sequence of links in a form useful for 
  comparison with output of popco1's list-constraints-for-popco2-comparison 
  function.  Links have the form generated by links-for-popco-comparison.
  Example usage:
  (pprint (sorted-links-for-popco1-comparison a) (clojure.java.io/writer \"yo.txt\"))"
  [nnstru mask]
  (sort pp/compare-links
        (links-for-popco1-comparison nnstru mask)))

(defn links-for-popco1-comparison
  "Generates a sequence of links in a form useful for comparison with output
  of popco1's list-constraints-for-popco2-comparison function.  Link 
  reprsentations have the form generated by popco.nn.pprint/list-links, but 
  links are also normalized: ids uppercased, with the two ids alpha ordered 
  within each link representation.  Example usage:
  (pprint (links-for-popco1-comparison a) (clojure.java.io/writer \"yo.txt\"))"
  [nnstru mask]
  (map (comp pp/normalize-link upper-case-link)
       (pp/list-links nnstru mask)))

(defn upper-case-link
  "Given a representation of a link as 
     [node-id-keyword node-id-keyword weight]
  returns a represention that's similar, but with uppercased keyword names."
  [[id1 id2 wt1 wt2]]
  [(ug/upper-case-keyword id1) (ug/upper-case-keyword id2) wt1 wt2])

(defn same-nodes?
  [[id1a id1b] [id2a id2b]]   ; ignores additional elements within each argument
  (and (identical? id1a id2a)
       (identical? id1b id2b)))

(defn same-weights?
  [[_ _ wt1a wt1b][_ _ wt2a wt2b]]
  (and (== wt1a wt2a)
       (== wt1b wt2b)))

;; Don't use clojure.data/diff for this; it recurses into vectors and produces unintelligible results.
(defn matched-and-unmatched-links
  "NEED DOCSTRING"
  [links1 links2]
  (let [links1-set (set links1)
        links2-set (set links2)
        maybe-missing-from-links1 (st/difference links2-set links1-set)
        maybe-missing-from-links2 (st/difference links1-set links2-set)

        ;; links that are identical in both seqs
        same-links (sort pp/compare-links (st/intersection links1-set links2-set))

        ;; links between same nodes, but with different weights
        same-ids-diff-wts (sort #(pp/compare-links (first %1) (first %2)) ; since id pairs are same in first and second, just use one of them
                                (for [l1 maybe-missing-from-links2
                                      l2 maybe-missing-from-links1
                                      :when (same-nodes? l1 l2)]
                                  [l1 l2]))

        ;; links in the second seq but not the first
        missing-from-links1 (sort pp/compare-links 
                                  (st/difference maybe-missing-from-links1 
                                                 (set (map second same-ids-diff-wts))))

        ;; links in the first seq but not the second
        missing-from-links2 (sort pp/compare-links
                                  (st/difference maybe-missing-from-links2 
                                                 (set (map first same-ids-diff-wts))))]
    {:same same-links 
     :diffwts same-ids-diff-wts 
     :notin1 missing-from-links1 
     :notin2 missing-from-links2}))

(defn semantic-link-matches?
  "Test whether two links, one generated in popco 2, and generated in popco 1,
  are the same semantic links, i.e. links to the semantic node, with id 
  :SEMANTIC.  The single argument should be a 2-element sequence containing
  a popco 2 link representation followed by a popco 1 link representation.
  Link representations have the form generated by links-for-popco-comparison 
  in popco 2 or by list-constraints-for-popco2-comparison in popco 1.
  Since popco 2 uses unidirectional semantic->othernode links, while popco 1
  uses bidirectional, symmetric links to the semantic node, the test requires
  that the first link weight be 0.0 in the popco 2 link but any value in the 
  popco 1 link.  The latter value should be equal to the second link weight 
  values in both the popco 2 and popco 1 links."

  [[[popco2-id1 popco2-id2 popco2-wt1 popco2-wt2] 
    [popco1-id1 popco1-id2 popco1-wt1 popco1-wt2]]]

  (and (identical? popco2-id1 popco1-id1)
       (identical? popco2-id2 :SEMANTIC)
       (identical? popco1-id2 :SEMANTIC)
       (== popco2-wt1 0.0)
       (== popco2-wt2 popco1-wt1 popco1-wt2)))

(defn all-links-match?
    "Given a Clojure map whose structure is what's produced by 
    matched-and-unmatched-links, tests whether each link from the popco 2 side
    matches the corresponding link from the popco 1 side.  In particular, this
    function checks that the :notin* fields are empty, and that each pair of 
    links in the :diffwts field satisfies semantic-link-matches?."
    [{same-links :same 
     same-ids-diff-wts :diffwts 
     missing-from-links1 :notin1 
     missing-from-links2 :notin2}]
  (and (empty? missing-from-links1)
       (empty? missing-from-links2)
       (every? semantic-link-matches? same-ids-diff-wts)))
