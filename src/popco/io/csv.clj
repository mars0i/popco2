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
    ;(cons "tick"
          (for [name-str name-strs
                id-str id-strs]
            (str name-str "_" id-str))));)

;; TODO ? NOTE THIS ASSUMES THAT SALIENT IS FIRST. SHOULD I INSTEAD LOOK UP SALIENT'S LOCATION??
(defn person-propn-activns
  "Given a person, returns the activation values of its propositions other
  than SALIENT in the form of a Clojure vector.  Assumes that SALIENT is
  the first node."
  [pers]
  (rest   ; strip SALIENT node
        (mx/matrix :persistent-vector 
                   (:propn-activns pers))))

;; Consider redefining mapcat in terms of pmap??
(defn propn-activns-row
  [popn]
  ;(cons (:tick popn) 
        (mapcat person-propn-activns (:members popn)));)

;(defn propn-activn-tick-rows
;  [popns]
;  (map 
;    #(cons (:tick %) (mapcat person-propn-activns (:members %)))
;    popns))

(defn spit-csv
  "Given a sequence of sequences of data, opens a file and writes to it
  using write-csv.  Options are those that can be passed to spit or writer."
  [f rows & options]
   (with-open [w (apply io/writer f options)]
     (csv/write-csv w rows)))

;; NOTE:
;; write-propn-activns-csv roughly writes line by line, similar to write-propn-activns-csv-by-line.
;; This is explicity in the latter, since it doseq's through the ticks, writing a line
;; each time.  But in the spit version:
;; (a) spit-csv uses write-csv
;; (b) write-csv uses write-csv* which uses loop/recur to go through the rows: https://github.com/clojure/data.csv/blob/b70b33d56c239972f3e1c53c3c4f1b786909e93f/src/main/clojure/clojure/data/csv.clj#L123
;; (c) I use map to iterate through ticks, i.e. lazily
;;     (also propn-activns-row uses mapcat to put together data from persons in a tick, which is lazy)
;; So it's only at when write-csv* loops through the rows that the ticks are realized.
;; So that even though it looks like write-propn-activns-csv collects all of the data at once before writing it, it doesn't.
;;
(defn write-propn-activns-csv
  "Collects reads activns from a sequence of opulations into a large seq of 
  seqs, and then writes them all at once into a csv file.  Writes a header row
  first if ':append true' is an option.  (Does not return the popns: Since this
  function realizes the lazy seq of popns, not returning the sequence allows
  you to lose the head, etc.--less memory use, more speed.  If you want to use
  the sequence again, hold on to it elsewhere.)"
  [popns & options]
  (let [append? (when options ((apply hash-map options) :append)) ; get val of :append if present, else nil
        data (map propn-activns-row popns) ; pmap wouldn't help, because the popns comes from iterate, so you need earlier elements to get later ones (unless you doall it first)
        rows (if append?
               data 
               (cons (column-names (first popns)) ; if not appending, add header row
                     data))]
    (apply spit-csv "activns.csv" rows options))) ; could pass the hashmap to write, but spit-csv is convenient and should require separate args
