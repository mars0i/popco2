(ns popco.core.popco
  (:use [clojure.core.matrix :as mx]
        popco.core.main
        utils.general)
  ;[clojure.data :as da] ; for 'diff'
  (:require [popco.core.population :as popn]
            [popco.core.person :as pers]
            [popco.core.lot :as lot]
            [popco.core.communic :as cm]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.nn.update :as up]
            [popco.nn.constants :as nc]
            [popco.nn.pprint :as pp]
            [popco.io.csv :as csv])
  ;popco.nn.testtools
  ;popco.test.popco1comp
  (:import [popco.core.lot Propn Pred Obj]
           [popco.core.person Person]
           [popco.core.population Population]
           [popco.nn.nets AnalogyNet PropnNet]))
;; add :gen-class ?

;; set pretty-print width to terminal width
(set-pprint-width (Integer/valueOf (System/getenv "COLUMNS"))) ; or read-string

(mx/set-current-implementation :vectorz)
;; use one of these:
;(mx/set-current-implementation :persistent-vector)
;(mx/set-current-implementation :ndarray)
;(mx/set-current-implementation :vectorz)
;(mx/set-current-implementation :clatrix)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
