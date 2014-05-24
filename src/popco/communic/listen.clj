(ns popco.communic.listen
  (:require [utils.general :as ug]
            ;[clojure.pprint :as pp] ; only if needed for cl-format
            [popco.core.lot :as lot]
            [popco.core.constants :as cn]
            [popco.nn.propn :as pn]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]
            [incanter.stats :as incant]))

(declare update-propn-net-from-transmissions receive-transmissions
         person-propn-net-clone person-masks-clone
         unmask-for-new-propns add-to-propn-net!
         try-add-to-analogy-net!  propn-still-masked?
         propn-already-unmasked?  propn-components-already-unmasked?
         ids-to-poss-mn-id unmask-mapnode-extended-family!)

(defn combine-speaker-utterance-maps
  "Given a collection of maps from listener ids to collections of Utterances,
  combine them into a single map of the same kind, concat'ing together
  values with the same key."
  [utterance-maps]
  (apply merge-with (comp vec concat) utterance-maps))

;; TODO maybe create clone the net inside this function, and use it directly
(defn update-propn-net-from-utterances
  "ADD TO DOCSTRING. Note utterances is a collection of Utterances."
  [listener utterances]
  (let [propn-net (pn/clone (:propn-net listener)) ; make new net to be assoc'ed into person (or not, to make it mutating)
        id-to-idx (:id-to-idx propn-net)
        propn-mat (:wt-mat propn-net)] ; propn-nets have unified wt-mat
    (doseq [utterance utterances]
      (mx/mset! propn-mat  ; here we are mutating the propn matrix that's still referenced in newly-created propn-net
                (id-to-idx (:propn-id utterance))
                cn/+salient-node-index+
                (* cn/+trust+ (:valence utterance)))) ; future option: replace +trust+ with a function of listener and speaker
    (assoc listener :propn-net propn-net)))

;; entry point from main.clj
;; TODO QUESTION: do I have to reapply semantic-iffs here??
(defn receive-utterances
  "ADD DOCSTRING"
  [utterance-map listener]
  (let [utterances (utterance-map (:id listener))
        propns (map :propn-id utterances)
        new-propns (filter (partial propn-still-masked? listener) propns)
        listener (if new-propns
               (unmask-for-new-propns listener new-propns)
               listener)]
    (update-propn-net-from-utterances listener utterances)))

(defn person-masks-clone
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh copy of its analogy and proposition masks."
  [pers]
  (assoc pers 
         :analogy-mask (mx/clone (:analogy-mask pers))
         :propn-mask (mx/clone (:propn-mask pers))))

(defn unmask-for-new-propns
  [original-pers new-propns]
  (let [pers (person-masks-clone original-pers)] ; TODO Is this really necesary?
    (doseq [new-propn new-propns]
      (add-to-propn-net! pers new-propn)
      (let [propn-to-extended-fams (:propn-to-extended-famss (:propn-net pers))
            fams (propn-to-extended-fams new-propn)]
        (doseq [fam fams                           ; loop through all extended fams containing this propn
                propn fam]                         ; and each propn in that family
          (try-add-to-analogy-net! pers propn))))  ; see whether we can now add analogies using it. [redundantly tries to add analogies for recd-propn-id repeatedly, though will not do much after the first time]
    pers))

(defn add-to-propn-net!
  "ADD DOCSTRING"
  [pers propn]
  (let [pnet (:propn-net pers)]
    (nn/unmask! (:propn-mask pers) ((:id-to-idx pnet) propn))))

(defn try-add-to-analogy-net!
  "ADD DOCSTRING.  See communic.md for further explanation."
  [pers propn]
  (when (propn-components-already-unmasked? pers propn)                ; if sent propn missing extended-family propns, can't match
    (doseq [a-propn ((:propn-to-analogs (:analogy-net pers)) propn)] ; check possible analog propns to sent propn
      (when (and (propn-already-unmasked? pers a-propn)                ; if pers has this analog propn
                 (propn-components-already-unmasked? pers a-propn))    ; and its extended-family-propns 
        (let [mn-id (or (ids-to-poss-mn-id pers a-propn propn)         ; then unmask mapnode corresponding to this propn pair
                        (ids-to-poss-mn-id pers propn a-propn))]
          (unmask-mapnode-extended-family! pers mn-id))))))            ; and all extended family mapnodes

(defn propn-still-masked?
  "Return true if, in person (first arg), propn (second arg) doesn't exist
   in the proposition net in the sense that it's masked; false otherwise."
  [{{id-to-idx :id-to-idx} :propn-net ; bind field of propn-net of person that's passed as 2nd arg
    propn-mask :propn-mask}           ; bind propn-mask of person
   propn]
  (nn/node-masked? propn-mask (id-to-idx propn)))

(defn propn-already-unmasked?
  "Return true if, in person (first arg), propn (second arg) exists in the
  proposition net in the sense that it has been unmasked; false otherwise."
  [{{id-to-idx :id-to-idx} :propn-net ; bind field of propn-net of person that's passed as 2nd arg
    propn-mask :propn-mask}           ; bind propn-mask of person
   propn]
  (nn/node-unmasked? propn-mask (id-to-idx propn)))

(defn propn-components-already-unmasked?
  "Return true if, in person (first arg), propn (second arg) is a possible
  candidate for matching--i.e. if its component propns (and therefore
  preds, objs) already exist, i.e. have been unmasked.  Returns false if not."
  [{{propn-to-descendant-propn-idxs :propn-to-descendant-propn-idxs} :propn-net ; bind field of propn-net of person that's passed as 2nd arg
    propn-mask :propn-mask} ; bind propn-mask of person
   propn]
  (every? (partial nn/node-unmasked? propn-mask) 
          (propn-to-descendant-propn-idxs propn))) ; if propn is missing extended-descendant propns, can't match

(defn ids-to-poss-mn-id
  "Given two id keywords and a person, constructs and returns 
  a corresponding mapnode id, or nil if the id has no index."
  [{{id-to-idx :id-to-idx} :analogy-net} ; bind index map from analogy-net in person
   propn1-id propn2-id]
  (an/ids-to-poss-mapnode-id propn1-id propn2-id id-to-idx))

(defn unmask-mapnode-extended-family!
  "ADD DOCSTRING"
  [{{propn-mn-to-ext-fam-idxs :propn-mn-to-ext-fam-idxs} :analogy-net ; bind index map from analogy-net in person
    analogy-mask :analogy-mask} ; bind mask in person
   mn-id]
  (doseq [idx (propn-mn-to-ext-fam-idxs mn-id)]
    (nn/unmask! analogy-mask idx)))
