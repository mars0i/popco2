csv.clj notes
=======

### Laziness and write-csv

`write-propn-activns-csv` roughly writes line by line, similar to
`write-propn-activns-csv-by-line` (and old version).  This is explicit
in the latter, since it doseq's through the ticks, writing a line each
time.  But in the spit version:

1. `spit-csv` uses `write-csv`.

2. `write-csv` uses `write-csv*` which uses loop/recur to go through the rows:
https://github.com/clojure/data.csv/blob/b70b33d56c239972f3e1c53c3c4f1b786909e93f/src/main/clojure/clojure/data/csv.clj#L123

3. I use `map` to iterate through ticks, i.e. lazily.

4.  `propn-activns-row` uses `mapcat` to put together data from persons
in a tick, which is lazy) So it's only when `write-csv*` loops through
the rows that the ticks are realized.  So that even though it looks like
`write-propn-activns-csv` collects all of the data at once before
writing it, it doesn't.


### Versions of propn-activns-row

Alternate versions of `propn-activns-row` appear below.  Quick tests
with `time` suggest that there is no significant difference between them
worth exploring with Criterium.  If anything, it looks like the original
version might be very slightly faster.

````

;; ORIGINAL VERSION uses person-propn-activns
(defn propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (mapcat person-propn-activns (:members popn)))

(defn person-propn-activns
  "Given a person, returns the activation values of its propositions other
  than SALIENT in the form of a Clojure vector.  Assumes that SALIENT is
  the first node."
  [pers]
  (rest   ; strip SALIENT node
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
  (let [persons (:members popn)
        name-strs (map (comp name :nm) persons)
        id-strs (map name (rest (:id-vec (:propn-net (first persons)))))]
    (for [name-str name-strs
          id-str id-strs]
      (str name-str "_" id-str))))

;; ALTERNATE VERSIONS OF propn-activns-row

(defn alt0-propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (vec
    (concat 
      (pmap
        person-propn-activns (:members popn)))))

(defn alt1-propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (let [activn-vecs (map :propn-activns (:members popn))
        len-1 (dec (first (mx/shape (first activn-vecs))))] ; or use .length.  we can assume all vecs same length.
    (mx/matrix :persistent-vector
               (mx/join 
                 (map #(mx/subvector % 1 len-1) ; assumes SALIENT nodes are index 0
                      activn-vecs))))) 

;; ANOTHER NEW VERSION
;; Note: as of 5/2104, join in vectorz, i.e. in matrix_api.clj, appears to
;; convert into Clojure vectors and then use concat.
(defn alt2-propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (mx/join 
    (map (comp vec
               rest ; strip SALIENT nodes
               (partial mx/matrix :persistent-vector) 
               :propn-activns)
         (:members popn))))

(defn alt3-propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (let [activn-vecs (map :propn-activns (:members popn))
        len-1 (dec (first (mx/shape (first activn-vecs))))] ; or use .length.  we can assume all vecs same length.
    (vec
      (concat
        (map #(mx/subvector % 1 len-1) ; strip SALIENT nodes (assumes they have index 0)
             activn-vecs)))))

````
