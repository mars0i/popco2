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

;; From https://github.com/clojure/tools.cli#example-usage
;; Needs to be fixed.
(defn usage [options-summary]
  (cons "This is my program. There are many like it, but this one is mine.
   Usage: program-name [options] action\n" options-summary))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n" 
       (apply str errors)))

(defn exit [status msgs]
  (apply println msgs)
  (System/exit status))

;; Note keys are "normally set to the keywordized name of the long option without the leading dashes." (http://clojure.github.io/tools.cli)
(def cli-options [["-h" "--help" "Print this help"]
                  ["-n" "--popn-ns NAMESPACE" "Namespace for population definition." :parse-fn symbol]
                  ["-r" "--run EXPRESSION"    "Clojure expression to execute."] ; better to avoid -e, since lein exec uses it
                  ["-p" "--popn POPN-NAME"    "Name of symbol referencing population." :default "popn" :parse-fn #(symbol (str "sim/" %))]
                 ])

(defmacro my-load-string [s] `(read-string ~s))

;(require (vector (symbol (System/getProperty "POPCOSIM")) :as 'sim))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (clojure.tools.cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage cli-options))
      errors (exit 1 (error-msg errors)))
    (println (:popn options))
    (println (:popn-ns options))
    (println (:run options))

    (require (vector (:popn-ns options) :as 'sim)) 
    (load-string (:run options)) ; read string and eval.
))
