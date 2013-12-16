(ns something
  [:use criterium.core
        clojure.core.matrix
        popco.core.acme])

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
