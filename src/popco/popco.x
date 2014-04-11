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
