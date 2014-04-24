(ns popco.nn.update
  (:use clojure.core.matrix)
  (:require [popco.nn.nets :as nn]
            [utils.general :as ug]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; NOTES
;;; See settle.md for more notes, including abbreviations, overview of
;;; algorithm, article references, tips, etc.

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

(declare next-activns settle-net settle-analogy-net settle-propn-net
         update-propn-wts-from-analogy-activns update-person-nets update-nets
         clip-to-extrema dist-from-max dist-from-min)

(def settling-iters 5)  ; default number of times to run through the settling algorithm in each tick

(def ^:const decay 0.9) ; amount to decay the old activn before adding inputs from other nodes

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Network settling (with Grossberg algorithm)

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
  "Return person pers with its analogy net updated by 1 or more iters of settling."
  ([pers] 
   (settle-analogy-net pers 1))
  ([pers iters]
   (settle-net pers :analogy-net :analogy-activns :analogy-mask iters)))

(defn settle-propn-net
  "Return person pers with its propn net updated by 1 or more iters of settling."
  ([pers] 
   (settle-propn-net pers 1))
  ([pers iters]
   (settle-net pers :propn-net :propn-activns :propn-mask iters)))

(defn settle-net
  "Return person pers with the net selected by net-key and activns-key updated
  by iters rounds of settling."
  [pers net-key activns-key mask-key iters]
  (assoc pers activns-key
         (ug/fn-pow 
           (partial next-activns (net-key pers) (mask-key pers))
           (activns-key pers)
           iters)))

;; GROSSBERG ALGORITHM SETTLING FUNCTION
;; See settle.md for explanation and reference sources, including
;; explanation of unidirectional links.
;; Q: Should I use add-product here for the inner addition of emuls?
(defn next-activns 
  "Calculate a new set of activations for nodes starting from the current
  activations in vector activns, using network link weights in constraint
  network net to update activations from neighbors using the Grossberg (1978) 
  algorithm as described in Holyoak & Thagard's (1989) \"Analogue Retrieval
  by Constraint Satisfaction\"."
  [net mask activns]
  (let [pos-activns (emap nn/posify activns)] ; Negative activations are ignored as inputs.
    (emul mask
          (emap clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
                (add (emul decay activns)                         ; (decay def'ed above) Sum into decayed activations ... [note must be undone for special nodes]
                     (emul (mmul (nn/pos-wt-mat net) pos-activns) ; positively weighted inputs scaled by
                           (emap dist-from-max activns))          ;  inputs' distances from 1, and
                     (emul (mmul (nn/neg-wt-mat net) pos-activns) ; negatively weighted inputs scaled by
                           (emap dist-from-min activns)))))))     ;  inputs' distances from -1.

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
