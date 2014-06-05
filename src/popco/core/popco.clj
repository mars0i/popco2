(ns popco.core.popco
  (:use [clojure.core.matrix :as mx]
        popco.core.main
        utils.general)
  ;[clojure.data :as da] ; for 'diff'
  (:require [utils.probability :as prob]
            [popco.core.population :as popn]
            [popco.core.person :as pers]
            [popco.core.lot :as lot]
            [popco.communic.utterance :as cu]
            [popco.communic.listen :as cl]
            [popco.communic.speak :as cs]
            [popco.nn.nets :as nn]
            [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.nn.update :as up]
            [popco.core.constants :as nc]
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

;; use one of these:
(mx/set-current-implementation :vectorz)
;(mx/set-current-implementation :ndarray)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
