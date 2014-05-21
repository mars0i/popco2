(ns popco.communic.send
  (:require [utils.general :as ug]
            ;[clojure.pprint :as pp] ; only if needed for cl-format
            [popco.core.lot :as lot]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]
            [incanter.stats :as incant]))

(declare choose-listeners transmit-utterances choose-person-conversers
         utterances-worth-saying choose-utterances choose-transmissions)

;(defn communicate
;  "Implements a single timestep's (tick's) worth of communication.  Given a
;  sequence of persons, constructs conversations and returns the persons, updated
;  to reflect the conversations."
;  [persons & trans-repts]
;  (transmit-utterances persons 
;                       ((ug/comp* trans-repts) (choose-conversations persons))))

(defn choose-listeners
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


(defn utterances-worth-saying
  [{:keys [propn-net propn-mask propn-activns utterable-mask]}]
  ;; absolute values of activns of unmasked utterable propns:
  (let [utterable-abs-activns (mx/abs (mx/emul propn-mask utterable-mask propn-activns))
        propn-id-vec (:id-vec propn-net)]
    (for [i (range (mx/dimension-count utterable-abs-activns))
          :when #(< (rand) (mx/mget utterable-abs-activns i))]
      (nth propn-id-vec i))))

;; TODO should I pass out the activns here too?

(defn choose-utterances
  "NEED REVISION SEE PREVIOUS FNS. Currently a noop. Given a converser-pair, a map with keys :speaker and 
  :listener, chooses a proposition from speaker's beliefs to communicate to 
  listener, and returns a conversation, i.e. a map with the proposition assoc'ed
  into the converser-pair map, with new key :propn"
  [pers num-utterances]
  (if-let [poss-utterances (utterances-worth-saying pers)]
    (incant/sample poss-utterances :size num-utterances :replacement true)
    nil))

;; TODO
(defn choose-transmissions
  [pers]
  (let [listeners (choose-listeners pers)
        propns (choose-utterances pers (count listeners))
        ;; now get activns
        ]
    ;; now return them as trios of some kind
    ))



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
