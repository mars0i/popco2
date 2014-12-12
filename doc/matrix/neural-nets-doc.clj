;;;; neural-nets-doc.clj
;;;; Early version of neural-nets-doc.clj, that has additional
;;;; documentation.
;;;; Initial exploration of matrix-based constraint satisfaction
;;;; network for a new version of POPCO.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTES

;; References:
;; 1. network.lisp in POPCO, which is based on Thagard's ACME network.lisp.
;; 2. "HT": Holyoak & Thagard 1989, "Analogue Retrieval by Constraint
;; Satisfaction", Cognitive Science 13, pp. 295-355. See pp. 313, 315.
;; 3. "MR": Abrams 2013, "A Moderate Role for Cognitive Models in Agent-Based 
;; Modeling of Cultural Change", Complex Adaptive Systems Modeling 2013.
;; Note that the latter has errors.

;; Note the distinction in clojure.core.matrix between:
;; mul:  Multiply together corresponding elements of matrices
;;       which should have the same shape.
;; mmul: Regular matrice multiplication A * B:
;;       mul (see above) each row i of A with each column j of B,
;;       summing the result each time to produce element <i,j> of
;;       the result matrix.  (For vectors, this is inner product,
;;       with A as a row vector and B as a column vector.)
;;
;; emap maps a function over each element of a matrix to produce a new
;;      matrix.

;; Convention: Vector names are all-lower-case.  Matrices have initial cap
;; in each component of the name.

(use 'clojure.core.matrix)
(set-current-implementation :vectorz)
;(set-current-implementation :clatrix)

(defmacro defun 
  "Like defn, but with Lisp-style doc string placement after argvec.
  (Don't use with destructuring/multiple argument/body lists or with no
  docstring.  In those cases you're better off using defn anyway.)"
  [fn-name argvec docstring & body]
  `(defn ~fn-name ~docstring ~argvec ~@body))

(defmacro FUNCTION [docstring defn-sym fn-name & args-and-body]
  "Allows putting the docstring *before* defn, so docstring doesn't
  interrupt the code.  Second argument must be defn.  Usage:
  (FUNCTION docstring
  defn name args/body)"
  (if (not= defn-sym 'defn)
    (throw (Exception. "Macro FUNCTION: Second argument must be the symbol defn.")))
  `(defn ~fn-name ~docstring ~@args-and-body))

(defmacro DEF [DEFINED-SYM docstring defining-sym defined-sym & args-and-body]
  "Allows putting the docstring *before* defn, def, etc., so docstring 
  doesn't interrupt code.  Example usage:
  (DEF MY-FUNCTION-NAME
  docstring
  defn my-function-name [x y] blah blah blah)"
  `(~defining-sym ~defined-sym ~docstring ~@args-and-body))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; scalar functions

; alternative definition styles allowed by preceding:

(DEF RAND-1+1
"Returns a random number in [-1, 1)."
defn rand-1+1 []
  (dec (rand 2)))

(DEF POSIFY
"Return the non-negative number closest to x, i.e. 0 if x < 0, else x."
defn posify [x]
  (max 0 x))

(defun negify [x]
  "Return the non-positive number closest to x, i.e. 0 if x > 0, else x."
  (min 0 x))

(defun dist-from-max [activn]
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn < 0."
  (- 1 activn))

(defn dist-from-min 
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn > 0."
  [activn]
  (+ 1 activn)) ; What's HT1989 p. 313 really says is (activn - -1)

(defn clip-to-extrema
  "Returns -1 if x < -1, 1 if x > 1, and x otherwise."
  [x]
  (max -1 (min 1 x)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data transformations

;; NEED TO TRIPLE CHECK EVERYTHING 
;; against HT, MR, network.lisp, grossbergalgorithm.nts.

;; step 0: generate some data to operate on
(def num-nodes 4000)
(def activns (matrix (repeatedly num-nodes rand-1+1)))
(def Wts (matrix (repeatedly num-nodes #(repeatedly num-nodes rand-1+1))))

;; step 1
;; ; neg activns will have no effect
(def pos-activns (emap posify activns)) ; max(0, a_j) in MR; o_i(t) in HT

;; step 2
(def Pos-Wts (emap posify Wts)) ; w_ij > 0 in MR
(def Neg-Wts (emap negify Wts)) ; w_ij < 0

;; step 3
(def pos-wtd-inputs (mmul Pos-Wts pos-activns)) ; p_i in MR; enet_j in HT
(def neg-wtd-inputs (mmul Neg-Wts pos-activns)) ; n_i in MR; inet_j in HT

;; step 4
(def dists-from-max 
  (emap dist-from-max activns)) ; .99 - a_i in MR; max - a_j(t) in HT
(def dists-from-min 
  (emap dist-from-min activns)) ; incorrect in MR; a_j(t) - min in HT

;; step 5
(def decayed-activns (mul 0.9 activns)) ; incorrect in MR; a_j(t)(1-d) in HT

;; step 6
;; Almost final step
(def unclipped-new-activns  ; s_i in MR; equation p. 313 in HT
  (add decayed-activns 
       (mul pos-wtd-inputs dists-from-max)
       (mul neg-wtd-inputs dists-from-min)))

;; step 7
;; Now just pull back activns that have exceeded -1 and 1:
(def new-activns 
  (emap clip-to-extrema unclipped-new-activns)) ; a'_i in MR; p. 315 in HT

(defn next-activns [Wts activns]
  (let [pos-activns (emap posify activns)] ; Negative activations are ignored as inputs. <1>
    (emap clip-to-extrema                  ; Values outside [-1,1] are clipped to -1, 1. <7>
          (add (mul 0.9 activns)           ; Sum into decayed activations ... <6>
               (mul (mmul (emap posify Wts) pos-activns) ; positively weighted inputs scaled by <2, 3>
                    (emap dist-from-max activns))        ;  inputs' distances from 1, and <4>
               (mul (mmul (emap negify Wts) pos-activns) ; negatively weighted inputs scaled by <2, 3>
                    (emap dist-from-min activns))))))    ;  inputs' distances from -1. <4>
