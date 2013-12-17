(ns tst
  (:use criterium.core))

(defn looptest [s n]
    (loop [f (first s) r (rest s)] 
      (if (< f n) 
        (recur (first r) (rest r)) 
        f)))

(defn letfntest [s n]
    (letfn [(g [f r] 
              (if (< f n) 
                (recur (first r) (rest r)) 
                f))] 
      (g (first s) (rest s))))

(defn lettest [s n]
    (let [g (fn [f r] (if (< f n) 
                        (recur (first r) (rest r)) 
                        f))] 
      (g (first s) (rest s))))

(defn bench-test []
  (let [s (doall (range 1000001))] 
    (println "\nlooptest:")
    (bench (def _ (looptest s 1000000)))
    (println "\nletfntest:")
    (bench (def _ (letfntest s 1000000)))
    (println "\nlettest:")
    (bench (def _ (lettest s 1000000)))))
