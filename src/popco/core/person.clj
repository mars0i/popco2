;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Definition of person and related functions
(ns popco.core.person
  (:require [utils.string :as us]
            [utils.random :as ur]
            [popco.communic.listen :as cl]
            [popco.communic.speak :as cs]
            [popco.core.constants :as cn]
            [popco.nn.nets :as nn]
            [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]))


(defrecord Person [id 
                   propn-net 
                   analogy-net
                   analogy-idx-to-propn-idxs utterable-ids utterable-mask
                   groups  talk-to-groups  talk-to-persons
                   max-talk-to rng quality quality-fn bias-filter])
(us/add-to-docstr ->Person
   "Makes a POPCO Person, with these fields:
   :id -              name of person (a keyword)
   :propn-net -       PropnNet for this person
   :analogy-net -     AnalogyNet (same for all persons)
   :analogy-idxs-to-propn-idx - map from propn mapnode indexes in analogy net
                                to corresponding propn index pairs in propn net.
   :utterable-ids   - Ids of propositions that person is willing to say to others.
   :utterable-mask  - Indexes of propositions that person is willing to say to others.
   :groups          - Groups of which this person is a member, i.e. in virtue of
                      which someone might talk to the person. (popco1: groups)
   :talk-to-groups  - Groups whose members this person is willing to talk to.
                      (popco1: talks-to)
   :talk-to-persons - Other persons that this person talks to, determined by the
                      specification, at initalization, of the groups that this
                      person talks to.  i.e. this contains all members of the
                      groups in talk-to-groups. (no equivalent in popco1)
   :max-talk-to     - Maximum number of people that this person will talk to in
                      any one tick.  If >= (count talk-to-persons), has no 
                      effect.(popco1: num-listeners)
   :rng             - A random number generator object.  e.g. can be passed to rand-idx.
   :bias-filter     - Will decide who to listen to based on the 'quality' value passed in utterances.
   :quality-fn      - Assesses 'quality' of person, fills quality field with a value.
   :quality         - May contain a 'quality' to be passed in utterances for use by bias-filters.")

;; MAYBE: Consider making code below more efficient if popco is extended
;; to involve regularly creating new persons in the middle of simulation runs
;; e.g. with natural selection.

;; non-lazy
(defn make-person
  "Creates a person with name (id), propns, and a pre-constructed
  propn-net and analogy-net.  Uses propns to construct propn-mask and
  analogy-mask.  The propn-net passed in will not be used directly, but will be
  copied to make a propn-net with a new weight matrix (:all-wt-mat), since each 
  person may modify its own propn weight matrix.  The analogy net can be shared 
  with every other person, however, since this will not be modified.  (The 
  analogy mask might be modified.)  See docstring for ->Person for info on other
  arguments."
  ([id propns propn-net analogy-net utterable-ids groups talk-to-groups max-talk-to]
   (make-person id propns propn-net analogy-net utterable-ids groups talk-to-groups max-talk-to nil nil))
  ([id propns propn-net analogy-net utterable-ids groups talk-to-groups max-talk-to bias-filter quality-fn]
   (let [num-poss-propn-nodes (count (:node-vec propn-net))
         num-poss-analogy-nodes (count (:node-vec analogy-net))
         propn-ids (map :id propns)
         pers (->Person id 
                        (pn/clone propn-net)
                        (an/clone analogy-net)
                        (nn/make-analogy-idx-to-propn-idxs analogy-net propn-net) ; yes, analogy-idx-to-propn-idxs
                        (vec utterable-ids)
                        (let [utterable-mask (mx/zero-vector num-poss-propn-nodes)] ; utterable-mask is a core.matrix vector
                          (doseq [i (map (:id-to-idx propn-net) utterable-ids)]
                            (nn/unmask! utterable-mask i))
                          utterable-mask)
                        (vec groups)
                        (vec talk-to-groups)
                        nil  ; talk-to-persons will get filled when make-population calls update-talk-to-persons
                        max-talk-to
                        (ur/make-rng (ur/next-long cn/initial-rng))
                        bias-filter ; defaults to nil (see param lists above)
                        quality-fn  ; defaults to nil (see param lists above)
                        nil)]
     ;; set up propn net and associated vectors:
     (doseq [propn-id propn-ids] (cl/add-to-propn-net! (:propn-net pers) propn-id))                ; unmask propn nodes
     (nn/set-mask! (:mask (:propn-net pers)) cn/+feeder-node-idx+ (/ 1.0 cn/+decay+))        ; special mask val to undo next-activn's decay on this node
     (mx/add! (:activns (:propn-net pers)) (mx/emul (:mask (:propn-net pers)) cn/+propn-node-init-activn+)) ; initial activns for unmasked propns
     (nn/set-activn! (:activns (:propn-net pers)) cn/+feeder-node-idx+ 1.0)                  ; salient node always has activn = 1
     ;; set up analogy net and associated vectors:
     (doseq [propn-id propn-ids] (cl/try-add-to-analogy-net! pers propn-id))         ; unmask analogy nodes (better to fill propn mask first)
     (nn/set-mask! (:mask (:analogy-net pers)) cn/+feeder-node-idx+ (/ 1.0 cn/+decay+))     ; special mask val to undo next-activn's decay on this node
     (mx/add! (:activns (:analogy-net pers)) (mx/emul (:mask (:analogy-net pers)) cn/+analogy-node-init-activn+)) ; initial activns for unmasked analogy nodes
     (nn/set-activn! (:activns (:analogy-net pers)) cn/+feeder-node-idx+ 1.0)               ; semantic node always has activn = 1
     pers)))


(defn clone
  "Accepts a single argument, a person, and returns a person containing fresh a
  copy of any internal structure one might want to mutate.  (Useful for creating 
  distinct persons rather than to update the same person for a new tick.)"
  [{:keys [id 
           propn-net
           analogy-net
           analogy-idx-to-propn-idxs  utterable-ids  utterable-mask
           groups  talk-to-groups  talk-to-persons
           max-talk-to bias-filter quality-fn]}]
  (->Person id
            (pn/clone propn-net)
            (an/clone analogy-net) ; has new mask and activns, but shares the rest, including weight matrices
            analogy-idx-to-propn-idxs  ; should never change
            utterable-ids              ; These last few are normal Clojure vecs,
            utterable-mask             ;  so
            groups                     ;  if we ever want to change them at runtime,
            talk-to-groups             ;  they'll have to be replaced anyway.           
            talk-to-persons
            max-talk-to  ; an integer
            (ur/make-rng (ur/next-long cn/initial-rng))
            bias-filter
            quality-fn
            nil))

(defn new-person-from-old
  "Create a clone of person pers, but with name new-name, or a name generated
  using clojure.core/gensym, if not."
  ([pers] (new-person-from-old pers (keyword (gensym (name (:id pers))))))
  ([pers new-name] (assoc (clone pers) :id new-name)))

(defn new-person-seq-from-old
  "Create a lazy sequence of clones of person pers with ids that consist of
  pers's id plus \"0\", \"1\", \"2\", etc."
  ([pers] (new-person-seq-from-old pers 0))
  ([pers start]
   (let [old-name (name (:id pers))
         new-names (map (fn [nm i] (keyword (str nm (+ i start)))) ; adding n here shifts numbers up while remaining lazy
                        (repeat old-name) (range))]
     (map (partial new-person-from-old pers) new-names))))

(defn propn-net-zeroed
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh, zeroed proposition network.  (Useful e.g. for updating pers's
  proposition network as a function of analogy net activations.)"
  [pers]
  (let [p-net (:propn-net pers)
        num-nodes (count (:node-seq p-net))]
    (assoc-in pers [:propn-net :all-wt-mat]
              (mx/zero-matrix num-nodes num-nodes))))

(defn update-talk-to-persons
  "Fill person's talk-to-persons field based on its talk-to-groups field
  and the map group-to-persons that maps groups to their members."
  [group-to-persons pers]
  (assoc pers 
         :talk-to-persons
         (vec 
           (set 
             (remove #(identical? % (:id pers))  ; people don't talk to themselves in popco
                     (mapcat group-to-persons (:talk-to-groups pers)))))))
