(ns popco.core.popco
  (:use [clojure.core.matrix :as mx]
        popco.core.main
        utils.general)
  ;[clojure.data :as da] ; for 'diff'
  (:require [popco.core.population :as popn]
            [popco.core.person :as pers]
            [popco.core.lot :as lot]
            [popco.core.communic :as com]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.nn.update :as up]
            [popco.nn.constants :as nc]
            [popco.nn.pprint :as pp])
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
;(set-current-implementation :persistent-vector)
;(set-current-implementation :ndarray)
;(set-current-implementation :vectorz)
;(set-current-implementation :clatrix)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
