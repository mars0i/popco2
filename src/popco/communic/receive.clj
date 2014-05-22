(ns popco.communic.receive
  (:require [utils.general :as ug]
            ;[clojure.pprint :as pp] ; only if needed for cl-format
            [popco.core.lot :as lot]
            [popco.nn.propn :as pn]
            ;[popco.core.person :as prs]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]
            [incanter.stats :as incant]))

(declare transmit receive-propn! add-to-propn-net! try-add-to-analogy-net!
         propn-still-masked? propn-already-unmasked? propn-components-already-unmasked?
         ids-to-poss-mn-id unmask-mapnode-extended-family!)

;; NOTE transmit-utterances might not be purely functional.
;(defn receive-transmissions
;  "Currently a noop: Takes persons with specifications of conversations assoc'ed
;  in with :convs, and returns the persons with the conversations stripped out, 
;  but with the persons updated to reflect the conversations.  (See 
;  choose-conversations for the structure of conversations.)  (Note we need the 
;  persons as well as conversations, so that we don't lose persons that no one 
;  speaks to.)"
;  [persons-and-transmissions]
;  (let [[persons transmissions] (ug/split-elements persons-and-transmissions)
;        transmission-map (ug/join-pairs-to-coll-map (apply concat transmissions))] ; TODO: are there faster methods at http://stackoverflow.com/questions/23745440/map-of-vectors-to-vector-of-maps
;    ;; ADD TRANSMISSION STEP
;    ;; Maybe something like:
;    ;; (pmap persons #(domap (comp apply receive-propn!) (transmission-map person)))
;    persons)) ; TODO TEMPORARY

(defn receive-transmissions
  [transmission-map pers]
  (let [pers-id (:id pers)
        tranmissions (transmission-map pers-id)]
    :TODO) ; TODO
  pers)

(defn kludge-masks-clone ; FIXME temp to get around a cyclic dependence for testing
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh copy of its analogy and proposition masks."
  [pers]
  (assoc pers 
         :analogy-mask (pn/clone (:analogy-mask pers))
         :propn-mask (pn/clone (:propn-mask pers))))

(defn unmask-for-new-propns
  [original-pers new-propns]
  (let [pers (kludge-masks-clone original-pers)]
    (doseq [new-propn new-propns]
      (add-to-propn-net! pers new-propn)
      (let [propn-to-extended-fams (:propn-to-extended-famss (:propn-net pers))
            fams (propn-to-extended-fams new-propn)]
        (doseq [fam fams                           ; loop through all extended fams containing this propn
                propn fam]                         ; and each propn in that family
          (try-add-to-analogy-net! pers propn))))  ; see whether we can now add analogies using it. [redundantly tries to add analogies for recd-propn-id repeatedly, though will not do much after the first time]
    pers))

(defn kludge-propn-net-clone ; FIXME temp to get around a cyclic dependence for testing
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh copy of its proposition network.  (Useful e.g. for updating pers's
  proposition network as a function of analogy net activations.)"
  [pers]
  (assoc pers 
         :propn-net (pn/clone (:propn-net pers))))
    
(defn update-propn-net-from-transmissions
  [pers transmission]
  (kludge-propn-net-clone pers)) ; FIXME actually update the propnnet. does this *have* to be purely functional...?

(defn receive-propn
  "ADD DOCSTRING"
  [pers transmissions]
  (let [propns (map first transmissions)
        new-propns (filter (partial propn-still-masked? pers) propns)
        pers (if new-propns
               (unmask-for-new-propns pers new-propns)
               pers)]
    (update-propn-net-from-transmissions pers transmissions)))

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TEMPORARY DEFS FOR TESTING
; (def net-has-node? nn/node-unmasked?)
;; Keeping around as a sanity check of the new version above.
; (defn old-add-to-analogy-net
;   [pers propn]
;   (let [analogy-mask (:analogy-mask pers)
;         anet (:analogy-net pers)
;         aid-to-idx (:id-to-idx anet)
;         aid-to-ext-fam-idxs (:propn-mn-to-ext-fam-idxs anet)
;         analog-propns ((:propn-to-analogs anet) propn)
; 
;         propn-mask (:propn-mask pers)
;         pnet (:propn-net pers)
;         pid-to-idx (:id-to-idx pnet)
;         pid-to-propn-idxs (:propn-to-descendant-propn-idxs pnet) 
;         
;         propn-net-has-node? (partial net-has-node? propn-mask)
;         unmask-mapnode! (partial nn/unmask! analogy-mask) ]
; 
;     ;(pp/cl-format true "propn: ~s~%" propn) ; DEBUG
;     (when (every? propn-net-has-node? (pid-to-propn-idxs propn)) ; if sent propn missing extended-family propns, can't match
;       (doseq [a-propn analog-propns]                         ; now check any possible matches to sent propn
;         ;(pp/cl-format true "\ta-propn: ~s ~s ~s~%" a-propn (pid-to-idx a-propn) (propn-net-has-node? (pid-to-idx a-propn))) ; DEBUG
;         ;(pp/cl-format true "\tsub-a-propns propn-net-has-node?: ~s ~s~%" (pid-to-propn-idxs a-propn) (every? propn-net-has-node? (pid-to-propn-idxs a-propn))) ; DEBUG
;         (when (and 
;                 (propn-net-has-node? (pid-to-idx a-propn))                ; pers has this analog propn
;                 (every? propn-net-has-node? (pid-to-propn-idxs a-propn))) ; and its extended-family-propns 
;           ;; Then we can unmask all mapnodes corresponding to this propn pair:
;           (let [aid (or (an/ids-to-poss-mapnode-id a-propn propn aid-to-idx)   ; MAYBE: replace the OR by passing in the analog-struct?
;                         (an/ids-to-poss-mapnode-id propn a-propn aid-to-idx))]
;             ;(pp/cl-format true "\t\taid + idxs: ~s~%" aid (aid-to-ext-fam-idxs aid)) ; DEBUG
;             (ug/domap unmask-mapnode! (aid-to-ext-fam-idxs aid)))))))) ; unmask propn mapnode, pred mapnode, object mapnodes, recurse into arg propn mapnodes
