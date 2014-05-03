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



(declare once 
         pl-once many-times 
         ;once! many-times! 
         pl-many-times popco report-popn report-to-console inc-tick report)

(def folks (atom (->Population 0 [])))

;(defn init
;  ([] (init @folks))
;  ([popn]
;   (reset! folks 
;           (assoc popn :members
;                  (map up/settle-analogy-net (:members popn))))))

;; DOESN'T WORK?
(defn popco
  "Returns a lazy sequence of population states, one for each tick, with 
  between-tick reporting on each realized population state, starting from
  initial population state popn, or folks by default."
  ([] (popco folks))
  ([popn] (map report-popn (pl-many-times popn))))

(defn many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate once popn))

;(defn many-times!
;  "Returns a lazy sequence of population states, one for each tick.
;  No between-tick reporting is done when the sequence is realized."
;  [popn]
;  (iterate once! popn))

(defn pl-many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate pl-once popn))

;; This should be obvious:
;; Any side-effects caused by code in 'once' will occur if it's
;; called on its own, but not on unrealized calls to it under 'iterate'.

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  [popn]
  (->Population
    (inc (:tick popn))
    (doall    ; one or both of these steps might not be purely functional:
      (cm/communicate 
        (up/update-nets (:members popn))))))

;(defn once!
;  "Implements a single timestep's (tick's) worth of evolution of the population.
;  Returns the population in its new state.  May mutate persons' internal data
;  structures."
;  [popn]
;  (->Population
;    (inc (:tick popn))
;    (doall    ; one or both of these steps might not be purely functional:
;      (cm/communicate 
;        (up/update-nets! (:members popn))))))

(defn pl-once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  [popn]
  (->Population
    (inc (:tick popn))
    (doall    ; one or both of these steps might not be purely functional:
      (cm/communicate 
        (up/pl-update-nets (:members popn))))))



(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  ([popn] (once popn {:mapfn pmap :tick-repts [] :comm-repts []}))
  ([popn & [{mapfn :mapfn, tick-repts :tick-repts, comm-repts :comm-repts}]]
   (->Population
     (inc (:tick popn))
     (->> (:members popn)
       (mapfn per-person-fns)
       ((ug/comp* comm-repts))
       (cm/transmit-utterances)
       (map (ug/comp* tick-repts))))))

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  ([popn] (once {:mapfn pmap :tick-repts [] :transm-repts []} popn))
  ;; BUG NEED TO ASSOC IN MISSING ARGS
  ([{mapfn :mapfn, tick-repts :tick-repts, trans-repts :transm-repts} 
    popn]
   (->Population
     (inc (:tick popn))
     (map (ug/comp* tick-repts)         ; pmap might reorder output
          (cm/transmit-utterances 
            (map (ug/comp* trans-repts) ; pmap might reorder output
                 (mapfn per-person-fns (:members popn))))))))

(defn many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  ([popn] (iterate once popn))
  ([optmap popn] (iterate (partial once optmap) popn)))

(def per-person-fns (comp cm/choose-conversations up/update-person-nets))

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  ([popn] (once {} popn))
  ([optmap popn]
   (let [{:keys [mapfn tick-repts trans-repts]} 
         (merge {:mapfn pmap :tick-repts [] :trans-repts []} optmap)]
     (->Population
       (inc (:tick popn))
       (map (ug/comp* tick-repts)         ; pmap might reorder output
            (cm/transmit-utterances 
              (map (ug/comp* trans-repts) ; pmap might reorder output
                   (mapfn per-person-fns (:members popn)))))))))

(defn many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  ([popn] (iterate once popn))
  ([optmap popn] (iterate (partial once optmap) popn)))

(def per-person-fns (comp cm/choose-conversations up/update-person-nets))

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  ([popn] (once {} popn))
  ([optmap popn]
   (let [{:keys [mapfn tick-repts trans-repts] 
          :or {mapfn pmap tick-repts [identity] trans-repts [identity]}}
         optmap]
     (->Population
       (inc (:tick popn))
       ((apply comp tick-repts)         ; pmap might reorder output
          (cm/transmit-utterances 
            ((apply comp trans-repts) ; pmap might reorder output
               (mapfn per-person-fns (:members popn)))))))))



;; An earlier version included a report-fn argument.  I now think it's
;; better to just map or doseq such functions, externally, over the
;; lazy sequence returned by this function.
(defn many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate once popn))

;(defn many-times
;  "Returns a lazy sequence of population states, one for each tick.
;  No between-tick reporting is done when the sequence is realized."
;  ([popn] (iterate once popn))
;  ([report-fn popn] (iterate (comp report-fn once) popn)))

;; It's not clear that it will ever be necessary to use 'map' rather than
;; 'pmap' for mapfn, except for testing (which should be done, e.g. on Cheaha).
;; See comment on many-times about earlier, more complicated versions.
(defn unparalleled-many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate (partial once map) popn))

;(defn unparalleled-many-times
;  "Returns a lazy sequence of population states, one for each tick.
;  No between-tick reporting is done when the sequence is realized."
;  ([popn] (iterate (partial once map) popn))
;  ([report-fn popn] (iterate (comp report-fn (partial once map)) popn)))


(defn person-propn-names
  "Given a sequence of persons, return a sequence of strings containing
  \"personalized\" proposition names, i.e. with the person's name appended
  to the front of the proposition id string, with the form \"person_propn\".
  These are suitable for use as column names in a csv file containing data
  on proposition activations for all of the persons.  Note that the number
  of strings returned will be (number of persons X number of propositions)."
  [persons]
  (let [name-strs (map (comp name :nm) persons)
        id-strs (map name (rest (:id-vec (:propn-net (first persons)))))]
    (vector    ; write-csv wants a vector of vectors
      (concat 
        (for [name-str name-strs
              id-str id-strs]
          (str name-str "_" id-str))))))

