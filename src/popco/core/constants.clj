;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.constants
  (require [utils.random :as ran]
           [utils.file :as uf]))
;; constants and other global parameters for use throughout popco

;; Consistent use of these facilitate allowing global change
;; of numeric types used instead of the default, i.e. java.lang.Double, with an implementation
;; that supports other number types, such as ndarray.

(def ^:const +data-dir+ "data")

;; These arent' constant per se, but will usually be the same for an entire session,
;; and this is a good, central place to put them in order to avoid cyclic dependencies.
(def session-id (ran/make-long-seed)) (println "Session id/seed:" session-id)
(def initial-rng (ran/make-rng session-id))
;; Now create a source file that will recreate this initial-rng later if desired:
(uf/make-dir-if-none +data-dir+)
(spit (str +data-dir+ "/restoreRNG" session-id ".clj")
      (clojure.string/join 
        "\n"
        [(str "(intern 'popco.core.constants 'session-id " session-id ")")
         "(println \"Session id/seed:\" popco.core.constants/session-id)"
         "(intern 'popco.core.constants 'initial-rng (utils.random/make-rng popco.core.constants/session-id))"
         "\n"]))

(def ^:const +warnings?+ true)
;(def ^:const +warnings?+ false)

;; java.lang.Double:

;; TODO: get rid of these from all of the source?  or not?  purpose is to allow alt numbers, but it's confusing
(def ^:const +zero+ 0.0) 
(def ^:const +one+  1.0)
(def ^:const +neg-one+ -1.0)

(def ^:const +settling-iters+ 5) ; default number of times to run through the settling algorithm in each tick
(def ^:const +decay+ 0.9)        ; amount to decay the old activn before adding inputs from other nodes

(def trust 0.05)       ; in popco.communic.receive, governs influence of utterances on listener's salient links

;; For explanation of these next two, see section "Belief network concepts and initialization",
;; page 12, item #1 in the "Moderate Role" paper about popco1.
;; (These vars were called *propn-excit-weight* and *propn-inhib-weight* in popco1.)
(def ^:const +analogy-to-propn-pos-multiplier+ 0.2)    ; *propn-excit-weight* in popco1
(def ^:const +analogy-to-propn-neg-multiplier+ 0.025)  ; *propn-inhib-weight* in popco1
(def ^:const +feeder-node-idx+ 0) ; "feeder", i.e. only for sending activn, i.e. index of SALIENT in propn net and SEMANTIC in analogy net

;; These next few intended to be used *only* in popco.nn.analogy:
(def ^:const +pos-link-increment+ 0.1)
(def ^:const +neg-link-value+ -0.2)
(def ^:const +sem-similarity-link-value+ 0.1) ; *ident-weight* in POPCO1: max abs wt for predicate semantic similarity
(def ^:const +analogy-max-wt+ 0.5) ; As in popco1: forces weights to be <= 0.5 as a kludge to avoid extreme cycling.
(def ^:const +analogy-node-init-activn+ 0.01)    ; *init-activ* in popco1.  Used by note-unit by default, but here used only for analogy net.

;; To be used *only* in popco.nn.propn:
(def ^:const +propn-node-init-activn+ 0.0)       ; *propn-init-activ* in popco1.

;; At present I'm not using these values from popco1 in popco2, since they do nothing:
;; (def ^:const +utterance-probability-increment+ 1)
;; (def ^:const +utterance-probability-multiplier+ 0)
;; Modify communic/seems-worth-saying to reflect them as in popco1's same-named function, if desired.
