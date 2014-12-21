;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Utility functions having to do with math (see also utils.random)
(ns utils.math)

;; This is a slightly modified version of an example at clojure-doc.org:
(defn round2
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn sign-of [x] (if (neg? x) -1 1))

(defn clip-to-prob
  "Restict x to the probability range: Returns 0 if x < 0, 1 if x > 1,
  and x otherwise."
  [x]
  (max 0.0 (min 1.0 x)))