;; TODO ?  NOTE THIS ASSUMES THAT SALIENT IS FIRST.  SHOULD I INSTEAD LOOK UP
;; SALIENT'S LOCATION??
(defn person-data
  "Given a person, returns the activation values of its propositions other
  than SALIENT in the form of a Clojure vector.  Assumes that SALIENT is
  the first node."
  [pers]
  (rest 
    (mx/matrix :persistent-vector 
               (:propn-activns pers))))

(defn data-row
  [persons]
  (vector  ; write-csv wants a vector of vectors each time it's called
    (mapcat person-data persons)))

;; ATTEMPTS AT FULLY-FUNCTIONAL EXPERIMENTS:

;; DOESN'T WORK Throws closed stream exception
;; i.e. you can't embed a stream in a closure??
(defn many-times-with-csv1
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (person-propn-names persons))
      (mn/many-times (comp write-data-row mn/ticker) popn))))

;; This one throws the closed stream exception, too.
(defn many-times-with-csv2
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (person-propn-names persons))
      (map write-data-row
           (mn/many-times popn)))))

;; this one has same problem
(defn many-times-with-csv3
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (person-propn-names persons))
      (mn/many-times write-data-row popn))))

;; IN THIS ONE THE CLOSURE SEEMS TO WORK.  What??
(defn mwe []
  (with-open [w (io/writer "foo.csv")] 
    (let [rows (repeatedly 3 #(vector (range 4)))
          write-row (fn [row] (csv/write-csv w row))]
      (doall
        (map write-row rows)))))

;; THIS ONE WORKS.  It adds doall, just like the mwe.
(defn many-times-with-csv4
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          write-data-row (fn [popn]
                           (csv/write-csv w (data-row (:members popn)))
                           popn)]
      (csv/write-csv w (person-propn-names persons))
      (doall
        (take 10
              (map (comp write-data-row mn/ticker)
                   (mn/many-times popn)))))))

;; THIS ONE FAILS.  It has no doall.
(defn mwe2 []
  (with-open [w (io/writer "foo.csv")] 
    (let [rows (repeatedly 3 #(vector (range 4)))
          write-row (fn [row] (csv/write-csv w row))]
      (map write-row rows))))

;; LESSON:
;; Embedding a stream in a closure that's embedded in a lazy sequence
;; will fail if the lazy-sequence escapes out of the with-open block
;; without being fully realized, and you then try to realize part
;; of the sequence outside of the with-open block.

;; SUGGESTION:
;; Create the lazy popn sequence.  Then pass it to a function
;; that will realize and write, maybe mapping with doall up to
;; tick n, or or doseq-ing or dotimes-ing until tick n.
;; (Try to save the head if you want to do other stuff with it.)


;; THIS WORKS--i.e. as far as the printing part goes.
;; Because it doesn't use a closure, but rather visible scope??
;; Also I'm not making it return the next popn--it just runs
;; through them.
(defn many-times-with-csv
  [popn]
  (with-open [w (io/writer "popco.csv")] 
    (let [persons (:members popn)
          popns (mn/many-times popn)]
      (csv/write-csv w (person-propn-names persons))
      (doseq [popn popns]
        (csv/write-csv w (data-row (:members popn)))
        (mn/ticker popn)))))
;; note nothing useful returned here




;; Examles based on https://github.com/clojure/data.csv.

;(defn example1
;  [pers]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w 
;                   (vector 
;                     (map name (:id-vec (:propn-net pers)))
;                     (mx/matrix :persistent-vector (:propn-activns pers))))))
;
;
;(defn example2
;  [pers]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w 
;                   [(map name (:id-vec (:propn-net pers)))
;                     (mx/matrix :persistent-vector (:propn-activns pers))])))
;
;(defn example3
;  [pers]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w [(map name (:id-vec (:propn-net pers)))])
;    (csv/write-csv w [(mx/matrix :persistent-vector (:propn-activns pers))])))
;
;(defn example4
;  [persons]
;  (with-open [w (io/writer "out-file.csv")] 
;    (csv/write-csv w [(map name (:id-vec (:propn-net (first persons))))])
;    (doseq [pers persons]
;      (csv/write-csv w [(mx/matrix :persistent-vector (:propn-activns pers))]))))




(defn old-spit-propn-activns-csv
  "Collects reads activns from a sequence of popns into a large seq of seqs, and
  then writes them all at once into a csv file.  Writes a header row first."
  [popns append? & other-options]
  (let [data (data-vec-of-rows popns) 
        rows (if append?
               data 
               (cons (column-names (first popns)) ; if not appending, add header row
                     data))]
    (apply spit-csv "activns.csv" rows :append append? other-options)))

(defn old-write-propn-activns-csv-by-line
  "Reads activns tick by tick from popns in a sequence, writing a row for each
  popn.  Writes a header row first unless append? is true."
  ([popns]
   (write-propn-activns-csv-by-line popns false))
  ([popns append?]
   (with-open [w (io/writer "activns.csv" :append append?)] 
     (when-not append?
       (csv/write-csv w (vector (column-names (first popns)))))
     (doseq [popn popns]
       (csv/write-csv w (vector (data-row popn)))))))

;; WHAT ABOUT APPENDING ROWS? DON'T WANT HEADERS AGAIN.
;(defn vec-of-rows
;  "Creates a sequence of sequences of activns, one inner sequence for each tick,
;  from popns in an input sequence.  Writes a header row first."
;  [popns]
;  (cons 
;    (column-names (first popns))
;    (data-vec-of-rows popns)))
