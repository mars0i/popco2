(ns popco.test.crime3
  (:use clojure.test
        popco.nn.analogy
        popco.nn.propn
        popco.core.person
        popco.test.popco1comp
        sims.crime3.propns))
 
(def a-net (make-analogy-net crime-propns living-propns pos-link-increment neg-link-value sem-relats))
(def p-net (make-propn-net (concat crime-propns living-propns)))

(declare from-popco1)

(deftest test-crime3-analogy-nets-match
         (testing "Do popco 2 and popco 1 crime3 analogy nets match?")

         ;; all propns
         (is (let [this-person (make-person :crime-living all-propns p-net a-net)]
               (load-file "src/popco/test/FromPopco1CRIME-LIVING.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1)))

         ;; all crime, beast only
         (is (let [this-person (make-person :crime-beast (concat viral-crime-propns beastly-crime-propns beast-propns) p-net a-net)]
               (load-file "src/popco/test/FromPopco1CRIME-BEAST.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1)))

         ;;; all crime, virus only
         (is (let [this-person (make-person :crime-virus (concat viral-crime-propns beastly-crime-propns virus-propns) p-net a-net)]
               (load-file "src/popco/test/FromPopco1CRIME-VIRUS.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1)))

         ;;; all crime, no living
         (is (let [this-person (make-person :crime-noliving (concat viral-crime-propns beastly-crime-propns) p-net a-net)]
               (load-file "src/popco/test/FromPopco1CRIME-NOLIVING.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1)))

         ;;; viral crime, all living
         (is (let [this-person (make-person :viralcrime-living (concat viral-crime-propns virus-propns beast-propns) p-net a-net)]
               (load-file "src/popco/test/FromPopco1VIRALCRIME-LIVING.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1)))

         ;;; beastly crime, all living
         (is (let [this-person (make-person :beastlycrime-living (concat beastly-crime-propns virus-propns beast-propns) p-net a-net)]
               (load-file "src/popco/test/FromPopco1BEASTLYCRIME-LIVING.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1)))

         ;;;; no crime, all living
         (is (let [this-person (make-person :nocrime-living () p-net a-net)]
               (load-file "src/popco/test/FromPopco1NOCRIME-LIVING.clj")
               (person-analogy-net-matches-popco1 this-person from-popco1))))
