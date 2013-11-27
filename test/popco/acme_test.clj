(ns test.acme
  (:use popco.acme
        popco.sims.crime.crime3propns))

(def propn-indexes [0 1 2  5 6 7 8 9])
; vectors can be treated as functions of indexes:
(def v8  (map virus-propns       propn-indexes))
(def vc8 (map viral-crime-propns propn-indexes))
(def nns8 (make-acme-nn-stru v8 vc8 pos-link-increment))
