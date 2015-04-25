;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Redefine popco.core.constants/analogy-to-propn-*-multiplier to half of the usual values
(ns sims.bali.analogy-to-propn-halved)

;; Override definition of trust in popco.core.constants.
;; Why it's OK to do this during initialization, and what to watch out for:
;; http://stackoverflow.com/questions/5181367/is-defn-thread-safe .
(in-ns 'popco.core.constants)
(def analogy-to-propn-pos-multiplier (* 0.5 analogy-to-propn-pos-multiplier))
(def analogy-to-propn-neg-multiplier (* 0.5 analogy-to-propn-neg-multiplier))
(in-ns 'analogy-to-propn-halved)
