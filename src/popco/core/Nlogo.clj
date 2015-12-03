;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.Nlogo
  (:require [popco.core.main :as mn])
  (:gen-class
   :name popco.core.Nlogo
   :methods [#^{:static true} [once [ "[J" ] "[J" ] ])) ;; array of longs to array of longs

(defn -once 
  "Temporary test version--doesn't really do anything."
  [subaks]
  (reverse subaks))

