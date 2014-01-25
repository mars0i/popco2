(ns popco.core.communic
  (:require [utils.general :as ug]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx])) ; for unmask!

;; TODO MOVE ELSEWHERE?
(defn unmask!
  [mask id-to-idx node] ;; TODO CHANGE TO TAKE ONLY INDEX ARG, NOT THE NODE ID
    (mx/mset! mask (id-to-idx node) 1.0))

;; TODO MOVE ELSEWHERE?
(defn net-has-node?
  [mask id-to-idx node]
  (= 1.0 (mask (id-to-idx node))))

(declare receive-propn add-to-propn-net add-to-analogy-net unmask-propn-components!)

(defn receive-propn
  [pers propn]
    (add-to-propn-net pers propn)
    (add-to-analogy-net pers propn))

(defn add-to-propn-net
  [pers propn]
  (let [pnet (:propn-net pers)]
    (unmask! (:propn-mask pers) ((:id-to-idx pnet) propn)))) ;; TODO NOT RIGHT

;; WHAT add-to-analogy-net IS SUPPOSED TO DO:
;; All legal mappings between lot-elements are found by make-analogy-net,
;; along with their links.
;; By default, however, these nodes are masked.  They don't contribute to
;; the changes in activation values.
;; When a propn is added to a person, we may be able to unmask some of the
;; masked mapnodes--to add them into the process.
;; We can't add a propn-propn mapnode, though, unless all of its components' 
;; analogues already exist in the receiver person.  However, we don't need to 
;; worry about predicates and objects.  If a matched propn exists in the receiver,
;; then so does its predicate and so to its argument objects.  However, if
;; the propn sent is higher-order, then it's possible for it to be there without
;; its argument propns to be in the receiver.  (This might not be what *should*
;; happen, but it's what popco 1 did, so its a behavior we want to be able to
;; produce at least as an option.)  Similarly, if a matching propn exists in the
;; receiver, nevertheless its arg propns might not exist in the receiver.  So:
;; We can only unmask a mapnode involving the sent propn if the appropriate
;; extended family propns on both sides exist all the way down.  And in that
;; case, they all will get corresponding mapnodes unmasked, along with predicates
;; and objects along the way.

(defn add-to-analogy-net
  [pers propn]
  (let [analogy-mask (:analogy-mask pers)
        anet (:analogy-net pers)
        analogy-id-to-idx (:id-to-idx anet)
        analogue-propns ((:propn-to-analogues anet) propn)

        propn-mask (:propn-mask pers)
        pnet (:propn-net pers)
        propn-id-to-idx (:id-to-idx pnet)
        propn-to-propns (:propn-to-family-propn-idxs pnet) 
        
        propn-net-has-node?  (partial net-has-node? propn-mask propn-id-to-idx) ]

    (when (every? propn-net-has-node? (propn-to-propns propn)) ; if sent propn missing extended-family propns, can't match
      (doseq [a-propn analogue-propns]                         ; now check any possible matches to sent propn
        (when (and 
                (propn-net-has-node? a-propn)                           ; pers has this analogue propn
                (every? propn-net-has-node? (propn-to-propns a-propn))) ; and its extended-family-propns 
          ;; TODO unmask the propn mapnode, pred mapnode, object mapnodes, and recurse into propn args
          ;; USE :propn-mn-to-ext-fam-idxs
          )))))

