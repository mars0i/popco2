(ns popco.io.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn example
  "Based on https://github.com/clojure/data.csv"
  [pers]
  (with-open [out-file (io/writer "out-file.csv")] 
    (csv/write-csv out-file 
                   (vector 
                     (map name (:id-vec (:propn-net pers)))
                     (matrix :persistent-vector (:propn-activns pers))))))

