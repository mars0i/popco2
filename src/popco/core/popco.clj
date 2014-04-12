(ns popco.core.popco
  [:use [clojure.core.matrix :as mx]
        ;[clojure.data :as da] ; for 'diff'
        popco.core.population
        popco.core.person
        popco.core.lot
        popco.core.communic
        popco.nn.nets
        popco.nn.analogy
        popco.nn.propn
        popco.nn.settle
        popco.nn.testtools
        popco.nn.pprint
        popco.test.popco1comp
        utils.general]
  [:import [popco.core.lot Propn Pred Obj]
           [popco.core.person Person]
           [popco.core.population Population]
           [popco.nn.nets AnalogyNet PropnNet]])
;; add :gen-class ?

;; set pretty-print width to terminal width
(set-pprint-width (Integer/valueOf (System/getenv "COLUMNS"))) ; or read-string

(mx/set-current-implementation :vectorz)
;; use one of these:
;(set-current-implementation :persistent-vector)
;(set-current-implementation :ndarray)
;(set-current-implementation :vectorz)
;(set-current-implementation :clatrix)

(def folks (atom (->Population 0 [])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
