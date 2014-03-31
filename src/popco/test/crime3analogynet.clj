(ns popco.test.crime3analogynet
  (:use popco.nn.analogy
        popco.nn.propn
        popco.core.person
        popco.test.popco1comp
        sims.crime3.propns))
 
(def a-net (make-analogy-net crime-propns living-propns pos-link-increment neg-link-value sem-relats))
(def p-net (make-propn-net (concat crime-propns living-propns)))

(def jo (make-person :jo all-propns p-net a-net))
(def job (make-person :job (concat crime-propns beast-propns) p-net a-net))
(def jov (make-person :jov (concat crime-propns virus-propns) p-net a-net))


;; all propns
(load-file "src/popco/test/FromPopco1CRIME-LIVING.clj")
(let [this-person (make-person :crime-living all-propns p-net a-net)]
  (println (person-analogy-net-matches-popco1 this-person from-popco1)))

;; all crime, beast only
;(make-person :crime-beast (concat viral-crime-propns beastly-crime-propns beast-propns) p-net a-net )
;;; all crime, virus only
;(make-person :crime-virus (concat viral-crime-propns beastly-crime-propns virus-propns) p-net a-net )
;;; all crime, no living
;(make-person :crime-noliving (concat viral-crime-propns beastly-crime-propns) p-net a-net )
;;; viral crime, all living
;(make-person :viralcrime-living (concat viral-crime-propns virus-propns beast-propns) p-net a-net )
;;; beastly crime, all living
;(make-person :beastlycrime-living (concat beastly-crime-propns virus-propns beast-propns) p-net a-net )
;;;; no crime, all living
;(make-person :nocrime-living ()

