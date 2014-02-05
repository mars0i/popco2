(ns popco.core.communic
  (:require [utils.general :as ug]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.pprint :as pp] ; TEMPORARY
            [clojure.core.matrix :as mx])) ; for unmask!

;; TODO MOVE ELSEWHERE?
(defn unmask!
  "Given a core.matrix vector representing a mask, and an index
  into the mask, set the indexed element of the mask to 1."
  [mask idx]
    (mx/mset! mask idx 1.0))

;; TODO MOVE ELSEWHERE?
(defn net-has-node?
  "Given a core.matrix vector representing a mask, and an index
  into the mask, return true if the mask is 1 at that index;
  otherwise false."
  [mask idx]
  (= 1.0 (mx/mget mask idx)))

(declare receive-propn add-to-propn-net add-to-analogy-net unmask-propn-components!)

(defn receive-propn
  [pers propn]
    ; TODO should I test for already being unmasked, before unmasking again?
    (add-to-propn-net pers propn)
    ; TODO should I test for already having mapnodes, before trying to create them again
    ; TODO also find propns that the new propn participates in, and try to add them to analogy net as well
    (add-to-analogy-net pers propn))

(defn add-to-propn-net
  [pers propn]
  (let [pnet (:propn-net pers)]
    (unmask! (:propn-mask pers) ((:id-to-idx pnet) propn))))

;; WHAT add-to-analogy-net IS SUPPOSED TO DO:
;; Background: All legal mappings between lot-elements are found by make-analogy-net,
;; in analogy.clj, along with their links.
;; By default, however, those nodes are masked.  They don't contribute to
;; the changes in activation values.
;; When a propn is added to a person, we may be able to unmask some of the
;; masked mapnodes--to add them into the process.
;; We can't add a propn-propn mapnode, though, unless its analogue, 
;; all of its component propositions, and their analogues 
;; already exist in the receiver person.  However, we don't need to 
;; worry about predicates and objects.  If a matched propn exists in the receiver,
;; then so does its predicate and so do its argument objects.  However, if
;; the propn sent is higher-order, then it's possible for it to be there without
;; its argument propns to be in the receiver.  (This might not be what *should*
;; happen, but it's what popco 1 did, so its a behavior we want to be able to
;; produce at least as an option.)  Similarly, if a matching propn exists in the
;; receiver, nevertheless its arg propns might not exist in the receiver.  So:
;; We can only unmask a mapnode involving the sent propn if the appropriate
;; extended family propns on both sides exist all the way down.  And in that
;; case, they all will get corresponding mapnodes unmasked, along with predicates
;; and objects along the way.

(defn propn-already-unmasked?
  "Return true if, in person (first arg), propn (second arg) exists in the
  proposition net in the sense that it has been unmasked; false otherwise."
  [{{id-to-idx :id-to-idx} :propn-net ; bind field of propn-net of person that's passed as 2nd arg
    propn-mask :propn-mask}           ; bind propn-mask of person
   propn]
  (net-has-node? propn-mask (id-to-idx propn)))

(defn propn-components-already-unmasked?
  "Return true if, in person (first arg), propn (second arg) is a possible
  candidate for matching--i.e. if its component propns (and therefore
  preds, objs) already exist, i.e. have been unmasked.  Returns false if not."
  [{{propn-to-family-propn-idxs :propn-to-family-propn-idxs} :propn-net ; bind field of propn-net of person that's passed as 2nd arg
    propn-mask :propn-mask} ; bind propn-mask of person
   propn]
  (every? (partial net-has-node? propn-mask) 
          (propn-to-family-propn-idxs propn))) ; if propn is missing extended-family propns, can't match

(defn add-to-analogy-net
  [pers propn]
  (let [analogy-mask (:analogy-mask pers)
        anet (:analogy-net pers)
        aid-to-idx (:id-to-idx anet)
        aid-to-ext-fam-idxs (:propn-mn-to-ext-fam-idxs anet)
        analogue-propns ((:propn-to-analogues anet) propn)

        propn-mask (:propn-mask pers)
        pnet (:propn-net pers)
        ;pid-to-idx (:id-to-idx pnet)
        pid-to-propn-idxs (:propn-to-family-propn-idxs pnet) 
        
        propn-net-has-node? (partial net-has-node? propn-mask)
        unmask-mapnode! (partial unmask! analogy-mask) ]

    ;(pp/cl-format true "propn: ~s~%" propn) ; DEBUG
    (when (propn-components-already-unmasked? pers propn) ; if sent propn missing extended-family propns, can't match
      (doseq [a-propn analogue-propns]                         ; now check any possible matches to sent propn
        ;(do (pp/cl-format true "\ta-propn: ~s ~s ~s~%" a-propn (pid-to-idx a-propn) (propn-net-has-node? (pid-to-idx a-propn))) (pp/cl-format true "\tsub-a-propns propn-net-has-node?: ~s ~s~%" (pid-to-propn-idxs a-propn) (every? propn-net-has-node? (pid-to-propn-idxs a-propn)))) ; DEBUG
        (when (and 
                (propn-already-unmasked? pers a-propn)                    ; pers has this analogue propn
                (every? propn-net-has-node? (pid-to-propn-idxs a-propn))) ; and its extended-family-propns 
          ;; Then we can unmask all mapnodes corresponding to this propn pair:
          (let [aid (or (an/ids-to-poss-mapnode-id a-propn propn aid-to-idx)   ; replace the or by passing in the analogue-struct?
                        (an/ids-to-poss-mapnode-id propn a-propn aid-to-idx))]
            ;(pp/cl-format true "\t\taid + idxs: ~s~%" aid (aid-to-ext-fam-idxs aid)) ; DEBUG
            (ug/domap unmask-mapnode! (aid-to-ext-fam-idxs aid)))))))) ; unmask propn mapnode, pred mapnode, object mapnodes, recurse into arg propn mapnodes
