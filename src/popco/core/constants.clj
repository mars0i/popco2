(ns popco.core.constants)
;; constants for use throughout popco

;; Consistent use of these facilitate allowing global change
;; of numeric types used instead of the default, i.e. java.lang.Double, with an implementation
;; that supports other number types, such as ndarray.

;; java.lang.Double:
(def ^:const +zero+ 0.0) 
(def ^:const +one+  1.0)
(def ^:const +neg-one+ -1.0)
(def ^:const +settling-iters+ 5) ; default number of times to run through the settling algorithm in each tick
(def ^:const +decay+ 0.9)        ; amount to decay the old activn before adding inputs from other nodes
;; For explanation of these next two, see section "Belief network concepts and initialization",
;; page 12, item #1 in the "Moderate Role" paper about popco1.
;; (These vars were called *propn-excit-weight* and *propn-inhib-weight* in popco1.)
(def ^:const +analogy-to-propn-pos-multiplier+ 0.2)
(def ^:const +analogy-to-propn-neg-multiplier+ 0.025)
;; These next few intended to be used *only* in popco.nn.analogy:
(def ^:const +pos-link-increment+ 0.1)
(def ^:const +neg-link-value+ -0.2)
(def ^:const +sem-similarity-link-value+ 0.1) ; *ident-weight* in POPCO1: max abs wt for predicate semantic similarity
(def ^:const +analogy-max-wt+ 0.5) ; As in popco1: forces weights to be <= 0.5 as a kludge to avoid extreme cycling.
(def ^:const +semantic-node-index+ 0)

;; java.math.BigDecimal:
;(def ^:const +zero+ 0.0M)
;(def ^:const +one+  1.0M)
;(def ^:const +neg-one+ -1.0M)
;(def ^:const +settling-iters+ 5M)
;(def ^:const +decay+ 0.9M)
;(def ^:const +analogy-to-propn-pos-multiplier+ 0.2M)
;(def ^:const +analogy-to-propn-neg-multiplier+ 0.025M)
;(def ^:const +pos-link-increment+ 0.1M)
;(def ^:const +neg-link-value+ -0.2M)
;(def ^:const +sem-similarity-link-value+ 0.1M)
;(def ^:const +analogy-max-wt+ 0.5M)
;(def ^:const +semantic-node-index+ 0M)

;; clojure.lang.BigInt and clojure.lang.Ratio:
;(def ^:const +zero+ 0N)
;(def ^:const +one+  1N)
;(def ^:const +neg-one+ -1N)
;(def ^:const +settling-iters+ 5N)
;(def ^:const +decay+ 9/10)
;(def ^:const +analogy-to-propn-pos-multiplier+ 1/5)
;(def ^:const +analogy-to-propn-neg-multiplier+ 1/40)
;(def ^:const +pos-link-increment+ 1/10)
;(def ^:const +neg-link-value+ -1/5)
;(def ^:const +sem-similarity-link-value+ 1/10)
;(def ^:const +analogy-max-wt+ 1/2)
;(def ^:const +semantic-node-index+ 0N)
