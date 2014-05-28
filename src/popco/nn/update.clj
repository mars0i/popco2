(ns popco.nn.update
  (:require [utils.general :as ug]
            [popco.core.person :as pers]
            [popco.core.constants :as cn]
            [popco.nn.nets :as nn]
            [popco.nn.matrix :as pmx]
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
         pl-update-nets clip-to-extrema dist-from-max dist-from-min calc-propn-link-wt)

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
        a-activns (:analogy-activns pers)
        aidx-to-pidxs (:analogy-idx-to-propn-idxs pers)
        propn-from-analogy-wt-mat (propn-wts-from-analogy-activns dim a-activns aidx-to-pidxs)] ; rets new mat
    (mx/emap! clip-to-extrema (mx/add! propn-from-analogy-wt-mat linger-wt-mat))
    (assoc-in pers [:propn-net :wt-mat] propn-from-analogy-wt-mat)))

(defn propn-wts-from-analogy-activns
  "Generates a new proposition weight matrix from proposition map nodes 
  in the analogy network.   i.e. this sets the weight of a propn-to-propn 
  link as a function of the activation of the map node that maps those two 
  propositions, in the analogy network."
  [pdim a-activns aidx-to-pidxs]
  (let [aidxs (keys aidx-to-pidxs)
        wt-mat (pmx/zero-matrix pdim pdim)] ; make new mat
    (doseq [aidx aidxs
            :let [a-val (mx/mget a-activns aidx)
                  [p-idx1 p-idx2] (aidx-to-pidxs aidx)]]
      (nn/symlink! wt-mat p-idx1 p-idx2 (calc-propn-link-wt a-val)))
    wt-mat))

;; TODO: Deal with semantic-iffs and influence from communication.
;; TODO TODO maybe refactor so that I'm not going in and out of the person repeatedly--
;; i.e pass around components, and then reconstruct the person at the end.
;(defn old-update-propn-wts-from-analogy-activns
;  "Performs a functional update of person pers's propn link weight matrix 
;  from activations of proposition map nodes in the analogy network.  
;  i.e. this updates the weight of a propn-to-propn link as a function of 
;  the activation of the map node that maps those two propositions, in the 
;  analogy network.  Returns the fresh, updated person."
;  [pers]
;  (let [updated-pers (set-propn-wts-from-analogy-activns! (pers/propn-net-zeroed pers)) ; make a new wt-mat, then update from analogy net
;        p-mat (:wt-mat (:propn-net updated-pers))]
;    (assoc-in pers [:propn-net :wt-mat]
;              (emap! clip-to-extrema (add! p-mat (:linger-wt-mat pers))))))
;
;(defn old-set-propn-wts-from-analogy-activns!
;  "Mutates person pers's propn link weight matrix from activations of
;  proposition map nodes in the analogy network.  i.e. this sets the
;  weight of a propn-to-propn link as a function of the activation of
;  the map node that maps those two propositions, in the analogy network.
;  Returns the original person with its newly-modified propn net"
;  [pers]
;  (let [a-activns (:analogy-activns pers)
;        p-mat (:wt-mat (:propn-net pers))         ; We want the actual matrix. Safer with the keyword.
;        aidx-to-pidxs (:analogy-idx-to-propn-idxs pers)
;        aidxs (keys aidx-to-pidxs)]
;    (doseq [a-idx aidxs
;            :let [a-val (mget a-activns a-idx)
;                  [p-idx1 p-idx2] (aidx-to-pidxs a-idx)]]  ; CAN I DO THIS AT THE TOP OF THE doseq BY DESTRUCTURING MAP ELEMENTS?
;      (nn/dirlink! p-mat p-idx1 p-idx2 (calc-propn-link-wt a-val)))
;    pers)) ; this function mutates the matrix inside pers, so no need to assoc it into the result

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
             (mx/emap clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
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
      (mx/emap! clip-to-extrema                     ; Values outside [-1,1] are clipped to -1, 1.
                (mx/add! (mx/emul cn/+decay+ activns)                        ; (decay def'ed above) Sum into decayed activations ... [note must be undone for special nodes]
                         (mx/emul! (mx/mmul (nn/pos-wt-mat net) pos-activns) ; positively weighted inputs scaled by
                                   (mx/emap dist-from-max activns))          ;  inputs' distances from 1, and
                         (mx/emul! (mx/mmul (nn/neg-wt-mat net) pos-activns) ; negatively weighted inputs scaled by
                                   (mx/emap dist-from-min activns))))        ;  inputs' distances from -1.
      mask)))


(def next-activns purely-functional-next-activns)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; scalar functions

(defn clip-to-extrema
  "Returns -1 if x < -1, 1 if x > 1, and x otherwise."
  [x]
  (max cn/+neg-one+ (min cn/+one+ x)))

(defn dist-from-max
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn < 0."
  [activn]
  (- cn/+one+ activn))

(defn dist-from-min 
  "Return the distance of activn from 1.  Note return value will be > 1
  if activn > 0."
  [activn]
  (inc activn)) ; HT1989 p. 313 says: (- activn -1).  inc seems to be slightly faster than (+ 1 ...)
