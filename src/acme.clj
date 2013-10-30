
(defrecord Propn [pred args id])
;; pred: predicate - should be keyword with initial uppercase
;; args: array of argments, which could either be objects (keyword starting with "ob-") or names of other propositions
;; id: name for proposition, which should start with all upper domain id, then dash (e.g. :CV-blah-blah)
;; (question: should I consider putting propns raw as arguments??)
