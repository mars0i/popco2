;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.popco
  (:use [clojure.core.matrix :as mx]
        popco.core.main
        utils.general)
  ;[clojure.data :as da] ; for 'diff'
  (:require [clojure.tools.cli]    ; for making standalone version
            [utils.random :as ran]
            [popco.communic.listen :as cl]
            [popco.communic.speak :as cs]
            [popco.communic.utterance :as cu]
            [popco.core.constants :as cn]
            [popco.core.lot :as lot]
            [popco.core.person :as pers]
            [popco.core.population :as popn]
            [popco.core.reporters :as rpt]
            [popco.io.propncsv :as csv]
            [popco.io.gexf-static :as gxs]
            [popco.io.gexf-dynamic :as gxd]
            [popco.nn.analogy :as an]
            [popco.nn.matrix :as px]
            [popco.nn.nets :as nn]
            [popco.nn.pprint :as pp]
            [popco.nn.propn :as pn]
            [popco.nn.update :as up])
            ;popco.nn.testtools
            ;popco.test.popco1comp
  (:import [popco.core.lot Propn Pred Obj]
           [popco.core.person Person]
           [popco.core.population Population]
           [popco.nn.nets AnalogyNet PropnNet])
  (:gen-class)) ; for lein uberjar

;; set pretty-print width to terminal width
(set-pprint-width (Integer/valueOf (System/getenv "COLUMNS"))) ; or read-string

;; use one of these:
(mx/set-current-implementation :vectorz)
;(mx/set-current-implementation :ndarray)

(defn -main [& args]
  (let [[opts args banner] (clojure.tools.cli/cli args
                                ["-h" "--help" "Print this help"
                                 :default false :flag true])]
    (when (:help opts)
      (println banner))
    ;; FIXME add stuff here
    ))
