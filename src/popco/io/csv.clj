(ns popco.io.csv
  (:require [popco.core.constants :as cn]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.core.matrix :as mx]))


;; See csv.md for notes on laziness and performance.

;; ARE THESE FUNCTIONS EFFICIENT ENOUGH?

(declare column-names person-propn-activns propn-activns-row spit-csv write-propn-activns-csv)

(defn write-propn-activns-csv
  "Collects reads activns from a sequence of opulations into a large seq of 
  seqs, and then writes them all at once into a csv file.  Writes a header row
  first if ':append true' is an option.  (Does not return the popns: Since this
  function realizes the lazy seq of popns, not returning the sequence allows
  you to lose the head, etc.--less memory use, more speed.  If you want to use
  the sequence again, hold on to it elsewhere.)"
  [popns & options]
  (when-not (== 0 cn/+feeder-node-idx+) ; sanity check for person-propn-activns
    (throw (Exception. (format "This code assumes SALIENT node index is 0, but it's not."))))
  (let [append? (if options ((apply hash-map options) :append) nil)
        data (map propn-activns-row popns) ; pmap wouldn't help, because the popns comes from iterate, so you need earlier elements to get later ones (unless you doall it first)
        rows (if append?
               data 
               (cons (column-names (first popns)) ; if not appending, add header row
                     data))]
    (apply spit-csv "activns.csv" rows options))) ; could pass the hashmap to write, but spit-csv is convenient and should require separate args

(defn propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (mapcat person-propn-activns (:persons popn)))

;; NOTE THIS ASSUMES THAT SALIENT IS FIRST. There is a check for this assumption in write-propn-activns-csv.
(defn person-propn-activns
  "Given a person, returns the activation values of its propositions other
  than SALIENT in the form of a Clojure vector.  Assumes that SALIENT is
  the first node."
  [pers]
  (rest   ; strip SALIENT node (see sanity check exception in write-propn-activns-csv)
        (mx/matrix :persistent-vector 
                   (:propn-activns pers))))
 
(defn column-names
  "Given a sequence of persons, return a sequence of strings containing
  \"personalized\" proposition names, i.e. with the person's name appended
  to the front of the proposition id string, with the form \"person_propn\".
  These are suitable for use as column names in a csv file containing data
  on proposition activations for all of the persons.  Note that the number
  of strings returned will be (number of persons X number of propositions)."
  [popn]
  (let [persons (:persons popn)
        name-strs (map (comp name :id) persons)
        id-strs (map name (rest (:id-vec (:propn-net (first persons)))))]
    (for [name-str name-strs
          id-str id-strs]
      (str name-str "_" id-str))))

(defn spit-csv
  "Given a sequence of sequences of data, opens a file and writes to it
  using write-csv.  Options are those that can be passed to spit or writer."
  [f rows & options]
   (with-open [w (apply io/writer f options)]
     (csv/write-csv w rows)))
