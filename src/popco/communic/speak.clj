(ns popco.communic.speak
  (:require [utils.random :as ran]
            ;[clojure.pprint :as pp] ; only if needed for cl-format
            [popco.core.lot :as lot]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [popco.communic.utterance :as ut]
            [clojure.core.matrix :as mx]))

(declare choose-listeners worth-saying-ids choose-propn-ids-to-say
         make-utterances speaker-plus-utterances)

(defn choose-listeners
  "Given a person as argument, return a sequence of persons to whom
  the argument person wants to talk on this tick."
  [{:keys [talk-to-persons max-talk-to rng]}]
  (if (>= max-talk-to (count talk-to-persons))
    talk-to-persons
    (ran/sample-without-repl rng max-talk-to talk-to-persons)))

(defn worth-saying-ids
  "Given a Person, returns ids of propositions that the person might be willing
  to say to someone at this time.  The set of selected propositions is a subset
  of the intersection of the propositions currently entertained by the person
  (ones not masked in propn-mask) and the propositions that the person is 
  willing to communicate (ones unmasked in utterable-mask).  Each proposition
  in this intersection is then selected with probability equal to the absolute
  value of its activation."
  [{:keys [propn-net propn-mask propn-activns utterable-mask]}]
  ;; absolute values of activns of unmasked utterable propns:
  (let [propn-id-vec (:id-vec propn-net)
        utterable-abs-activns (mx/abs
                                (mx/emul propn-mask utterable-mask propn-activns))]
    (for [i (range (count propn-id-vec))
          :let [randnum (rand)
                activn (mx/mget utterable-abs-activns i)]
          :when (< randnum activn)]
      (propn-id-vec i))))

(defn choose-propn-ids-to-say
  "FIX DOCSTRING: Given a converser-pair, a map with keys :speaker and 
  :listener, chooses a proposition from speaker's beliefs to communicate to 
  listener, and returns a conversation, i.e. a map with the proposition assoc'ed
  into the converser-pair map, with new key :propn"
  [speaker num-utterances]
  (if (pos? num-utterances)
    (if-let [poss-utterance-ids (seq (worth-saying-ids speaker))]  ; since sample throws exception on empty coll
      (ran/sample-with-repl (:rng speaker) num-utterances poss-utterance-ids)
      nil)
    nil))

;; TODO: filter out SALIENT--don't send it
(defn make-utterances
  "Given a person, returns a Clojure map representing utterances to
  persons, i.e. a map from persons who are listeners--i.e. persons
  who the current person is trying to speak to--to utterances to be
  conveyed from the current person to each of those listener.
  Utterances are sequences in which the first element represents
  a proposition [TODO: as id, or index?], and the second element
  captures the way in which the proposition should influence the
  listener [TODO: raw or cooked activation?]." ; FIXME
  [speaker]
  (let [listeners (choose-listeners speaker) ; may be empty
        to-say-ids (choose-propn-ids-to-say speaker (count listeners))] ; nil if no listeners, possibly nil if so
    (if to-say-ids
      (zipmap listeners 
              (map #(vector (ut/make-utterance speaker %)) to-say-ids))
      {})))

;; So person will get passed through
(defn speaker-plus-utterances
  [pers]
  [pers (make-utterances pers)])
