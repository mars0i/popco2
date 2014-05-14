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

````clojure
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
 
;; ALTERNATE VERSIONS OF propn-activns-row

(defn alt0-propn-activns-row
  [popn]
  (vec
    (concat 
      (pmap
        person-propn-activns (:members popn)))))

(defn alt1-propn-activns-row
  [popn]
  (let [activn-vecs (map :propn-activns (:members popn))
        len-1 (dec (first (mx/shape (first activn-vecs))))] ; or use .length.  we can assume all vecs same length.
    (mx/matrix :persistent-vector
               (mx/join 
                 (map #(mx/subvector % 1 len-1) ; assumes SALIENT nodes are index 0
                      activn-vecs))))) 

(defn alt2-propn-activns-row
  [popn]
  (mx/join 
    (map (comp vec
               rest ; strip SALIENT nodes
               (partial mx/matrix :persistent-vector) 
               :propn-activns)
         (:members popn))))

(defn alt3-propn-activns-row
  [popn]
  (let [activn-vecs (map :propn-activns (:members popn))
        len-1 (dec (first (mx/shape (first activn-vecs))))] ; or use .length.  we can assume all vecs same length.
    (vec
      (concat
        (map #(mx/subvector % 1 len-1) ; strip SALIENT nodes (assumes they have index 0)
             activn-vecs)))))
````
