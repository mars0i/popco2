(ns popco.core.communic
  (:require [utils.general :as ug]
            ;[clojure.pprint :as pp] ; only if needed for cl-format
            [popco.core.lot :as lot]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]
            [incanter.stats :as incant]))

(declare communicate choose-conversations choose-person-conversers choose-utterance
         receive-propn! add-to-propn-net! try-add-to-analogy-net! propn-still-masked?
         propn-already-unmasked? propn-components-already-unmasked? ids-to-poss-mn-id 
         unmask-mapnode-extended-family! transmit-utterances choose-thought)

;(defn communicate
;  "Implements a single timestep's (tick's) worth of communication.  Given a
;  sequence of persons, constructs conversations and returns the persons, updated
;  to reflect the conversations."
;  [persons & trans-repts]
;  (transmit-utterances persons 
;                       ((ug/comp* trans-repts) (choose-conversations persons))))

(defn person-choose-listeners
  "Given a person as argument, return a sequence of persons to whom
  the argument person wants to talk on this tick."
  [{:keys [talk-to-persons max-talk-to]}]
  (if (>= max-talk-to (count talk-to-persons))
    talk-to-persons
    ;; TODO use clojure.core/rand-nth ?
    (incant/sample talk-to-persons :size max-talk-to :replacement false)))

; obsolete
;(defn choose-propn-to-utter
;  [{:keys [propn-net propn-mask propn-activns]}]
;  :NO-OP) ; TODO



;; NOTE transmit-utterances might not be purely functional.
(defn transmit-utterances
  "Currently a noop: Takes persons with specifications of conversations assoc'ed
  in with :convs, and returns the persons with the conversations stripped out, 
  but with the persons updated to reflect the conversations.  (See 
  choose-conversations for the structure of conversations.)  (Note we need the 
  persons as well as conversations, so that we don't lose persons that no one 
  speaks to.)"
  [persons]
  persons)

(defn choose-person-conversers
  "Currently a noop. Given a person pers, returns a converser-pair assoc'ed
  into a person with :convs.  A converse-pair is a sequence 
  of 2-element maps with keys :speaker and :listener, where :speaker is pers, 
  and :listener is another person."
  [pers]
  pers)

;; TODO COMPLETELY CONFUSED
(defn utterances-worth-saying
  [{:keys [propn-net propn-mask propn-activns utterable-idxs]}]
  (let [curr-utterable-idxs (mx/emul propn-mask utterable-idxs propn-activns)
        utterable-abs-activns (mx/abs (mx/emul curr-utterable-idxs propn-activns))]
    (for [i (range (first (mx/shape propn-mask)))
          :when #(< (rand) (mx/mget utterable-abs-activns i))]
      (map (:id-vec propn-net) nil))))


(defn choose-utterance
  "NEED REVISION SEE PREVIOUS FNS. Currently a noop. Given a converser-pair, a map with keys :speaker and 
  :listener, chooses a proposition from speaker's beliefs to communicate to 
  listener, and returns a conversation, i.e. a map with the proposition assoc'ed
  into the converser-pair map, with new key :propn"
  [pers]
  (if-let [poss-utterances (utterances-worth-saying pers)]
    (incant/sample poss-utterances :size 1)
    nil))


(def choose-conversations (comp choose-utterance choose-person-conversers))
;(defn choose-conversations
;  "Given a sequence of persons, returns a sequence of conversations, i.e.
;  maps with keys :speaker, :listener, and :propn, indicating that speaker
;  will communicate propn to listener."
;  [persons]
;  (map choose-utterance 
;       (mapcat choose-person-conversers persons)))

;(defn choose-thought
;  "Currently a noop: Returns a dummy proposition."
;  [pers]
;  (lot/->Propn (lot/->Pred :TODO) [] :TODO))
  

;; TODO this or some other function will eventually have to add in other effects
;; on the proposition network in order to add/subtract activation via weight to
;; the SALIENT node.
(defn receive-propn!
  "ADD DOCSTRING"
  [pers recd-propn-id]
  (when (propn-still-masked? pers recd-propn-id) ; if recd propn already unmasked, the rest is already done
    (add-to-propn-net! pers recd-propn-id)
    (let [propn-to-extended-fams-ids (:propn-to-extended-fams-ids (:propn-net pers))
          fams (propn-to-extended-fams-ids recd-propn-id)]
      (doseq [fam fams                           ; loop through all extended fams containing this propn
              propn fam]                         ; and each propn in that family
        (try-add-to-analogy-net! pers propn)))))  ; see whether we can now add analogies using it
;; This last loop redundantly tries to add analogies for recd-propn-id repeatedly, though will not do much after the first time

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
