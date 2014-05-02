(ns popco.io.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.core.matrix :as mx] ;; temporary for testing?
            [popco.core.main :as mn] ;; temporary for testing?
            ))

;; Based on https://github.com/clojure/data.csv.

;(defn example1
;  [pers]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w 
;                   (vector 
;                     (map name (:id-vec (:propn-net pers)))
;                     (mx/matrix :persistent-vector (:propn-activns pers))))))
;
;
;(defn example2
;  [pers]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w 
;                   [(map name (:id-vec (:propn-net pers)))
;                     (mx/matrix :persistent-vector (:propn-activns pers))])))
;
;(defn example3
;  [pers]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w [(map name (:id-vec (:propn-net pers)))])
;    (csv/write-csv w [(mx/matrix :persistent-vector (:propn-activns pers))])))
;
;(defn example4
;  [persons]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w [(map name (:id-vec (:propn-net (first persons))))])
;    (doseq [pers persons]
;      (csv/write-csv w [(mx/matrix :persistent-vector (:propn-activns pers))]))))

;; ARE THESE FUNCTIONS EFFICIENT ENOUGH?
 
(defn field-names
  [persons]
  (let [name-strs (map (comp name :nm) persons)
        id-strs (map name (rest (:id-vec (:propn-net (first persons)))))]
    (vector    ; write-csv wants a vector of vectors
      (concat 
        (for [name-str name-strs
              id-str id-strs]
          (str name-str "_" id-str))))))

(defn person-data
  [pers]
  (rest 
    (mx/matrix :persistent-vector 
               (:propn-activns pers))))

(defn data-row
  [persons]
  (vector  ; write-csv wants a vector of vectors
    (mapcat person-data persons)))

;; ATTEMPTS AT FULLY-FUNCTIONAL EXPERIMENTS:

;; DOESN'T WORK Throws closed stream exception
;; i.e. you can't embed a stream in a closure??
(defn many-times-with-csv1
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (field-names persons))
      (mn/many-times (comp write-data-row mn/ticker) popn))))

;; This one throws the closed stream exception, too.
(defn many-times-with-csv2
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (field-names persons))
      (map write-data-row
           (mn/many-times popn)))))

;; this one has same problem
(defn many-times-with-csv3
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (field-names persons))
      (mn/many-times write-data-row popn))))

;; IN THIS ONE THE CLOSURE SEEMS TO WORK.  What??
(defn mwe []
  (with-open [w (io/writer "foo.csv")] 
    (let [rows (repeatedly 3 #(vector (range 4)))
          write-row (fn [row] (csv/write-csv w row))]
      (doall
        (map write-row rows)))))

;; THIS ONE WORKS.  It adds doall, just like the mwe.
(defn many-times-with-csv4
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (field-names persons))
      (doall
        (take 10
              (map (comp write-data-row mn/ticker)
                   (mn/many-times popn)))))))

;; THIS ONE FAILS.  It has no doall.
(defn mwe2 []
  (with-open [w (io/writer "foo.csv")] 
    (let [rows (repeatedly 3 #(vector (range 4)))
          write-row (fn [row] (csv/write-csv w row))]
      (map write-row rows))))

;; LESSON:
;; Embedding a stream in a closure that's embedded in a lazy sequence
;; will fail if the lazy-sequence escapes out of the with-open block
;; without being fully realized, and you then try to realize part
;; of the sequence outside of the with-open block.

;; SUGGESTION:
;; Create the lazy popn sequence.  Then pass it to a function
;; that will realize and write, maybe mapping with doall up to
;; tick n, or or doseq-ing or dotimes-ing until tick n.
;; (Try to save the head if you want to do other stuff with it.)


;; THIS WORKS--i.e. as far as the printing part goes.
;; Because it doesn't use a closure, but rather visible scope??
;; Also I'm not making it return the next popn--it just runs
;; through them.
(defn many-times-with-csv
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          popns (mn/many-times popn)]
      (csv/write-csv w (field-names persons))
      (doseq [popn popns]
        (csv/write-csv w (data-row (:members popn)))
        (mn/ticker popn)))))
;; note nothing useful returned here
