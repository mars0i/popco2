(ns popco.core.communic
  (:require [utils.general :as ug]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx])) ; for unmask!

;; TODO MOVE ELSEWHERE
(defn unmask!
  [mask id-to-idx node]
    (mx/mset! mask (id-to-idx node) 1.0))

;; TODO MOVE ELSEWHERE
(defn net-has-node?
  [mask id-to-idx node]
  (= 1.0 (mask (id-to-idx node))))

(declare receive-propn add-to-propn-net add-to-analogy-net)

(defn receive-propn
  [pers propn]
    (add-to-propn-net pers propn)
    (add-to-analogy-net pers propn))

(defn add-to-propn-net
  [pers propn]
  (let [pnet (:propn-net pers)]
    (unmask! (:propn-mask pers) ((:id-to-idx pnet) propn))))

(defn add-to-analogy-net
  [pers propn]
  (let [anet (:analogy-net pers)
        analogy-id-to-idx (:id-to-idx anet)
        analogy-mask (:analogy-mask pers)
        analogue-propns ((:propn-to-analogues anet) propn)

        pnet (:propn-net pers)
        propn-id-to-idx (:id-to-idx pnet)
        propn-mask (:propn-mask pers)
        propn-to-propns (:propn-to-family-propn-idxs pnet) 
        
        propn-net-has-node?  (partial net-has-node? propn-mask propn-id-to-idx) ]

    (when (every? propn-net-has-node? (propn-to-propns propn)) ; if sent propn missing extended-family propns, can't match
      (doseq [a-propn analogue-propns]                         ; now check any possible matches to sent propn
        (when (and 
                (propn-net-has-node? a-propn)                           ; pers has this analogue propn
                (every? propn-net-has-node? (propn-to-propns a-propn))) ; and its extended-family-propns 
            (unmask! analogy-mask                ; TODO THIS STEP HAS TO DO A *LOT* MORE
                     analogy-id-to-idx           ; TODO ALSO UNMASK PRED MAPNODES, AND THEN DO THE SAME RECURSING INTO THE ARGS.
                     (an/ids-to-mapnode-id propn a-propn))))))) ; TODO or reverse args? deal with source/target direction??
