(ns popco.nn.settle
  (:use clojure.core.matrix)
  (:require [popco.nn.nets :as nn]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; NOTES

;; References:
;; 1. network.lisp in POPCO, which is based on Thagard's ACME network.lisp.
;; 2. "HT": Holyoak & Thagard 1989, "Analogue Retrieval by Constraint
;; Satisfaction", Cognitive Science 13, pp. 295-355. See pp. 313, 315.
;; 3. "MR": Abrams 2013, "A Moderate Role for Cognitive Models in Agent-Based 
;; Modeling of Cultural Change", Complex Adaptive Systems Modeling 2013.
;; Note that the latter has errors.
;; 4. Grossberg. S. (1978). A theory of visual coding, memory, and development.
;; In E.L.J. Leeuwenberg & H.F.J. Buffart (Eds.), Formal theories of visual 
;; perception. New York: Wiley.

;; Note the distinction in clojure.core.matrix between:
;; emul: Multiply together corresponding elements of matrices,
;;       which should have the same shape.
;; mul:  Same as emul.
;; mmul: Regular matrice multiplication A * B:
;;       emul (see above) each row i of A with each column j of B,
;;       summing the result each time to produce element <i,j> of
;;       the result matrix.  (For vectors, this is inner product,
;;       with A as a row vector and B as a column vector.)
;;
;; emap maps a function over each element of a matrix to produce a new
;;      matrix.

;; Convention: Vector names are all-lower-case.  Matrices have initial cap
;; in each component of the name.

(declare clip-to-extrema dist-from-max dist-from-min)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Network settling (with Grossberg algorithm)

;; NOTE: 
;;
;; The Grossberg algorithm does no normalization in anything like
;; the probabilistic sense: That is, outputs are outputs; they're not scaled
;; relative to other signals coming into the same node:  The only "averaging"
;; comes from the weighting across links due to the network, and the scaling
;; by distance from max and min.  Moreover, the link weights are absolute
;; numbers; they are not themselves scaled relative to other link weights.
;; (In essence, it's the job of the neural net settling process to do 
;; something analogous to probabilistic normalization.)
;;
;; (Qualification: For the proposition network there is a kludgey method of
;; scaling negative links relative to positive links since there are more
;; negative links: We just give the negative links a lower abs weight.)
;;
;; WHAT THIS MEANS is that you can effectively "remove" links from the 
;; network simply by masking the input vector, forcing some values to zero.
;; If a node sends no activation over the wires, then it's just as if links
;; from that node had weight zero--i.e. as if these links didn't exist.
;; Nothing *else* about the network needs to change.
;;
;; AND this means that since the analogy network has certain links to a 
;; map node if and only if the node exists, and never adds or removes links
;; to a map node once it exists, we can create the analogy network once,
;; including all map nodes possible given the set of propositions in the
;; two analog structures.  Then we can "remove" map nodes and their
;; links from the network simply by zeroing the map node activations in
;; the input vector.  Or rather, we can add map nodes *and their links*
;; to the analogy network simply by beginning to put nonzero activations 
;; in the corresponding map nodes.  In this way, different persons can
;; have effectively different analogy networks, corresponding to their
;; different repertoires of propositions, while using a single set of
;; analogy network weight matrices (which needn't slow down run time
;; due to being modified later).
;;
;; Note: The proposition networks, by contrast, can't be treated this way,
;; because they acquire new links between old nodes.

;; SHOULD I USE add-product here for the inner addition of emuls?
;; 
;; NOTE For This way of doing matrix multiplication using (mmul <matrix> <vector>),
;; <vector> is 1D and is treated as a column vector.  This means that the weight
;; at index i,j represents the directional link from node j to node i, since j is
;; the column (input) index, and i is the row index.  (Doesn't matter for symmetric
;; links, since for the there will be identical weights at i,j and j,i, but matters
;; for assymetric, directional links.)
(defn next-activns 
  "Calculate a new set of activations for nodes starting from the current
  activations in vector activns, using network link weights in constraint
  network nnet to update activations from neighbors using the Grossberg (1978) 
  algorithm as described in Holyoak & Thagard's (1989) \"Analogue Retrieval
  by Constraint Satisfaction\"."
  [activns nnet]
  (let [pos-activns (emap nn/posify activns)] ; Negative activations are ignored as inputs.
    (emap clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
          (add (emul 0.9 activns)                            ; Sum into decayed activations ...
               (emul (mmul (nn/pos-wt-mat nnet) pos-activns) ; positively weighted inputs scaled by
                     (emap dist-from-max activns))           ;  inputs' distances from 1, and
               (emul (mmul (nn/neg-wt-mat nnet) pos-activns) ; negatively weighted inputs scaled by
                     (emap dist-from-min activns))))))       ;  inputs' distances from -1.

(defn settle-analogy-nets
  "Currently a noop; returns the population unchanged."
  [popn]
  popn)

(defn settle-propn-nets
  "Currently a noop; returns the population unchanged."
  [popn]
  popn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; scalar functions

(defn clip-to-extrema
  "Returns -1 if x < -1, 1 if x > 1, and x otherwise."
  [x]
  (max -1 (min 1 x)))

(defn dist-from-max
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn < 0."
  [activn]
  (- 1 activn))

(defn dist-from-min 
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn > 0."
  [activn]
  (+ 1 activn)) ; HT1989 p. 313 says: (- activn -1)

