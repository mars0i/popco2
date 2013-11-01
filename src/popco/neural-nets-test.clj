(ns popco)

;(use 'criterium.core)
;(use 'clojure.core.matrix)

; do this:
;(load-file "../popco2/neural-nets.clj")
; then do this:
;(set-current-implementation ...)
; :persistent-vector
; :ndarray
; :vectorz

(print (format "%n==============================%nImplementation: %s:%n" (current-implementation)))
(bench-settle 250  0.035)
(bench-settle 500  0.035)
(bench-settle 1000 0.035)
