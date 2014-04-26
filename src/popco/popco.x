;;; This file contains deleted code that I might possibly want later

(defn format-top-labels-for-csv
  "ADD DOCSTRING"
  [labels intercolumn-width left-pad-width]
  (let [label-height (max-strlen labels)
        initial-pad (cl-format nil "\"~V\"," left-pad-width "")
        interline-pad (cl-format nil "\",~%")
        intercolumn-pad (cl-format nil ",")]
    (apply str                                           ; make the whole thing into a big string
           (conj (vec                                    ; add final newline
                   (cons initial-pad                     ; add initial spaces to first line
                         (interpose interline-pad        ; add newlines and initial spaces to each line except the first
                                    (map #(apply str %)  ; concatenate each inner vector to a string
                                         (map #(interpose intercolumn-pad %)  ; add spaces between chars from each column
                                              (mx/transpose                   ; (cheating by using a numeric matrix op, but convenient)
                                                            (collcolls-to-vecvecs    ; convert to Clojure vector of vector, which will be understood by core.matrix
                                                                                  (map #(cl-format nil "~v@a" label-height %) ; make labels same width (transposed: height)
                                                                                       labels)))))))) 
                 "\n"))))

(defn format-mat-with-row-labels-for-csv
  "ADD DOCSTRING"
  [pv-mat labels nums-width]
  (let [labels-width (max-strlen labels)
        nums-widths (repeat (count labels) (+ 1 nums-width))] ; we'll need a list of repeated instances of nums-width
    (map (fn [row label]
           (cl-format nil "\"~v\",~{~vf,~}~%" 
                      label 
                      (interleave nums-widths row))) ; Using v to set width inside iteration directive ~{~} requires repeating the v arg
         pv-mat labels)))

; This is rather slow, but fine if you don't need to run it very often.
(defn format-matrix-with-labels-for-csv
  "Format a matrix mat with associated row and column labels into a string
  that could be printed prettily.  row-labels and col-labels must be sequences
  of strings in index order, corresponding to indexes from 0 to n."
  [mat row-labels col-labels]
  (let [pv-mat (mx/matrix :persistent-vector mat) ; "coerce" to Clojure vector of Clojure (row) vectors
        nums-width (+ 0 (max-strlen 
                          (map #(cl-format nil "~f" %) 
                               (apply concat pv-mat))))
        left-pad-width (+ nums-width (max-strlen row-labels))]
      (apply str
             (concat
               (format-top-labels-for-csv col-labels nums-width left-pad-width)
               (format-mat-with-row-labels-for-csv pv-mat row-labels nums-width)))))

;; **********
;; **********
;; CONSIDER USING clojure.data/diff for the following
;; **********
;; **********
;; FUNCTION TO RETURN BOTH MATCHED PAIRS AND UNMATCHED INDIVIDUAL LINKS, DIVIDED BY
;; SOURCE.  FIRST ATTEMPT INCOMPLETE AND NOT RIGHT ANYWAY:
;(defn matched-and-unmatched-links
;  [links1 links2]
;  (cond 
;    (or (empty? links1) (empty? links2))
;    [nil (concat links1 links2)]  ; if either is non-empty, we want it as second value
;
;    (same-nodes? (first links1) (first links2))
;    (let [[matched unmatched] (recur (rest links1) (rest links2))] ; won't work--not tail call
;        [(cons [(first links1) (first links2)] matched) unmatched]))
;  ;; unfinished
;  )


;; inefficient but fast enough for its use case
(defn matched-links
  "Given two seqs of links in list-links format, generate a sequence of pairs
  of links that have the same node ids.  Weights need not be the same.  Assumes
  that links have been normalized as by popco.nn.pprint/normalize-link, i.e. 
  within each link representation, the two ids are in alpha order, but links do
  not have to be sorted within each of the two toplevel seqs.  Unmatched links
  are simply ignored."
  [links1 links2]
  (for [[id1a id1b & _ :as lk1] links1
        [id2a id2b & _ :as lk2] links2
        :when (and (identical? id1a id2a)
                  (identical? id1b id2b))]
    (list lk1 lk2)))

