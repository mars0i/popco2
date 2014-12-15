;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns popco.core.popco
  (:use popco.core.main)
  ;[clojure.data :as da] ; for 'diff'
  (:require [utils.general :as ug]
            [utils.random :as ur]
            [utils.file :as uf]
            [utils.math :as um]
            [utils.string :as us]
            [clojure.tools.cli]    ; for making standalone version
            [clojure.string]
            [clojure.core.matrix :as mx]
            ;[mikera.vectorz.matrix-api] ; needed for uberjar?
            [popco.communic.listen :as cl]
            [popco.communic.speak :as cs]
            [popco.communic.utterance :as cu]
            [popco.core.constants :as cn]
            [popco.core.lot :as lot]
            [popco.core.person :as pers]
            [popco.core.population :as popn]
            [popco.core.reporters :as rpt]
            [popco.io.propncsv :as csv]
            [popco.io.gexf :as gxd]
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
  (:gen-class)) ; for uberjar

;; set pretty-print width to terminal width
(if-let [colstr (System/getenv "COLUMNS")]
  (set-pprint-width (Integer/valueOf colstr)))

;; use one of these:
(mx/set-current-implementation :vectorz)
;(mx/set-current-implementation :ndarray)

;; Note keys are "normally set to the keywordized name of the long option without the leading dashes." (http://clojure.github.io/tools.cli)
(def cli-options [["-h" "--help" "Print this help"]
                  ["-i" "--initial <file>" "Will attempt to load Clojure source file <file>' before doing anything else. (Use for random seed files.)"]
                  ["-n" "--popn-ns <namespace>" "Namespace that defines a symbol 'popn with a popco population as its value." :parse-fn symbol]
                  ["-r" "--run <clojure expression>"    "Clojure expression to execute."] ; better to avoid -e, since lein exec uses it
                 ])

(defn usage [options]
  (let [fmt-line (fn [[short-opt long-opt desc]]
                   (str short-opt ", " long-opt ": " desc))
        addl-help ["Note: Symbol 'popns will automatically be defined to be (many-times popn)."
                   "Example usage:"
                   "lein run -n sims.crime3.threegroups -r '(rp/write-propn-activns-csv (take 5000 (mn/many-times sim/popn)) :cooker rp/cook-name-for-R)'" ]]
    (clojure.string/join "\n" (concat (map fmt-line options)
                                      addl-help))))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n" 
       (apply str errors)))

;; This will be executed when the program is invoked with 'lein run'.
;; It won't be executed when the program is invoked with 'lein repl'.
(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (clojure.tools.cli/parse-opts args cli-options)
        popn-ns-sym (:popn-ns options) ; symbol representing namespace in which the population, named popn, is defined
        to-run-str (:run options)     ; string of Clojure code to run
        initial-file (:initial options)]

    ;; Check for reasons to abort:
    (cond
      (or (:help options) 
          (not popn-ns-sym)
          (not to-run-str))  (do (println (usage cli-options))
                                 (System/exit 0))
      errors                 (do (println (error-msg errors))
                                 (System/exit 1)))



    (when initial-file
      (load-file initial-file))
    ;; We have to define aliases here for load-string to see them.  I don't understand why.  Short aliases are handy.
    (require '[popco.core.main :as mn])
    (require '[popco.core.reporters :as rp])
    (require (vector (:popn-ns options) :as 'sim)) 
    ;(load-string "(def popns (popco.core.main/many-times sim/popn))") ; don't do this--holds onto the head
    (load-string (str "(do "
                      (:run options)
                      "(println) (System/exit 0))" )) ))
