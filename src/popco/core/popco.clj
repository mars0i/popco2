(ns popco.core.popco
  [:use clojure.core.matrix
        popco.core.person
        popco.core.lot
        popco.nn.nets
        popco.nn.analogy
        popco.nn.propn
        popco.nn.settle
        popco.nn.testtools
        popco.nn.pprint
        utils.general]
  [:import [popco.core.lot Propn Pred Obj]
           [popco.core.person Person]
           [popco.nn.nets AnalogyNet PropnNet]])
;; add :gen-class ?

;; set pretty-print width to terminal width
(set-pprint-width (Integer/valueOf (System/getenv "COLUMNS"))) ; or read-string

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
