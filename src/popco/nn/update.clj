(ns popco.nn.update
  (:require [utils.general :as ug]
            [popco.core.person :as pers]
            [popco.core.constants :as cn]
            [popco.nn.nets :as nn]
            [clojure.core.matrix :as mx]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; NOTES
;;; See update.md for more notes, including abbreviations, overview of
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
         update-propn-wts-from-analogy-activns set-propn-wts-from-analogy-activns! 
         propn-wts-from-analogy-activns
         update-person-nets update-nets 
         ;update-person-nets!  update-nets! 
         pl-update-nets dist-from-max dist-from-min calc-propn-link-wt)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Network settling (with Grossberg algorithm)

(defn update-person-nets
  "Perform one tick's (functional) updating of the networks of a single person."
  [pers]
  (-> pers
    (settle-analogy-net cn/+settling-iters+) ; is this step necessary?? only because of cycling??
    (update-propn-wts-from-analogy-activns)
    (settle-propn-net cn/+settling-iters+)))

(defn update-person-nets-popco1-style
  "Perform one tick's (functional) updating of the networks of a single person,
  in the order in which popco1 did it.  i.e. does, per-person, what popco1 did
  with settle-nets and update-propn-nets-from-analogy-nets."
  [pers]
  (-> pers
    (settle-analogy-net cn/+settling-iters+)
    (settle-propn-net cn/+settling-iters+)    ; popco1 settled the two disjuoint nets simultaneously
    (update-propn-wts-from-analogy-activns)))

;; Note 
;; default impl of mx/add! just emaps! +
;; The vectorz version calls a vectorz function.

;; TODO: fix docstrings
(defn update-propn-wts-from-analogy-activns
  "Performs a functional update of person pers's propn link weight matrix 
  from activations of proposition map nodes in the analogy network and from
  the semantic/conversational weight matrix.  i.e. this updates the weight of
  a propn-to-propn link as a function of the activation of the map node that 
  maps those two propositions, in the analogy network, and then adds in the
  semantic weights and conversationally-derived weights.  Returns updated 
  person with a new weight matrix."
  [pers]
  (let [linger-wt-mat (:linger-wt-mat (:propn-net pers))
        dim (first (mx/shape linger-wt-mat))
        a-activns (:activns (:analogy-net pers))
        aidx-to-pidxs (:analogy-idx-to-propn-idxs pers)
        propn-from-analogy-wt-mat (propn-wts-from-analogy-activns dim a-activns aidx-to-pidxs)] ; rets new mat
    (mx/emap! nn/clip-to-extrema (mx/add! propn-from-analogy-wt-mat linger-wt-mat))
    (assoc-in pers [:propn-net :all-wt-mat] propn-from-analogy-wt-mat)))

(defn propn-wts-from-analogy-activns
  "Generates a new proposition weight matrix from proposition map nodes 
  in the analogy network.   i.e. this sets the weight of a propn-to-propn 
  link as a function of the activation of the map node that maps those two 
  propositions, in the analogy network."
  [pdim a-activns aidx-to-pidxs]
  (let [aidxs (keys aidx-to-pidxs) ; TODO QUESTION: Should we instead use only unmasked indexes?
        wt-mat (mx/zero-matrix pdim pdim)] ; make new mat
    (doseq [aidx aidxs        ; loop through indexes of all propn map nodes in analogy net.
            :let [a-val (mx/mget a-activns aidx)          ; if the propn map node has not been unmasked, this activation will be 0.
                  [p-idx1 p-idx2] (aidx-to-pidxs aidx)]]  ; TODO should this be done only if a-val is nonzero?
      (nn/symlink! wt-mat p-idx1 p-idx2 (calc-propn-link-wt a-val)))
    wt-mat))

;; calc-assoc-weight in imp.lisp in popco1
(defn calc-propn-link-wt
  "Given an activation value from the analogy network, calculate the weight
  that the corresponding link in the propn network should get by default."
  [a-activn]
  (if (pos? a-activn)
    (* a-activn cn/+analogy-to-propn-pos-multiplier+)
    (* a-activn cn/+analogy-to-propn-neg-multiplier+)))

(defn settle-analogy-net
  "Return person pers with its analogy net updated by 1 or more iters of settling."
  ([pers] 
   (settle-analogy-net pers 1))
  ([pers iters]
   (settle-net pers :analogy-net iters)))

(defn settle-propn-net
  "Return person pers with its propn net updated by 1 or more iters of settling."
  ([pers] 
   (settle-propn-net pers 1))
  ([pers iters]
   (settle-net pers :propn-net iters)))

(defn settle-net
  "Return person pers with the net selected by net-key and activns-key updated
  by iters rounds of settling."
  [pers net-key iters]
  (let [net (net-key pers)]
    (assoc pers 
           net-key (assoc net :activns
                          (ug/fn-pow 
                            (partial next-activns net (:mask net))
                            (:activns net)
                            iters)))))

  ;; GROSSBERG ALGORITHM SETTLING FUNCTION
;; See settle.md for explanation and reference sources, including
;; explanation of unidirectional links.
;; TODO: Should I use add-product here for the inner addition of emuls?
;; TODO: Should I use ! versions of these functions?

;; Uses no ! operators from core.matrix
(defn purely-functional-next-activns 
  "Calculate a new set of activations for nodes starting from the current
  activations in vector activns, using network link weights in constraint
  network net to update activations from neighbors using the Grossberg (1978) 
  algorithm as described in Holyoak & Thagard's (1989) \"Analogue Retrieval
  by Constraint Satisfaction\"."
  [net mask activns]
  (let [pos-activns (mx/emap nn/posify activns)] ; Negative activations are ignored as inputs.
    (mx/emul mask
             (mx/emap nn/clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
                      (mx/add (mx/emul cn/+decay+ activns)                       ; (decay def'ed above) Sum into decayed activations ... [note must be undone for special nodes]
                              (mx/emul (mx/mmul (nn/pos-wt-mat net) pos-activns) ; positively weighted inputs scaled by
                                       (mx/emap dist-from-max activns))          ;  inputs' distances from 1, and
                              (mx/emul (mx/mmul (nn/neg-wt-mat net) pos-activns) ; negatively weighted inputs scaled by
                                       (mx/emap dist-from-min activns)))))))     ;  inputs' distances from -1.

;; This version mutates new matrices created inside this function.  It seems to be slightly faster.
;; The general principle was to modify the old version of next-activns only by adding "!" when
;; the previous operations had created a new matrix that could therefore be mutated.  This requires
;; some care, but the output turns out to be the same, as expected.  Note that I reversed the order
;; of arguments for the outer emul by mask in order to take advantage of this scheme there.
;; Note that at present (5/2014), I could actually use mmul! for the inner mmul on pos/neg-wt-mat
;; *when it comes from the propn net*, but not when it comes from the analogy net.
(defn mutating-next-activns 
  "Calculate a new set of activations for nodes starting from the current
  activations in vector activns, using network link weights in constraint
  network net to update activations from neighbors using the Grossberg (1978) 
  algorithm as described in Holyoak & Thagard's (1989) \"Analogue Retrieval
  by Constraint Satisfaction\"."
  [net mask activns]
  (let [pos-activns (mx/emap nn/posify activns)] ; Negative activations are ignored as inputs.
    (mx/emul!
      (mx/emap! nn/clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
                (mx/add! (mx/emul cn/+decay+ activns)                        ; (decay def'ed above) Sum into decayed activations ... [note must be undone for special nodes]
                         (mx/emul! (mx/mmul (nn/pos-wt-mat net) pos-activns) ; positively weighted inputs scaled by
                                   (mx/emap dist-from-max activns))          ;  inputs' distances from 1, and
                         (mx/emul! (mx/mmul (nn/neg-wt-mat net) pos-activns) ; negatively weighted inputs scaled by
                                   (mx/emap dist-from-min activns))))        ;  inputs' distances from -1.
      mask)))


(def next-activns purely-functional-next-activns)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; scalar functions

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
