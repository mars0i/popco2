(ns something
  [:use criterium.core
        clojure.core.matrix
        popco.core.neuralnets
        popco.core.neuralnets-testtools])

;(use 'criterium.core)
;(use 'clojure.core.matrix)

; do this:
;(load-file "../popco2/neural-nets.clj")
; then do this:
;(set-current-implementation ...)
; :persistent-vector
; :ndarray
; :vectorz
;
;

(defn bench-settle [num-nodes prob-of-link]
  (let [a (make-random-activn-vec num-nodes)
        M (make-sparse-random-net-mat num-nodes prob-of-link)
        Mp (emap posify M)
        Mn (emap negify M)]
    ;; jit/etc burn-in
    (dotimes [i 5] (def _ (next-activns M a)))
    ;; tests
    (print (format "%nWith single weight-matrix: num-nodes: %s, prob-of-link: %s:%n%n" num-nodes prob-of-link))
    (bench (def _ (next-activns M a)))
    (print (format "%nWith split weight-matrices: num-nodes: %s, prob-of-link: %s:%n%n" num-nodes prob-of-link))
    (bench (def _ (next-activns Mp Mn a))) ))



(print (format "%n==============================%nImplementation: %s:%n" (current-implementation)))
(bench-settle 250  0.035)
(bench-settle 500  0.035)
(bench-settle 1000 0.035)
