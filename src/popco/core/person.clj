(ns popco.core.person
  (:require [utils.general :as ug]
            [popco.communic.listen :as cl]
            [popco.core.constants :as cn]
            [popco.nn.nets :as nn]
            [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]
            [clojure.core.matrix :as mx]))

;; Definition of person and related functions

;; TODO: Add specification of groups with which to communicate, using Kristen Hammack's popco1 code as a model
(defrecord Person [id 
                   propn-net    propn-mask    propn-activns 
                   analogy-net  analogy-mask  analogy-activns
                   analogy-idx-to-propn-idxs utterable-ids utterable-mask
                   groups  talk-to-groups  talk-to-persons
                   max-talk-to])
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
   :rand-idx-fn     - A function that behaves like rand-int, returning a random
                      integer in [0, n).  (Should not be accessed directly except
                      within the get-rand-idx-fn function, so that this internal
                      structure can be replaced with something more general if
                      needed.)")

(defn get-rand-idx-fn
  [pers]
  (:rand-idx-fn pers))

;; MAYBE: Consider making code below more efficient if popco is extended
;; to involve regularly creating new persons in the middle of simulation runs
;; e.g. with natural selection.

;; non-lazy
(defn make-person
  "Creates a person with name (id), propns with propn-ids, and a pre-constructed
  propn-net and analogy-net.  Uses propns to construct propn-mask and
  analogy-mask.  The propn-net passed in will not be used directly, but will be
  copied to make a propn-net with a new weight matrix (:wt-mat), since each 
  person may modify its own propn weight matrix.  The analogy net can be shared 
  with every other person, however, since this will not be modified.  (The 
  analogy mask might be modified.)"
  [id propns propn-net analogy-net utterable-ids groups talk-to-groups max-talk-to rand-idx-fn]
  (let [num-poss-propn-nodes (count (:node-vec propn-net))
        num-poss-analogy-nodes (count (:node-vec analogy-net))
        propn-ids (map :id propns)
        pers (->Person id 
                       (pn/clone propn-net)
                       (mx/zero-vector num-poss-propn-nodes)     ; propn-mask
                       (mx/zero-vector num-poss-propn-nodes)     ; propn-activns
                       analogy-net                                ; yup, analogy-net
                       (mx/zero-vector num-poss-analogy-nodes)   ; analogy-mask
                       (mx/zero-vector num-poss-analogy-nodes)   ; analogy-activns
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
                       rand-idx-fn)]
    ;; set up propn net and associated vectors:
    (doseq [propn-id propn-ids] (cl/add-to-propn-net! pers propn-id))                        ; unmask propn nodes
    (nn/set-mask! (:propn-mask pers) cn/+feeder-node-idx+ (/ cn/+one+ cn/+decay+))        ; special mask val to undo next-activn's decay on this node
    (mx/add! (:propn-activns pers) (mx/emul (:propn-mask pers) cn/+propn-node-init-activn+))       ; initial activns for unmasked propns
    (nn/set-activn! (:propn-activns pers) cn/+feeder-node-idx+ cn/+one+)                  ; salient node always has activn = 1
    ;; set up analogy net and associated vectors:
    (doseq [propn-id propn-ids] (cl/try-add-to-analogy-net! pers propn-id))                  ; unmask analogy nodes (better to fill propn mask first)
    (nn/set-mask! (:analogy-mask pers) cn/+feeder-node-idx+ (/ cn/+one+ cn/+decay+))     ; special mask val to undo next-activn's decay on this node
    (mx/add! (:analogy-activns pers) (mx/emul (:analogy-mask pers) cn/+analogy-node-init-activn+)) ; initial activns for unmasked analogy nodes
    (nn/set-activn! (:analogy-activns pers) cn/+feeder-node-idx+ cn/+one+)               ; semantic node always has activn = 1
    pers))


(defn clone
  "Accepts a single argument, a person, and returns a person containing fresh a
  copy of any internal structure one might want to mutate.  (Useful for creating 
  distinct persons rather than to update the same person for a new tick.)"
  [{:keys [id 
           propn-net    propn-mask    propn-activns 
           analogy-net  analogy-mask  analogy-activns
           analogy-idx-to-propn-idxs  utterable-ids  utterable-mask
           groups  talk-to-groups  talk-to-persons
           max-talk-to]}]
  (->Person id
            (pn/clone propn-net)
            (mx/clone propn-mask)
            (mx/clone propn-activns)
            analogy-net                ; should never change
            (mx/clone analogy-mask)
            (mx/clone analogy-activns)
            analogy-idx-to-propn-idxs  ; should never change
            utterable-ids              ; These last few are normal Clojure vecs,
            utterable-mask             ;  so
            groups                     ;  if we ever want to change them at runtime,
            talk-to-groups             ;  they'll have to be replaced anyway.           
            talk-to-persons
            max-talk-to  ; an integer
            'FIXME)) ; TODO generate new rng here

(defn new-person-from-old
  "Create a clone of person pers, but with name new-name, or a generated name,
  if not."
  ([pers] (new-person-from-old pers (keyword (gensym (name (:id pers))))))
  ([pers new-name] (assoc (clone pers) :id new-name)))

(defn propn-net-zeroed
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh, zeroed proposition network.  (Useful e.g. for updating pers's
  proposition network as a function of analogy net activations.)"
  [pers]
  (let [p-net (:propn-net pers)
        num-nodes (count (:node-seq p-net))]
    (assoc-in pers [:propn-net :wt-mat]
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