;; Maybe not useful since throws away non-matches
(defn matched-links
  "Given two seqs of links in list-links format, generate a sequence of pairs
  of links that have the same node ids.  Weights need not be the same.  Assumes
  that links have been normalized as by popco.nn.pprint/normalize-link, i.e. 
  within each link representation, the two ids are in alpha order, but links do
  not have to be sorted within each of the two toplevel seqs.  Unmatched links
  are simply ignored."
  [links1 links2]
  (for [lk1 links1
        lk2 links2
        :when (same-nodes? lk1 lk2)]
    (list lk1 lk2)))

(defn popco
  [iters]
  (loop [i 0
         population folks]
    (when (< i iters)
      (report-progress-to-console)
      (swap! tick inc)
      (recur (inc i) (once population)))))

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state."
  [popn]
  (->> popn
    (update-nets)
    (doall)       ; make sure that changes to vectors, matrices made before communication [NEEDED?]
    (cm/communicate)
    (doall)       ; make sure that changes to vectors, matrices made before settling [NEEDED?]
    (inc-tick)))

(defn update-nets
  "Implements a single timestep's (tick's) worth of network settling and updating of the
  proposition network from the analogy network.  Returns the population in its new state
  after these processes have been performed."
  [persons]
  (map update-person-net persons))
  (->> persons
    (st/settle-analogy-net!)
    (doall)  ; make sure changes to analogy activns made before updating propn net weights [NEEDED?]
    (nn/update-propn-wts-from-analogy-activns!)
    (doall)  ; make sure changes to propn matrix made before settling it [NEEDED?]
    (st/settle-propn-nets!)))
;;; TODO SHOULD THESE REALLY BE SIDE-EFFECTING? CAN'T I DO IT FUNCTIONALLY?
;;; I'M ONLY CHANGING THE ACTIVN VECTORS IN THE SETTLE FUNCTIONS.
;;; MAYBE THE MIDDLE ONE SHOULD BE IMPERATIVE, THOUGH.
;;; AND WRAP DOALLs or use DOSEQ/DOMAP OTHERWISE.
;;; 
;;; AND SEE cloj/doc/unrealizedrealization.clj.  Oy.


;; TODO: Deal with semantic-iffs and influence from communication.
(defn update-propn-wts-from-analogy-activns
  "Performs a functional update of person pers's propn link weight matrix 
  from activations of proposition map nodes in the analogy network.  
  i.e. this updates the weight of a propn-to-propn link as a function of 
  the activation of the map node that maps those two propositions, in the 
  analogy network.  Returns the fresh, updated person."
  [pers]
  (update-propn-wts-from-analogy-activns! (pers/propn-net-clone pers)))

(defn update-propn-wts-from-analogy-activns!
  "Mutates person pers's propn link weight matrix from activations of
  proposition map nodes in the analogy network.  i.e. this sets the
  weight of a propn-to-propn link as a function of the activation of
  the map node that maps those two propositions, in the analogy network.
  Returns the original person with its newly-modified propn net"
  [pers]
  (let [a-activns (:analogy-activns pers)
        p-mat (:wt-mat (:propn-net pers))         ; We want the actual matrix. Safer with the keyword.
        aidx-to-pidxs (:analogy-idx-to-propn-idxs pers)
        aidxs (keys aidx-to-pidxs)]
    (doseq [a-idx aidxs
            :let [a-val (mget a-activns a-idx)
                  [p-idx1 p-idx2] (aidx-to-pidxs a-idx)]]  ; CAN I DO THIS AT THE TOP OF THE doseq BY DESTRUCTURING MAP ELEMENTS?
      (mset! p-mat p-idx1 p-idx2 (calc-propn-link-wt a-val)))
    pers)) ; this version mutates the matrix inside pers, so no need to assoc it into the result
