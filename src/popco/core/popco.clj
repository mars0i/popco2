(ns popco.core.popco
  [:use clojure.core.matrix
        popco.core.acme
        popco.core.neuralnets
        popco.core.neuralnets-testtools
        utils.general]
  [:import [popco.core.acme Propn Obj]]
  (:gen-class))
;; cf. https://kotka.de/blog/2010/02/gen-class_how_it_works_and_how_to_use_it.html

;(println "Yow. Gettin' it all loaded!")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
