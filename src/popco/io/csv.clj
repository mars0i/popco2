(ns popco.io.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.core.matrix :as mx] ;; temporary for testing?
            [popco.core.main :as mn] ;; temporary for testing?
            ))


;; ARE THESE FUNCTIONS EFFICIENT ENOUGH?

;; LESSON FROM EXPERIMENTS:
;; Embedding a stream in a closure that's embedded in a lazy sequence
;; will fail if the lazy-sequence escapes out of the with-open block
;; without being fully realized, and you subsequently try to realize part
;; of the sequence outside of the with-open block.

;; SUGGESTION:
;; Create the lazy popn sequence.  Then pass it to a function
;; that will realize and write, maybe mapping with doall up to
;; tick n, or or doseq-ing or dotimes-ing until tick n.
;; (Try to save the head if you want to do other stuff with it.)
 
(defn column-names
  "Given a sequence of persons, return a sequence of strings containing
  \"personalized\" proposition names, i.e. with the person's name appended
  to the front of the proposition id string, with the form \"person_propn\".
  These are suitable for use as column names in a csv file containing data
  on proposition activations for all of the persons.  Note that the number
  of strings returned will be (number of persons X number of propositions)."
  [popn]
  (let [persons (:members popn)
        name-strs (map (comp name :nm) persons)
        id-strs (map name (rest (:id-vec (:propn-net (first persons)))))]
    (cons "tick"
          (for [name-str name-strs
                id-str id-strs]
            (str name-str "_" id-str)))))

;; TODO ? NOTE THIS ASSUMES THAT SALIENT IS FIRST. SHOULD I INSTEAD LOOK UP SALIENT'S LOCATION??
(defn person-propn-activns
  "Given a person, returns the activation values of its propositions other
  than SALIENT in the form of a Clojure vector.  Assumes that SALIENT is
  the first node."
  [pers]
  (rest   ; strip SALIENT node
        (mx/matrix :persistent-vector 
                   (:propn-activns pers))))

;; CONSIDER CHANGING map TO pmap, AND LIKEWISE FOR mapcat
(defn data-row
  [popn]
  (cons (:tick popn) 
        (mapcat person-propn-activns (:members popn))))

;; CONSIDER CHANGING map TO pmap, AND LIKEWISE FOR mapcat
(defn data-vec-of-rows
  [popns]
  (map 
    #(cons (:tick %) (mapcat person-propn-activns (:members %)))
    popns))

(defn write-propn-activns-lines-csv
  "Reads activns tick by tick from popns in a sequence, writing a row for each
  popn.  Writes a header row first."
  ([popns]
   (write-propn-activns-lines-csv popns false))
  ([popns append?]
   (with-open [w (io/writer "activns.csv" :append append?)] 
     (when-not append?
       (csv/write-csv w (vector (column-names (first popns)))))
     (doseq [popn popns]
       (csv/write-csv w (vector (data-row popn)))))))

;; WHAT ABOUT APPENDING ROWS? DON'T WANT HEADERS AGAIN.
(defn vec-of-rows
  "Creates a sequence of sequences of activns, one inner sequence for each tick,
  from popns in an input sequence.  Writes a header row first."
  [popns]
  (cons 
    (column-names (first popns))
    (data-vec-of-rows popns)))

(defn spit-csv
  "Given a sequence of sequences of data, opens a file and writes to it
  using write-csv.  Options are those that can be passed to spit or writer."
  [f rows & options]
   (with-open [w (apply io/writer f options)]
     (csv/write-csv w rows)))
