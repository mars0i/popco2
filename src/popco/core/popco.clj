;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.popco
  (:use popco.core.main
        utils.general)
  ;[clojure.data :as da] ; for 'diff'
  (:require [clojure.tools.cli]    ; for making standalone version
            [clojure.string]
            [clojure.core.matrix :as mx]
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

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n" 
       (apply str errors)))

;; Note keys are "normally set to the keywordized name of the long option without the leading dashes." (http://clojure.github.io/tools.cli)
(def cli-options [["-h" "--help" "Print this help"]
                  ["-n" "--popn-ns <namespace>" "Namespace that defines a symbol 'popn with a popco population as its value." :parse-fn symbol]
                  ["-r" "--run <clojure expression>"    "Clojure expression to execute."] ; better to avoid -e, since lein exec uses it
                 ])

(defn usage [options]
  (let [fmt-line (fn [[short-opt long-opt desc]]
                   (str short-opt ", " long-opt ": " desc))
        addl-help ["Note: Symbol 'popns will automatically be defined to be (many-times popn)."
                   "Example usage:"
                   "lein run -n sims.crime3.hermits -r '(popco.core.reporters/write-propn-activns-csv (take 100 (map popco.core.reporters/ticker popns)))'"]]
    (clojure.string/join "\n" (concat (map fmt-line options)
                                      addl-help))))

;(require (vector (symbol (System/getProperty "POPCOSIM")) :as 'sim))

;; This will be executed when the program is invoked with 'lein run'.
;; It won't be executed when the program is invoked with 'lein repl'.
(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (clojure.tools.cli/parse-opts args cli-options)
        popn-ns-sym (:popn-ns options)
        to-run-str (:run options)]
    (cond
      (or (:help options) 
          (not popn-ns-sym)
          (not to-run-str))  (do (println (usage cli-options))
                                 (System/exit 0))
      errors                 (do (println (error-msg errors))
                                 (System/exit 1)))

    (require (vector (:popn-ns options) :as 'sim)) 
    (load-string "(def popns (popco.core.main/many-times sim/popn))") ; otherwise compiled too early to know about sim
    (load-string (str "(do "
                      (:run options)
                      "(println) (System/exit 0))" )) ))
