(ns popco.core.popco
  [:use clojure.core.matrix
        popco.core.acme
        popco.core.neuralnets
        popco.core.neuralnets-testtools
        utils.general]
  [:import [popco.core.lot Propn Pred Obj]]
  (:gen-class))
;; cf. https://kotka.de/blog/2010/02/gen-class_how_it_works_and_how_to_use_it.html

;; set pretty-print width to terminal width
(set-pprint-width (Integer/valueOf (System/getenv "COLUMNS"))) ; or read-string

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
