(ns popco.nn.constants)
;; constants for use in neural network construction and updating

(def ^:const +settling-iters+ 5)  ; default number of times to run through the settling algorithm in each tick

(def ^:const +decay+ 0.9) ; amount to decay the old activn before adding inputs from other nodes

(def ^:const +analogy-to-propn-pos-multiplier+ 0.2)
(def ^:const +analogy-to-propn-neg-multiplier+ 0.025)
;; For explanation, see section "Belief network concepts and initialization",
;; page 12, item #1 in the "Moderate Role" paper about popco1.
;; These vars were called *propn-excit-weight* and *propn-inhib-weight* in popco1.
