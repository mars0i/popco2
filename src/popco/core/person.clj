(ns popco.core.person
  (:require [utils.general :as ug]
            [popco.core.communic :as cm]
            [popco.core.constants :as cn]
            [popco.nn.nets :as nn]
            [popco.nn.propn :as pn]
            [popco.nn.matrix :as pmx]
            [clojure.core.matrix :as mx]))

;; Definition of person and related functions

;; TODO: Add specification of groups with which to communicate, using Kristen Hammack's popco1 code as a model
(defrecord Person [nm 
                   propn-net propn-mask propn-activns 
                   analogy-net analogy-mask analogy-activns
                   analogy-idx-to-propn-idxs talk-to-groups talk-to-persons])
(ug/add-to-docstr ->Person
   "Makes a POPCO Person, with these fields:
   :id -              name of person (a keyword)
   :propn-net -       PropnNet for this person
   :propn-mask -      vector of 1's (propn is entertained) and 0's (it isn't)
   :propn-activns -   vector of activation values for nodes in propn net
   :analogy-net -     AnalogyNet (same for all persons)
   :analogy-mask -    vector of 1's (mapnode is present) or 0's (it's absent)
   :analogy-activns - activation values of nodes in analogy net
   :analogy-idxs-to-propn-idx - map from propn mapnode indexes in analogy net
                                to corresponding propn index pairs in propn net.
   :talk-to-groups  - Groups that this person talks to.  Should be used mainly
                      during initialization, and for information to user.
   :talk-to-persons - Other persons that this person talks to, determined by the
                      specification, at initalization, of the groups that this
                      person talks to.  i.e. this contains all members of the
                      groups in talk-to-groups.")

;; MAYBE: Consider making code below more efficient if popco is extended
;; to involve regularly creating new persons in the middle of simulation runs
;; e.g. with natural selection.

(defn make-person
  "Creates a person with name (nm), propns with propn-ids, and a pre-constructed
  propn-net and analogy-net.  Uses propns to construct propn-mask and
  analogy-mask.  The propn-net passed in will not be used directly, but will be
  copied to make a propn-net with a new weight matrix (:wt-mat), since each 
  person may modify its own propn weight matrix.  The analogy net can be shared 
  with every other person, however, since this will not be modified.  (The 
  analogy mask might be modified.)"
  [nm propns propn-net analogy-net]
  (let [num-poss-propn-nodes (count (:node-vec propn-net))
        num-poss-analogy-nodes (count (:node-vec analogy-net))
        propn-ids (map :id propns)
        pers (->Person nm 
                       (pn/clone propn-net)
                       (pmx/zero-vector num-poss-propn-nodes)     ; propn-mask
                       (pmx/zero-vector num-poss-propn-nodes)     ; propn-activns
                       analogy-net
                       (pmx/zero-vector num-poss-analogy-nodes)   ; analogy-mask
                       (pmx/zero-vector num-poss-analogy-nodes)   ; analogy-activns
                       (nn/make-analogy-idx-to-propn-idxs analogy-net propn-net)
                       nil ; temporary
                       nil ; temporary
                       )]

    ;; NEW VERSION
    ;; set up propn net and associated vectors:
    (doseq [propn-id propn-ids] (cm/add-to-propn-net! pers propn-id))                        ; unmask propn nodes
    (nn/set-mask! (:propn-mask pers) cn/+salient-node-index+ (/ cn/+one+ cn/+decay+))        ; special mask val to undo next-activn's decay on this node
    (mx/add! (:propn-activns pers) (mx/emul (:propn-mask pers) cn/+propn-node-init-activn+))       ; initial activns for unmasked propns
    (nn/set-activn! (:propn-activns pers) cn/+salient-node-index+ cn/+one+)                  ; salient node always has activn = 1

    ;; set up analogy net and associated vectors:
    (doseq [propn-id propn-ids] (cm/try-add-to-analogy-net! pers propn-id))                  ; unmask analogy nodes (better to fill propn mask first)
    (nn/set-mask! (:analogy-mask pers) cn/+semantic-node-index+ (/ cn/+one+ cn/+decay+))     ; special mask val to undo next-activn's decay on this node
    (mx/add! (:analogy-activns pers) (mx/emul (:analogy-mask pers) cn/+analogy-node-init-activn+)) ; initial activns for unmasked analogy nodes
    (nn/set-activn! (:analogy-activns pers) cn/+semantic-node-index+ cn/+one+)               ; semantic node always has activn = 1

    pers))


(defn clone
  "Accepts a single argument, a person, and returns a person containing fresh a
  copy of any internal structure one might want to mutate.  (Useful for creating 
  distinct persons rather than to update the same person for a new tick.)"
  [{nm :id propn-net :propn-net propn-mask :propn-mask propn-activns :propn-activns
    analogy-net :analogy-net analogy-mask :analogy-mask analogy-activns :analogy-activns
    analogy-idx-to-propn-idxs :analogy-idx-to-propn-idxs}]
  (->Person nm
            (pn/clone propn-net)
            (mx/clone propn-mask)
            (mx/clone propn-activns)
            analogy-net                 ; should never change
            (mx/clone analogy-mask)
            (mx/clone analogy-activns)
            analogy-idx-to-propn-idxs)) ; should never change

(defn new-person-from-old
  "Create a clone of person pers, but with name new-name, or a generated name,
  if not."
  ([pers] (new-person-from-old pers (keyword (gensym (name (:id pers))))))
  ([pers new-name] (assoc (clone pers) :id new-name)))

(defn propn-net-clone
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh copy of its proposition network.  (Useful e.g. for updating pers's
  proposition network as a function of analogy net activations.)"
  [pers]
  (assoc pers 
         :propn-net (pn/clone (:propn-net pers))))

(defn propn-net-zeroed
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh, zeroed proposition network.  (Useful e.g. for updating pers's
  proposition network as a function of analogy net activations.)"
  [pers]
  (let [p-net (:propn-net pers)
        num-nodes (count (:node-seq p-net))]
    (assoc-in pers [:propn-net :wt-mat]
              (pmx/zero-matrix num-nodes num-nodes))))

;; TEMPORARY
;; differs from make-person in using old-add-to-analogy-net as a sanity check
; (defn old-make-person
;   "Creates a person with name (nm), propns with propn-ids, and a pre-constructed
;   propn-net and analogy-net.  Uses propns to construct propn-mask and
;   analogy-mask.  Important: The propn-net passed in should be new, with a fresh
;   weight matrix (:wt-mat), since each person may modify its own propn weight
;   matrix.  The analogy net can be shared with every other person, however, since
;   this will not be modified.  (The analogy mask might be modified.)"
;   [nm propns propn-net analogy-net]
;   (let [num-poss-propn-nodes (count (:node-vec propn-net))
;         num-poss-analogy-nodes (count (:node-vec analogy-net))
;         propn-ids (map :id propns)
;         pers (->Person nm 
;                        propn-net
;                        (mx/zero-vector num-poss-propn-nodes)     ; propn-mask
;                        (mx/zero-vector num-poss-propn-nodes)     ; propn-activns
;                        analogy-net
;                        (mx/zero-vector num-poss-analogy-nodes)   ; analogy-mask
;                        (mx/zero-vector num-poss-analogy-nodes))] ; analogy-activns
;     (doseq [propn propns] (cm/add-to-propn-net pers (:id propn)))   ; better to fill propn mask before
;     (doseq [propn propns] (cm/old-add-to-analogy-net pers (:id propn))) ;  analogy mask, so propns are known
;     pers))
