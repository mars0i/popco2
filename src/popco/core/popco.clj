(ns popco.core.popco
  [:use clojure.core.matrix
        popco.nn.core
        popco.nn.analogy
        popco.nn.propn
        popco.nn.settle
        popco.nn.testtools
        utils.general]
  [:import [popco.core.lot Propn Pred Obj]])
;; add :gen-class ?

;; set pretty-print width to terminal width
(set-pprint-width (Integer/valueOf (System/getenv "COLUMNS"))) ; or read-string

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
