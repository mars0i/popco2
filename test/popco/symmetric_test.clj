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
        mc (mx/matrix :clatrix m)
       ]

    (println "\n*** supplied matrix ***")

    ;(println "\nvectorz comp-sym?:")
    ;(bench (def _ (comp-sym? mv)))
    ;(println "\nvectorz rec-sym?:")
    ;(bench (def _ (rec-sym? mv)))
    ;(println "\nvectorz loop-sym?:")
    ;(bench (def _ (loop-sym? mv)))
    ;(println "\nvectorz symmetric?:")
    ;(bench (def _ (mx/symmetric? mv)))

    ;(println "\nndarray comp-sym?:")
    ;(bench (def _ (comp-sym? mn)))
    ;(println "\nndarray rec-sym?:")
    ;(bench (def _ (rec-sym? mn)))
    ;(println "\nndarray loop-sym?:")
    ;(bench (def _ (loop-sym? mn)))
    (println "\nndarray symmetric?:")
    (bench (def _ (mx/symmetric? mn)))
    (println "\nndarray loop-symmetric?:")
    (bench (def _ (mx/loop-symmetric? mn)))

    ;(println "\npersistent-vector comp-sym?:")
    ;(bench (def _ (comp-sym? mp)))
    ;(println "\npersistent-vector rec-sym?:")
    ;(bench (def _ (rec-sym? mp)))
    ;(println "\npersistent-vector loop-sym?:")
    ;(bench (def _ (loop-sym? mp)))
    (println "\npersistent-vector symmetric?:")
    (bench (def _ (mx/symmetric? mp)))
    (println "\npersistent-vector loop-symmetric?:")
    (bench (def _ (mx/loop-symmetric? mp)))

    ;(println "\nclatrix comp-sym?:")
    ;(bench (def _ (comp-sym? mc)))
    ;(println "\nclatrix rec-sym?:")
    ;(bench (def _ (rec-sym? mc)))
    ;(println "\nclatrix loop-sym?:")
    ;(bench (def _ (loop-sym? mc)))
    (println "\nclatrix symmetric?:")
    (bench (def _ (mx/symmetric? mc)))
    (println "\nclatrix loop-symmetric?:")
    (bench (def _ (mx/loop-symmetric? mc)))
  ))

(defn bench-zero
  [dim]
  (println "\n*** zero matrices ***")

;  (let [m (mx/new-matrix :vectorz dim dim)]
;    ;(println "\nvectorz comp-sym?:")
;    ;(bench (def _ (comp-sym? m)))
;    (println "\nvectorz rec-sym?:")
;    (bench (def _ (rec-sym? m)))
;    (println "\nvectorz loop-sym?:")
;    (bench (def _ (loop-sym? m)))
;    (println "\nvectorz symmetric?:")
;    (bench (def _ (mx/symmetric? m)))
;    )

  (let [m (mx/new-matrix :ndarray dim dim)]
    ;(println "\nndarray comp-sym?:")
    ;(bench (def _ (comp-sym? m)))
    ;(println "\nndarray rec-sym?:")
    ;(bench (def _ (rec-sym? m)))
    ;(println "\nndarray loop-sym?:")
    ;(bench (def _ (loop-sym? m)))
    (println "\nndarray symmetric?:")
    (bench (def _ (mx/symmetric? m)))
    (println "\nndarray loop-symmetric?:")
    (bench (def _ (mx/loop-symmetric? m)))
    )

  (let [m (mx/new-matrix :persistent-vector dim dim)]
    ;(println "\npersistent-vector comp-sym?:")
    ;(bench (def _ (comp-sym? m)))
    ;(println "\npersistent-vector rec-sym?:")
    ;(bench (def _ (rec-sym? m)))
    ;(println "\npersistent-vector loop-sym?:")
    ;(bench (def _ (loop-sym? m)))
    (println "\npersistent-vector symmetric?:")
    (bench (def _ (mx/symmetric? m)))
    (println "\npersistent-vector loop-symmetric?:")
    (bench (def _ (mx/loop-symmetric? m)))
    )

  (let [m (mx/new-matrix :clatrix dim dim)]
    ;(println "\nclatrix comp-sym?:")
    ;(bench (def _ (comp-sym? m)))
    ;(println "\nclatrix rec-sym?:")
    ;(bench (def _ (rec-sym? m)))
    ;(println "\nclatrix loop-sym?:")
    ;(bench (def _ (loop-sym? m)))
    (println "\nclatrix symmetric?:")
    (bench (def _ (mx/symmetric? m)))
    (println "\nclatrix loop-symmetric?:")
    (bench (def _ (mx/loop-symmetric? m)))
  )
)
