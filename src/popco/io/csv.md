csv.clj
=======

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
