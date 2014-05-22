(ns popco.core.constants)
;; constants for use throughout popco

;; Consistent use of these facilitate allowing global change
;; of numeric types used instead of the default, i.e. java.lang.Double, with an implementation
;; that supports other number types, such as ndarray.

;; java.lang.Double:

;; TODO: get rid of these from all of the source?  or not?  purpose is to allow alt numbers, but it's confusing
(def ^:const +zero+ 0.0) 
(def ^:const +one+  1.0)
(def ^:const +neg-one+ -1.0)

(def ^:const +settling-iters+ 5) ; default number of times to run through the settling algorithm in each tick
(def ^:const +decay+ 0.9)        ; amount to decay the old activn before adding inputs from other nodes

(def ^:const +trust+ 0.05)       ; in popco.communic.receive, governs influence of utterances on listener's salient links

;; For explanation of these next two, see section "Belief network concepts and initialization",
;; page 12, item #1 in the "Moderate Role" paper about popco1.
;; (These vars were called *propn-excit-weight* and *propn-inhib-weight* in popco1.)
(def ^:const +analogy-to-propn-pos-multiplier+ 0.2)    ; *propn-excit-weight* in popco1
(def ^:const +analogy-to-propn-neg-multiplier+ 0.025)  ; *propn-inhib-weight* in popco1
(def ^:const +semantic-node-index+ 0)
(def ^:const +salient-node-index+ 0)

;; These next few intended to be used *only* in popco.nn.analogy:
(def ^:const +pos-link-increment+ 0.1)
(def ^:const +neg-link-value+ -0.2)
(def ^:const +sem-similarity-link-value+ 0.1) ; *ident-weight* in POPCO1: max abs wt for predicate semantic similarity
(def ^:const +analogy-max-wt+ 0.5) ; As in popco1: forces weights to be <= 0.5 as a kludge to avoid extreme cycling.
(def ^:const +analogy-node-init-activn+ 0.01)    ; *init-activ* in popco1.  Used by note-unit by default, but here used only for analogy net.

;; To be used *only* in popco.nn.propn:
(def ^:const +propn-node-init-activn+ 0.0)       ; *propn-init-activ* in popco1.

; Values from popco1, but at present I'm not using them in popco2,
;; since these values do nothing.  Modify communic/seems-worth-saying to
;; reflect them as in popco1's same-named function, if desired.
;(def ^:const +utterance-probability-increment+ 1)
;(def ^:const +utterance-probability-multiplier+ 0)
