;;;; neural-nets.clj
;;;; Initial exploration of matrix-based constraint satisfaction
;;;; network for a new version of POPCO.

;; use one of these:
;(set-current-implementation :persistent-vector)
;(set-current-implementation :ndarray)
;(set-current-implementation :vectorz)
;(set-current-implementation :clatrix)

;(ns '[neuralnets])
(use 'clojure.core.matrix)
(set-current-implementation :vectorz)
(use 'criterium.core)

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
;; emul: Multiply together corresponding elements of matrices
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; scalar functions

(defn rand-1+1 
  "Returns a random number in [-1, 1)."
  []
  (dec (rand 2)))

(defn rand-rand-1+1
  "Returns a random number in [-1, 1) if a prior binary random variable
  with probability prob of success succeeds, else 0.  prob should be
  a number in [0,1].  0 is equivalent to no possibility, not zero prob."
  [prob]
  (if (< (rand) prob)
    (rand-1+1)
    0))

(defn posify 
  "Return the non-negative number closest to x, i.e. 0 if x < 0, else x."
  [x]
  (max 0 x))

(defn negify
  "Return the non-positive number closest to x, i.e. 0 if x > 0, else x."
  [x]
  (min 0 x))

(defn dist-from-max
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn < 0."
  [activn]
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
;; Network settling

(defn next-activns 
  "Calculate a new set of activations for nodes starting from the current
  activations in vector activns, using network link weights in Wts to
  update activations from neighbors using the Grossberg (1978) algorithm
  as described in Holyoak & Thagard (1989).  If three args given, first two
  are nonnegative and nonpositive versions of Wts, i.e. with negative and
  positive weights, respectively, replaced by zeros."

  ([Wts activns]
   (next-activns
     (emap posify Wts) (emap negify Wts) activns)) ; split into nonegative and nonpositve weight matrices

  ([PosWts NegWts activns]
   (let [pos-activns (emap posify activns)] ; Negative activations are ignored as inputs.
     (emap clip-to-extrema                  ; Values outside [-1,1] are clipped to -1, 1.
           (add (emul 0.9 activns)                       ; Sum into decayed activations ...
                (emul (mmul PosWts pos-activns)          ; positively weighted inputs scaled by
                      (emap dist-from-max activns))      ;  inputs' distances from 1, and
                (emul (mmul NegWts pos-activns)          ; negatively weighted inputs scaled by
                      (emap dist-from-min activns))))))) ;  inputs' distances from -1.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tools for testing
;;
(defn make-random-activn-vec
  "Generate a vector of random activations of num-nodes nodes."
  [num-nodes]
  (matrix (repeatedly num-nodes rand-1+1)))

(defn make-random-net-mat
  "Generate a matrix of random links for num-nodes nodes."
  [num-nodes]
  (matrix (repeatedly num-nodes #(repeatedly num-nodes rand-1+1))))

(defn make-sparse-random-net-mat
  "Generate a matrix of random links for num-nodes nodes, with probability
  prob-of-link of creating a link (i.e. of choosing a random weight rather 
  than simply zero).  Note that the matrix is never sparse; the network 
  represented by may be."
  [num-nodes prob-of-link]
  (matrix 
    (repeatedly num-nodes 
                #(repeatedly num-nodes (fn [] (rand-rand-1+1 prob-of-link))))))


(defn bench-settle [num-nodes prob-of-link]
  (let [a (make-random-activn-vec num-nodes)
        M (make-sparse-random-net-mat num-nodes prob-of-link)
        Mp (emap posify M)
        Mn (emap negify M)]
    ;; jit/etc burn-in
    (dotimes [i 5] (def _ (next-activns M a)))
    ;; tests
    (print (format "%nWith single weight-matrix: num-nodes: %s, prob-of-link: %s:%n%n" num-nodes prob-of-link))
    (bench (def _ (next-activns M a)))
    (print (format "%nWith split weight-matrices: num-nodes: %s, prob-of-link: %s:%n%n" num-nodes prob-of-link))
    (bench (def _ (next-activns Mp Mn a))) ))

;;;;;;;;;;;;;;;;;;;;

;; Example:

;; In POPCO's crime3.lisp, e.g. as called in yotest.lisp, a person with all propositions
;; has the following numbers of nodes and links:
;;
;; nodes in analogy net:     links in analogy net:
;; 262                       2499
;; (i.e. the number of links is about .035 of those possible: 2499/(262*262) = .0364)
;;
;; nodes in proposition net: links in proposition net:
;;  50                        183
;;
;; total nodes:              total links:
;; 312                       2682
