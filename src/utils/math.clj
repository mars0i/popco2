;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Utility functions having to do with math (see also utils.random)
(ns utils.math
  (require [clojure.math.numeric-tower :as math]))

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

;; Generalized logistic function.

;; TODO: Change these into parametes of logistic
(def ^:const +logistic-growth-rate+ 35)  ; sometimes called B
(def ^:const +logistic-position+ .3) ; sometimes called M
(def ^:const +logistic-taughtness+ 1) ; sometimes called v

(def ^:const +e+ (. java.lang.Math E))
(def ^:const +logistic-upper-asymptote+ 1) ; sometimes called K
(def ^:const +logistic-lower-asymptote+ 0) ; sometimes called A

; Does same thing as *logistic-position*, but with exponential scale, and without interacting with growth rate:
(def ^:const +logistic-exponential-position+ 1) ; sometimes called Q

; MAIN DEFINITION
(defn logistic
  [x]
  (+
   +logistic-lower-asymptote+
   (/
    (- +logistic-upper-asymptote+ +logistic-lower-asymptote+)
    (math/expt 
      (inc (* +logistic-exponential-position+
              (exp +e+ (- (* +logistic-growth-rate+
                             (- x +logistic-position+))))))
      (/ 1 +logistic-taughtness+)))))


