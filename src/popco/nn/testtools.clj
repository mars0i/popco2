(ns popco.nn.testtools
  [:use clojure.core.matrix
        popco.nn.settle])

(defn rand-1+1 
  "Returns a random number in [-1, 1)."
  []
  (dec (rand 2)))

(defn rand-rand-1+1
  "Returns a random number in [-1, 1) if a prior binary random variable
  with probability prob of success succeeds, else 0.  prob should be
  a number in [0,1].  0 is equivalent to no possibility, not zero prob."
  [prob]
  (if (< (rand) prob)
    (rand-1+1)
    0))

(defn rand-0-or-1
  "Returns either 0 or 1, with probability prob."
  [prob]
  (if (< (rand) prob) 1 0))


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
