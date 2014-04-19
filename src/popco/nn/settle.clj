(ns popco.nn.settle
  (:use clojure.core.matrix)
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]))

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

(declare next-activns settle-net settle-analogy-net settle-propn-net
         update-propn-wts-from-analogy-activns update-person-nets update-nets
         clip-to-extrema dist-from-max dist-from-min)

(def settling-iters 5)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Network settling (with Grossberg algorithm)
;; See settle.md for notes.

(defn update-nets
  "Implements a single timestep's (tick's) worth of network settling and updating of the
  proposition network from the analogy network.  Returns the population in its new state
  after these processes have been performed."
  [persons]
  (map update-person-nets persons))

(defn update-person-nets
  "Perform one tick's updating of the networks of a single person."
  [pers]
  (-> pers
    (settle-analogy-net  settling-iters) ; is this step necessary??
    (update-propn-wts-from-analogy-activns)
    (settle-propn-net    settling-iters)))

(defn update-propn-wts-from-analogy-activns
  "Currently a noop; returns the person unchanged."
  [pers]
  pers)

(defn settle-analogy-net
  "Return person pers with its analogy net updated by settle-iters of settling."
  ([pers] 
   (settle-analogy-net pers 0))
  ([pers iters]
   (settle-net pers :analogy-activns :analogy-net iters)))

(defn settle-propn-net
  "Return person pers with its propn net updated by settle-iters of settling."
  ([pers] 
   (settle-propn-net pers 0))
  ([pers iters]
   (settle-net pers :propn-activns :propn-net iters)))

(defn settle-net
  "Return person pers with the net selected by net-key and activns-key updated
  by iters rounds of settling."
  [pers net-key activns-key iters]
  (assoc pers activns-key
         (take iters
               (iterate (partial next-activns (net-key pers))
                        (activns-key pers)))))

;; SHOULD I USE add-product HERE FOR THE INNER ADDITION OF EMULS?
;; 
;; NOTE: For This way of doing matrix multiplication using (mmul <matrix> <vector>),
;; <vector> is 1D and is treated as a column vector.  This means that the weight
;; at index i,j represents the directional link from node j to node i, since j is
;; the column (input) index, and i is the row index.  (Doesn't matter for symmetric
;; links, since for the there will be identical weights at i,j and j,i, but matters
;; for assymetric, directional links.)
(defn next-activns 
  "Calculate a new set of activations for nodes starting from the current
  activations in vector activns, using network link weights in constraint
  network net to update activations from neighbors using the Grossberg (1978) 
  algorithm as described in Holyoak & Thagard's (1989) \"Analogue Retrieval
  by Constraint Satisfaction\"."
  [net activns]
  (let [pos-activns (emap nn/posify activns)] ; Negative activations are ignored as inputs.
    (emap clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
          (add (emul 0.9 activns)                            ; Sum into decayed activations ...
               (emul (mmul (nn/pos-wt-mat net) pos-activns) ; positively weighted inputs scaled by
                     (emap dist-from-max activns))           ;  inputs' distances from 1, and
               (emul (mmul (nn/neg-wt-mat net) pos-activns) ; negatively weighted inputs scaled by
                     (emap dist-from-min activns))))))       ;  inputs' distances from -1.

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
  (- 1.0 activn))

(defn dist-from-min 
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn > 0."
  [activn]
  (inc activn)) ; HT1989 p. 313 says: (- activn -1).  inc seems to be slightly faster than (+ 1 ...)
