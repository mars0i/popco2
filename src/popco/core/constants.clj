(ns popco.core.constants)
;; constants for use throughout popco

;; Consistent use of these instead of +zero+ and +one+ facilitate allowing global change
;; of numeric types used instead of the default, i.e. java.lang.Double, with an implementation
;; that supports other number types, such as ndarray.

;(def ^:const +zero+ 0.0) ; java.lang.Double
;(def ^:const +one+  1.0) ; java.lang.Double
;(def ^:const +neg-one+ -1.0) ; java.lang.Double
(def ^:const +zero+ 0N)   ; clojure.lang.BigInt, clojure.lang.Ratio
(def ^:const +one+  1N)   ; clojure.lang.BigInt, clojure.lang.Ratio
(def ^:const +neg-one+ -1N)   ; clojure.lang.BigInt, clojure.lang.Ratio
;(def ^:const +zero+ 0.0M) ; java.math.BigDecimal
;(def ^:const +one+  1.0M) ; java.math.BigDecimal
;(def ^:const +neg-one+ -1.0M) ; java.math.BigDecimal

;(def ^:const +settling-iters+ 5)  ; default number of times to run through the settling algorithm in each tick
(def ^:const +settling-iters+ 5N)

;(def ^:const +decay+ 0.9) ; amount to decay the old activn before adding inputs from other nodes
(def ^:const +decay+ 9/10)

;; For explanation, see section "Belief network concepts and initialization",
;; page 12, item #1 in the "Moderate Role" paper about popco1.
;; (These vars were called *propn-excit-weight* and *propn-inhib-weight* in popco1.)
;(def ^:const +analogy-to-propn-pos-multiplier+ 0.2)
(def ^:const +analogy-to-propn-pos-multiplier+ 1/5)
;(def ^:const +analogy-to-propn-neg-multiplier+ 0.025)
(def ^:const +analogy-to-propn-neg-multiplier+ 1/40)

;; These are intended to be used *only* in popco.nn.analogy:
;(def ^:const +pos-link-increment+ 0.1)
(def ^:const +pos-link-increment+ 1/10)
;(def ^:const +neg-link-value+ -0.2)
(def ^:const +neg-link-value+ -1/5)
;(def ^:const +sem-similarity-link-value+ 0.1) ; *ident-weight* in POPCO1: max abs wt for predicate semantic similarity
(def ^:const +sem-similarity-link-value+ 1/10) ; *ident-weight* in POPCO1: max abs wt for predicate semantic similarity
;(def ^:const +analogy-max-wt+ 0.5) ; As in popco1: forces weights to be <= 0.5 as a kludge to avoid extreme cycling.
(def ^:const +analogy-max-wt+ 1/2) ; As in popco1: forces weights to be <= 0.5 as a kludge to avoid extreme cycling.
;(def ^:const +semantic-node-index+ 0)
(def ^:const +semantic-node-index+ 0N)
