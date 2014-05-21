(ns popco.communic.send
  (:require [utils.general :as ug]
            ;[clojure.pprint :as pp] ; only if needed for cl-format
            [popco.core.lot :as lot]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]
            [incanter.stats :as incant]))

(declare choose-listeners worth-saying-idxs choose-what-to-say-idxs 
         choose-transmissions)

(defn choose-listeners
  "Given a person as argument, return a sequence of persons to whom
  the argument person wants to talk on this tick."
  [{:keys [talk-to-persons max-talk-to]}]
  (if (>= max-talk-to (count talk-to-persons))
    talk-to-persons
    (ug/sample talk-to-persons :size max-talk-to :replacement false)))

(defn worth-saying-idxs
  "ADD DOCSTRING"
  [{:keys [propn-net propn-mask propn-activns utterable-mask]}]
  ;; absolute values of activns of unmasked utterable propns:
  (let [propn-id-vec (:id-vec propn-net)
        utterable-abs-activns (mx/abs
                                (mx/emul propn-mask utterable-mask propn-activns))]
    (for [i (range (mx/dimension-count utterable-abs-activns 0))
          :when #(< (rand) (mx/mget utterable-abs-activns i))]
      i)))

(defn choose-what-to-say-idxs
  "FIX DOCSTRING: Given a converser-pair, a map with keys :speaker and 
  :listener, chooses a proposition from speaker's beliefs to communicate to 
  listener, and returns a conversation, i.e. a map with the proposition assoc'ed
  into the converser-pair map, with new key :propn"
  [pers num-utterances]
  (if-let [poss-utterance-idxs (worth-saying-idxs pers)]
    (ug/sample poss-utterance-idxs :size num-utterances :replacement true)
    nil))

(defn transmit-utterances
  "ADD DOCSTRING"
  [pers]
  (let [id-to-idx (:id-to-idx (:propn-net pers))
        propn-activns (:propn-activns pers)
        listeners (choose-listeners pers)
        to-say-idxs (choose-what-to-say-idxs pers (count listeners))
        to-say-idx-activn-pairs (map #(vector % (mx/mget propn-activns %)) 
                                     to-say-idxs)]
    [pers (map hash-map listeners to-say-idx-activn-pairs)]))
