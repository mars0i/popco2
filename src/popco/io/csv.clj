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
 
(defn person-propn-names
  "Given a sequence of persons, return a sequence of strings containing
  \"personalized\" proposition names, i.e. with the person's name appended
  to the front of the proposition id string, with the form \"person_propn\".
  These are suitable for use as column names in a csv file containing data
  on proposition activations for all of the persons.  Note that the number
  of strings returned will be (number of persons X number of propositions)."
  [persons]
  (let [name-strs (map (comp name :nm) persons)
        id-strs (map name (rest (:id-vec (:propn-net (first persons)))))]
    (vector    ; write-csv wants a vector of vectors
      (concat 
        (for [name-str name-strs
              id-str id-strs]
          (str name-str "_" id-str))))))

;; TODO ? NOTE THIS ASSUMES THAT SALIENT IS FIRST. SHOULD I INSTEAD LOOK UP SALIENT'S LOCATION??
(defn person-propn-activns
  "Given a person, returns the activation values of its propositions other
  than SALIENT in the form of a Clojure vector.  Assumes that SALIENT is
  the first node."
  [pers]
  (rest 
    (mx/matrix :persistent-vector 
               (:propn-activns pers))))

(defn data-row
  [persons]
  (vector  ; write-csv wants a vector of vectors each time it's called
    (mapcat person-propn-activns persons)))

;; TODO Add tick number column
(defn write-propn-activns-csv
  ([popns]
   (write-propn-activns-csv popns false))
  ([popns append?]
   (with-open [w (io/writer "activns.csv" :append append?)] 
     (when-not append?
       (csv/write-csv w (person-propn-names (:members (first popns)))))
     (doseq [popn popns]
       (csv/write-csv w (data-row (:members popn)))))))
