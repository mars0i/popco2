;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.netlogo
  (:require [popco.core.main :as mn]))

(def *popn* (atom nil))


(defn bali-init
  "Initialize population of 172 popco persons, which represent subaks."
  []
  42)

(defn bali-once
  [subak-ids]
  (reverse subak-ids)) ; testing
