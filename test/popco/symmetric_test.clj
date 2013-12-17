(ns tst
  (:use criterium.core
        popco.core.acme)
  (:require [clojure.core.matrix :as mx]))

;           (print (format "%s %s%n" i j))

;; On my MacBook Air, this is much slower--3X to 4X than the other versions that don't create a seq of indexes.
(defn comp-sym?
  "Returns true if matrix is symmetric, false otherwise."
  [m]
  (and (mx/square? m)
       (every? (fn [[i j]] 
                 (= (mx/mget m i j) (mx/mget m j i)))
               (let [dim (first (mx/shape m))]
                 (for [i (range dim)
                       j (range dim)
                       :when (> j i)] ; no need to test both i,j and j,i since we do both at once. always true for (= j i).
                   [i j])))))

(defn rec-sym?
  [m]
  (and (mx/square? m)
       (let [dim (first (mx/shape m))] ; 1 past the last valid index
         (letfn [(f [i j]
                   (cond 
                     (>= i dim) true  ; got all the way through--it's symmetric
                     (>= j dim) (recur (+ 1 i) (+ 2 i)) ; got through j's--start again with new i
                     (= (mx/mget m i j) 
                        (mx/mget m j i)) (recur i (inc j)) ; same, so check next pair
                     :else false))] ; not same, not symmetric. we're done.  
           (f 0 1)))))

;; On my MacBook Air, this is about 20% slower than rec-sym.  Why?
(defn loop-sym?
  [m]
  (and (mx/square? m)
       (let [dim (first (mx/shape m))]
         (loop [i 0 
                j 1]
           (cond 
             (>= i dim) true                    ; checked all i's, j's
             (>= j dim) (recur (+ 1 i) (+ 2 i)) ; checked j's: restart with new i
             (= (mx/mget m i j) 
                (mx/mget m j i)) (recur i (inc j)) ; i,j = j,i: try next j
             :else false))))) ; not equal: matrix isn't symmetric


(defn bench-mat 
  [m]
  (let [mv (mx/matrix :vectorz m)
        mn (mx/matrix :ndarray m)
        mp (mx/matrix :persistent-vector m)
        ; mc (matrix :clatrix m)
       ]

    (println "\n*** supplied matrix ***")

    (println "\nvectorz comp-sym?:")
    (bench (def _ (comp-sym? mv)))
    (println "\nvectorz rec-sym?:")
    (bench (def _ (rec-sym? mv)))
    (println "\nvectorz loop-sym?:")
    (bench (def _ (loop-sym? mv)))

    (println "\nndarray comp-sym?:")
    (bench (def _ (comp-sym? mn)))
    (println "\nndarray rec-sym?:")
    (bench (def _ (rec-sym? mn)))
    (println "\nndarray loop-sym?:")
    (bench (def _ (loop-sym? mn)))

    (println "\npersistent-vector comp-sym?:")
    (bench (def _ (comp-sym? mp)))
    (println "\npersistent-vector rec-sym?:")
    (bench (def _ (rec-sym? mp)))
    (println "\npersistent-vector loop-sym?:")
    (bench (def _ (loop-sym? mp)))

    ;(println "\nclatrix comp-sym?:")
    ;(bench (def _ (comp-sym? mc)))
    ;(println "\nclatrix rec-sym?:")
    ;(bench (def _ (rec-sym? mc)))
    ;(println "\nclatrix loop-sym?:")
    ;(bench (def _ (loop-sym? mc)))
  ))

(defn bench-zero
  [dim]
  (let [mv (mx/new-matrix :vectorz dim dim)
        mn (mx/new-matrix :ndarray dim dim)
        mp (mx/new-matrix :persistent-vector dim dim)
        ; mc (new-matrix :clatrix dim dim)
       ]
    (println "\n*** zero matrices ***")

;    (println "\nvectorz comp-sym?:")
;    (bench (def _ (comp-sym? mv)))
    (println "\nvectorz rec-sym?:")
    (bench (def _ (rec-sym? mv)))
    (println "\nvectorz loop-sym?:")
    (bench (def _ (loop-sym? mv)))

;    (println "\nndarray comp-sym?:")
;    (bench (def _ (comp-sym? mn)))
    (println "\nndarray rec-sym?:")
    (bench (def _ (rec-sym? mn)))
    (println "\nndarray loop-sym?:")
    (bench (def _ (loop-sym? mn)))

;    (println "\npersistent-vector comp-sym?:")
;    (bench (def _ (comp-sym? mp)))
    (println "\npersistent-vector rec-sym?:")
    (bench (def _ (rec-sym? mp)))
    (println "\npersistent-vector loop-sym?:")
    (bench (def _ (loop-sym? mp)))

    ;(println "\nclatrix comp-sym?:")
    ;(bench (def _ (comp-sym? mc)))
    ;(println "\nclatrix rec-sym?:")
    ;(bench (def _ (rec-sym? mc)))
    ;(println "\nclatrix loop-sym?:")
    ;(bench (def _ (loop-sym? mc)))
  ))
