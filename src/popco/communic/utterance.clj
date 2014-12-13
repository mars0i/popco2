;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.communic.utterance
  (:require [clojure.core.matrix :as mx]
            [utils.general :as ug]))

(defrecord Utterance [propn-id valence speaker-id speaker-quality])

(ug/add-to-docstr ->Utterance
  "Make an utterance to be communicated to a listener.
  :propn-id -        id for propn being communicated.
  :valence -         Effect of utterance on speaker is proportional to this.
                     Originally, 1 or -1, but other values in [-1,1] are possible.
  :speaker-id -      id of person uttering the proposition.  Has no effect, but
                     provides useful information for someone observing the program.
  :speaker-quality - nil, or a value representing a property for model-bias comparisons
                     on behalf of the listener in communic/listener.  e.g. this field's
                     value could represent success, skill, prestige, or a property to be
                     used for similarity comparisons.  For more complex bias calculations,
                     such as a flexible similarity calculation, the whole person could be
                     passed in this field.")

(defn utterance-valence
  [speaker propn-id]
  (let [pnet (:propn-net speaker)
        id-to-idx (:id-to-idx pnet)
        propn-activns (:activns pnet)]
    (ug/sign-of (mx/mget propn-activns (id-to-idx propn-id)))))
    
(defn make-utterance
  ([speaker propn-id] (make-utterance speaker propn-id nil))
  ([speaker propn-id speaker-quality]
   (->Utterance propn-id 
                (utterance-valence speaker propn-id)
                (:id speaker)
                speaker-quality)))

