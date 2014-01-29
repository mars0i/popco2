(ns tst
  (:require [popco.core.communic :as pc])
  (:use criterium.core
        clojure.core.matrix
        utils.general))

(def howmany 1000)
(def nums (range howmany))
(def veczvec (zero-vector howmany))

(defn unmaskit!
  [idx]
  (pc/unmask! veczvec idx))

(println "loaded.")

(println "domap:")
(bench (def _ (domap unmaskit! nums)))
(println "domapmany:")
(bench (def _ (domapmany unmaskit! nums)))
(println "domaprun:")
(bench (def _ (domaprun unmaskit! nums)))

