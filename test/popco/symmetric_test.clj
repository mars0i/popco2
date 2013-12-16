(ns something
  [:use criterium.core
        clojure.core.matrix
        popco.core.acme])

(defn comp-sym?
  "Returns true if matrix is symmetric, false otherwise."
  [m]
  (and (mx/square? m)
       (every? (fn [[i j]] (= (mx/mget m i j) (mx/mget m j i)))
               (let [dim (first (mx/shape m))]
                 (for [i (range dim)
                       j (range dim)
                       :when (> j i)] ; no need to test both i,j and j,i since we do both at once. always true for (= j i).
                   [i j])))))

(defn rec-sym?
  [m]
  (let [dim (first (mx/shape m))] ; 1 past the last valid index
    (letfn [(f [m i j]
              (cond 
                (>= i dim) true  ; got all the way through--it's symmetric
                (>= j dim) (recur m (+ 1 i) (+ 2 i)) ; got through j's--start again with new i
                (= (mx/mget m i j) 
                   (mx/mget m j i)) (recur m i (inc j)) ; same, so check next pair
                :else false))] ; not same, not symmetric. we're done.  
      (f m 0 0))))

(defn bench-mat 
  [m]
  (let [mv (matrix :vectorz m)
        mn (matrix :ndarray m)
        mp (matrix :persistent-vector m)
        ; mc (matrix :clatrix m)
       ]

    (println "*** supplied matrix ***")

    (println "vectorz comp-sym?:")
    (bench (def _ (comp-sym? mv)))
    (println "vectorz rec-sym?:")
    (bench (def _ (rec-sym? mv)))

    (println "ndarray comp-sym?:")
    (bench (def _ (comp-sym? mn)))
    (println "ndarray rec-sym?:")
    (bench (def _ (rec-sym? mn)))

    (println "persistent-vector comp-sym?:")
    (bench (def _ (comp-sym? mp)))
    (println "persistent-vector rec-sym?:")
    (bench (def _ (rec-sym? mp)))

    ;(println "clatrix comp-sym?:")
    ;(bench (def _ (comp-sym? mc)))
    ;(println "clatrix rec-sym?:")
    ;(bench (def _ (rec-sym? mc)))
  ))

(defn bench-zero
  [dim]
  (let [mv (new-matrix :vectorz dim dim)
        mn (new-matrix :ndarray dim dim)
        mp (new-matrix :persistent-vector dim dim)
        ; mc (new-matrix :clatrix dim dim)
       ]
    (println "*** zero matrices ***")

    (println "vectorz comp-sym?:")
    (bench (def _ (comp-sym? mv)))
    (println "vectorz rec-sym?:")
    (bench (def _ (rec-sym? mv)))

    (println "ndarray comp-sym?:")
    (bench (def _ (comp-sym? mn)))
    (println "ndarray rec-sym?:")
    (bench (def _ (rec-sym? mn)))

    (println "persistent-vector comp-sym?:")
    (bench (def _ (comp-sym? mp)))
    (println "persistent-vector rec-sym?:")
    (bench (def _ (rec-sym? mp)))

    ;(println "clatrix comp-sym?:")
    ;(bench (def _ (comp-sym? mc)))
    ;(println "clatrix rec-sym?:")
    ;(bench (def _ (rec-sym? mc)))
  ))
