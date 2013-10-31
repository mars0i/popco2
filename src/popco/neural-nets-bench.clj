;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tools for testing

(ns popco
  [:use criterium.core])

(defn make-random-activn-vec
  "Generate a vector of random activations of num-nodes nodes."
  [num-nodes]
  (matrix (repeatedly num-nodes rand-1+1)))

(defn make-random-net-mat
  "Generate a matrix of random links for num-nodes nodes."
  [num-nodes]
  (matrix (repeatedly num-nodes #(repeatedly num-nodes rand-1+1))))

(defn make-sparse-random-net-mat
  "Generate a matrix of random links for num-nodes nodes, with probability
  prob-of-link of creating a link (i.e. of choosing a random weight rather 
  than simply zero).  Note that the matrix is never sparse; the network 
  represented by may be."
  [num-nodes prob-of-link]
  (matrix 
    (repeatedly num-nodes 
                #(repeatedly num-nodes (fn [] (rand-rand-1+1 prob-of-link))))))


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

;;;;;;;;;;;;;;;;;;;;

;; Example:

;; In POPCO's crime3.lisp, e.g. as called in yotest.lisp, a person with all propositions
;; has the following numbers of nodes and links:
;;
;; nodes in analogy net:     links in analogy net:
;; 262                       2499
;; (i.e. the number of links is about .035 of those possible: 2499/(262*262) = .0364)
;;
;; nodes in proposition net: links in proposition net:
;;  50                        183
;;
;; total nodes:              total links:
;; 312                       2682
