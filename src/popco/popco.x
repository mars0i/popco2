
;; TODO THIS IS NOT SUCCESS BIAS PER SE.  See doc/general/SuccessBias.md.
(defn worth-saying
  [pers abs-activn]
  (let [prob (+ abs-activn (success pers))]
    (< (ran/next-double (:rng pers)) 
       prob)))

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

(defn alt-propn-activns-row
  "Construct a sequence of activations representing all propn activns of all 
  persons at one tick."
  [popn]
  (let [activn-vecs (map :propn-activns (:members popn))
        num-vecs (count activn-vecs)
        vec-len (first (shape (first activn-vecs)))]  ; .length works with impls as of 5/2014, but not part of the core.matrix interface

;(defn invert-coll-map2
;  [m]
;  (let [v-elts (set (apply concat (vals m)))
;        init-maps (map #(hash-map % []) v-elts) ; we need maps with empty colls as keys, so conj will work
;        data-maps (for [[k v] m              ; a coll of maps from elts in val colls, to the keys of those colls
;                        elt v-elts           ; actually this does the same thing as join-pair-seq
;                        :when (contains? (set v) elt)] {elt k})] ; (set v) to make contains? work
;    (apply merge-with conj 
;           (concat init-maps
;                   data-maps))))


;(defn fn-pow2
;  "Apply function f to x, then to the result of that, etc., n times.
;  If n <= 0, just returns x."
;  [f x n]
;  (if (> n 0) 
;    (recur f (f x) (dec n))
;    x))

;(defn fn-pow1
;  "Apply function f to x, then to the result of that, etc., n times.
;  If n <= 0, just returns x."
;  [f x n]
;  (loop [x x 
;         n n]
;    (if (<= n 0) 
;      x
;      (recur (f x) (dec n)))))

;(defn fn-pow2
;  "Apply function f to x, then to the result of that, etc., n times.
;  If n <= 0, just returns x."
;  [f x n]
;  (if (<= n 0) 
;    x
;    (recur f (f x) (dec n))))

;(defn fn-pow0
;  [f x n]
;  (take
;    (iterate f x)
;    n))

;(defn skip-realized?
;  "Tests whether the first LazySeq instance in the sequence xs has been 
;  realized, skipping over e.g. Cons's before that point.  (e.g. 'iterate'
;  returns a Cons followed by a LazySeq.  If you want to know whether it's 
;  realized beyond the Cons, you have to check its rest.)"
;  [xs]
;  (if (instance? clojure.lang.IPending xs)
;    (realized? xs)
;    (if (empty? xs)
;      true
;      (recur (rest xs)))))

;; Compare skip-realized? with the follwoing, which tests whether there's any unrealized part anywhere down the line.
;; By A. Webb in response to a question of mine, at https://groups.google.com/d/msg/clojure/5rwZA-Bzp9A/dgChQhbeF_AJ
;(defn seq-realized?
;  "Returns false if there is an unrealized tail in the sequence,
;  otherwise true."
;  [s]
;  (if (instance? clojure.lang.IPending s)
;    (if (realized? s)
;      (if (seq s)
;        (recur (rest s))
;        true)
;      false)
;    (if (seq s)
;      (recur (rest s))
;      true)))

(defn old-invert-vec-map
  "Given a map whose values are collections, return a map of the same sort,
  but in which each val member is now a key, and the members of their val
  collections are the keys for the current vals' former collections."
  [m]
  (apply merge-with 
         (comp flatten vector)
         (map st/map-invert 
              (vec-map-to-join-pairs m))))




;(defn communicate
;  "Implements a single timestep's (tick's) worth of communication.  Given a
;  sequence of persons, constructs conversations and returns the persons, updated
;  to reflect the conversations."
;  [persons & trans-repts]
;  (transmit-utterances persons 
;                       ((ug/comp* trans-repts) (choose-conversations persons))))



; obsolete
;(defn choose-propn-to-utter
;  [{:keys [propn-net propn-mask propn-activns]}]
;  :NO-OP) ; TODO



;(defn choose-thought
;  "Currently a noop: Returns a dummy proposition."
;  [pers]
;  (lot/->Propn (lot/->Pred :TODO) [] :TODO))
  
;(defn choose-conversations
;  "Given a sequence of persons, returns a sequence of conversations, i.e.
;  maps with keys :speaker, :listener, and :propn, indicating that speaker
;  will communicate propn to listener."
;  [persons]
;  (map choose-utterance 
;       (mapcat choose-person-conversers persons)))

(defn choose-conversations (comp choose-utterance choose-person-conversers))
(defn choose-person-conversers
  "Currently a noop. Given a person pers, returns a converser-pair assoc'ed
  into a person with :convs.  A converse-pair is a sequence 
  of 2-element maps with keys :speaker and :listener, where :speaker is pers, 
  and :listener is another person."
  [pers]
  pers)

(defn lazy-split-elements2
  "Given a collection of pairs, returns a pair of two sequences, one containing
  the first elements of the pairs, in order, and the other containing the
  second elements of the pairs, in order.  Note that if the input collection
  is empty, split-elements returns a pair containing two empty sequences."
  [pairs]
  (loop [prs pairs
         firsts []
         seconds []]
    (if (empty? prs)
      (list firsts seconds)
      (let [[fst snd] (first prs)]
        (recur (rest prs)
               (cons fst (lazy-seq firsts))
               (cons snd (lazy-seq seconds)) )))))


(defn receive-transmissions
  [transmission-map pers]
  (let [pers-id (:id pers)
        tranmissions (transmission-map pers-id)]
    (receive-propn jpers
  pers)

;; NOTE transmit-utterances might not be purely functional.
;(defn receive-transmissions
;  "Currently a noop: Takes persons with specifications of conversations assoc'ed
;  in with :convs, and returns the persons with the conversations stripped out, 
;  but with the persons updated to reflect the conversations.  (See 
;  choose-conversations for the structure of conversations.)  (Note we need the 
;  persons as well as conversations, so that we don't lose persons that no one 
;  speaks to.)"
;  [persons-and-transmissions]
;  (let [[persons transmissions] (ug/split-elements persons-and-transmissions)
;        transmission-map (ug/join-pairs-to-coll-map (apply concat transmissions))] ; TODO: are there faster methods at http://stackoverflow.com/questions/23745440/map-of-vectors-to-vector-of-maps
;    ;; ADD TRANSMISSION STEP
;    ;; Maybe something like:
;    ;; (pmap persons #(domap (comp apply receive-propn!) (transmission-map person)))
;    persons)) ; TODO TEMPORARY

;; obsolete: just core.matrix/transpose
(defn split-elements
  "Given a collection of pairs, returns a pair of two sequences, one containing
  the first elements of the pairs, in order, and the other containing the
  second elements of the pairs, in order.  Note that if the input collection
  is empty, split-elements returns a pair containing two empty sequences."
  [pairs]
  (loop [prs pairs
         firsts []
         seconds []]
    (if (empty? prs)
      (list firsts seconds)
      (let [[fst snd] (first prs)]
        (recur (rest prs) (conj firsts fst) (conj seconds snd))))))

(defn transmit-utterances
  "Given a person, returns a pair containing the person, unchanged, and a
  Clojure map from the person's id to a pair containing the index of a 
  proposition, and its current activation in the person."
  [pers]
  (let [id-to-idx (:id-to-idx (:propn-net pers))
        propn-activns (:propn-activns pers)
        listeners (choose-listeners pers)
        to-say-idxs (choose-what-to-say-idxs pers (count listeners))
        to-say-idx-activn-pairs (map #(vector % (mx/mget propn-activns %)) 
                                     to-say-idxs)]
    [pers (map hash-map listeners to-say-idx-activn-pairs)]))

(defn make-utterances
  "Given a person, returns a Clojure map representing utterances to
  persons, i.e. a map from persons who are listeners--i.e. persons
  who the current person is trying to speak to--to utterances to be
  conveyed from the current person to each of those listener.
  Utterances are sequences in which the first element represents
  a proposition [TODO: as id, or index?], and the second element
  captures the way in which the proposition should influence the
  listener [TODO: raw or cooked activation?]." ; FIXME
  [speaker]
  (let [id-to-idx (:id-to-idx (:propn-net speaker))
        propn-activns (:propn-activns speaker)
        listeners (choose-listeners speaker)
        to-say-ids (choose-propn-ids-to-say speaker (count listeners))
        utterances (map #(->Utterance % (utterance-valence speaker %) (:id speaker))
                        to-say-ids)]

        to-say-id-activn-pairs (map #(vector % 
                                             (mx/mget propn-activns (id-to-idx %))
                                             (:id speaker))
                                    to-say-ids)]
    (map hash-map listeners to-say-id-activn-pairs)))

(defn display-propn-salient-wts
  "Display the utterance-map from the last tick that was stored 
  in the population, and return the population unchanged."
  [popn]
  ;(map (comp println mx/pm pmx/col1 nn/wt-mat :propn-net) (:persons popn))
  ;(map println 
       (map mx/pm (map pmx/col1 (map nn/wt-mat (map :propn-net (:persons popn)))));)
  ;(map (fn [pers] (->> pers :propn-net nn/wt-mat pmx/col1 mx/pm #(print "\n"))) (:persons popn))
  popn)

;; vectorz only supports Doubles, and ndarray defaults to Doubles.
;; If I want something else with ndarray, I need to replace some of
;; its functions with other ones.  But for use with vectorz, the
;; built-in core.matrix functions are best.  So there will be two
;; different versions of popco.nn.matrix--the usual one, which just
;; defines my functions as the original core.matrix functions, and
;; the special version designed for using ndarray with other sorts
;; of numbers.
(def zero-vector mx/zero-vector)
(def zero-matrix mx/zero-matrix)



(ns utils.random
  (:require [clojure.data.generators :as gen]
            [incanter.stats :as incant]
            [bigml.sampling [simple :as simple]])
  (:import [ec.util MersenneTwister MersenneTwisterFast] ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE
           [SFMT19937])) ; EXPERIMENTING--NEED TO DEAL WITH LICENSE NOTICES BEFORE RELEASE

;; Uses clojure.core's `rand-int` method of truncation to an int with `int`
;; rather than data.generator's `uniform` method of truncation using `Math/floor`
;; followed by clojure.core's `long`.  (Why call `Math/floor` before `long`?
;; Maybe experiment with adding this before the call to `int`.)
(defn make-rand-idx-from-next-double
  "This is essentially the same as Clojure's `rand-int` with an RNG argument.
  Works with any RNG that supports `.nextDouble`, including java.util.Random,
  and Sean Luke's MersenneTwister and MersenneTwisterFast."
  [rng n]
  (int (* n (.nextDouble rng))))

(def int-range (double (- Integer/MAX_VALUE Integer/MIN_VALUE)))

;; ????
(defn make-rand-idx-from-sfmt19937
  "Supposed to work like Clojure's `rand-int`, but generated by an RNG that's
  an SFMT19937."
  [rng n]
  (int (* n                            ; When we're all done, go back to an int.
          (- (/ (.next rng) int-range) ; SFMT19937 *only* produces ints; make it into a double.
             Integer/MIN_VALUE))))     ; then make it non-negative
                                       ; Is this a good strategy??


;; Note these are lazy:

;; is "make-" correct?
;; This version repeatedly calls nth coll with a new random index each time.
(defn make-sample-with-replacement-1
  [rand-idx num-samples coll]
  (repeatedly num-samples 
              #(nth coll (rand-idx (count coll))))) ; does repeatedly make this get reevaluated every time?

;; is "make-" correct?
;; This version is inspired by Incanter, which does it like this:
;;        (map #(nth x %) (sample-uniform size :min 0 :max max-idx :integers true))
;; You get a series of random ints between 0 and the coll size,
;; and then map nth coll through them.
(defn make-sample-with-replacement-2
  [rand-idx num-samples coll]
  (map #(nth coll %) 
       (repeatedly num-samples 
                   #(rand-idx (count coll)))))


;; example:
(def sample-with-replacement
  (partial make-sample-with-replacement-1
           (partial make-rand-idx-from-next-double
                    (MersenneTwisterFast. 325117))))

(defmulti  next-double class)
(defmethod next-double ec.util.MersenneTwister     [rng] (.nextDouble rng))
(defmethod next-double ec.util.MersenneTwisterFast [rng] (.nextDouble rng))
(defmethod next-double java.util.Random            [rng] (.nextDouble rng))
(defmethod next-double SFMT19937


;; Uses clojure.core's `rand-int` method of truncation to an int with `int`
;; rather than data.generator's `uniform` method of truncation using `Math/floor`
;; followed by clojure.core's `long`.  (Why call `Math/floor` before `long`?
;; Maybe experiment with adding this before the call to `int`.)
(defn make-rand-idx-from-next-double
  "This is essentially the same as Clojure's `rand-int` with an RNG argument.
  Works with any RNG that supports `.nextDouble`, including java.util.Random,
  and Sean Luke's MersenneTwister and MersenneTwisterFast.  Returns an int in
  [0,n).  Use `partial` to create a workalike for rand-int, maybe named 'rand-idx'."
  [rng n]
  (int (* n (.nextDouble rng))))

;; ????
;; question: Am I producing ints from 0 to count-1 with this method?  Or is there
;; some kind of off by 1 type of issue?
;(defn make-rand-idx-from-sfmt19937-next
;  "This is essentially the same as Clojure's `rand-int` with an RNG argument.
;  Works with any RNG that supports a method `.next` that returns an int, in
;  particular SFMT19937.  Use `partial` to create a workalike for rand-int, which
;  could be named 'rand-idx'."
;  [rng n]
;  (int (* n                            ; When we're all done, go back to an int.
;          (- (/ (.next rng) +int-range-double+) ; SFMT19937 *only* produces ints; make it into a double.
;             Integer/MIN_VALUE))))     ; then make it non-negative
;                                       ; Is this a good strategy??

(def ^:const +int-range-double+ (double (- Integer/MAX_VALUE Integer/MIN_VALUE)))


;; There are probably more efficient ways to do this, rather than
;; converting the mask vector to a row matrix, then multiplying it
;; by itself to create a square matrix, and then element-multiplying
;; that by the wt-mat.
;; NOT RIGHT since first element in mask vec isn't 1 or 0.
(defn unmasked-wt-mat
   "Returns a matrix like the nnstru's full (positive and negative) weight matrix,
   but with links between masked nodes zeroed out." 
  [nnstru]
    (mx/emul (wt-mat nnstru)
             (px/square-from-vec (:mask nnstru))))


(defn gexf-graph
  "Generate a GEXF specification suitable for reading by clojure.data.xml
  functions such as `emit` and `indent-str`.  nodes is a sequence (not vector)
  of clojure.data.xml specifications for GEXF nodes, which can be generated by
  popco.io.gexf/node.  edges is the same sort of thing for edge specifications,
  which can be generated by popco.io.gexf/edges.  mode, if present, should be
  one of the keywords :static (default) or :dynamic, which determine the GEXF 
  graph mode.  Dynamic graphs allow time indexing."
  ([nodes edges] (gexf-graph nodes edges :static))
  ([nodes edges mode]
   (as-elem [:gexf {:xmlns "http://www.gexf.net/1.2draft"
                    :xmlns:viz "http://www.gexf.net/1.1draft/viz"
                    :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"
                    :xsi:schemaLocation "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd"
                    :version "1.2"}
             [:graph 
              (cond (= mode :static) {:defaultedgetype "undirected" :mode "static"} 
                    (= mode :dynamic) {:defaultedgetype "undirected" :mode "dynamic" :timeformat "integer" :start "0"}  ; TODO Is that the correct timeformat??  Why always start = 0?
                    :else (throw (Exception. (str "Bad GEXF graph mode: " mode))))
              [:attributes {:class "node"}
               [:attribute {:id "activn" :title "activation" :type "float"}
                [:default {} "0.0"]]]
              [:attributes {:class "edge"}
               [:attribute {:id "popco-wt" :title "popco weight" :type "float"}
                [:default {} "0.0"]]]
              [:nodes {:count (count nodes)} nodes]
              [:edges {:count (count edges)} edges]]])))

(defn node
  "id should be a string. It will also be used as label. 
  activn is a POPCO activation value.  ticks, if present,
  is a pair containing start and and (inclusive) end ticks."
  [id activn & tick-list]
  (let [color (cond (or (= id "SALIENT") 
                        (= id "SEMANTIC")) {:r "255" :g "0" :b "255"}
                    (pos? activn) {:r "255" :g "255" :b "0"} ; yellow
                    (neg? activn) {:r "50" :g "50" :b "255"}
                    :else {:r "100" :g "100" :b "100"})]
    [:node (merge {:id id :label id}
                  (if-let [tick (first tick-list)]
                    {:start tick :end tick}
                    {}))
     [:attvalues {} [:attvalue {:for "activn" :value (str activn)}]]
     [:viz:color color]
     [:viz:size {:value (str (* node-size-multiplier (mx/abs activn)))}] ] ))

(defn popco-to-gexf-wt
  "Translate a popco link weight into a string suitable for use as an edge
  weight in a GEXF specification for Gephi, by taking the absolute value and
  possibly making that absolute value larger or smaller."
  [popco-wt]
  (str (+ 5 (* 10 (mx/abs popco-wt)))))

(defn edge
  "node1-id and node2-id are strings that correspond to id's passed to the
  function node.  popco-wt should be a POPCO link weight.  It will determine
  edge thickness via the GEXF weight attribute via function popco-to-gexf-wt,
  but will also be stored as the value of attribute popco-wt."
  [node1-id node2-id popco-wt & tick-list]
  (let [gexf-wt (popco-to-gexf-wt popco-wt)
        color (cond (pos? popco-wt) {:r "0" :g "255" :b "0"}
                    (neg? popco-wt) {:r "255" :g "0" :b "0"}
                    :else {:r "0" :g "0" :b "0"})]
    [:edge (merge {:id (str node1-id "::" node2-id) :source node1-id :target node2-id :weight gexf-wt}
                  (if-let [tick (first tick-list)]
                    {:start tick :end tick}
                    {}))
     [:attvalues {} [:attvalue {:for "popco-wt" :value (str popco-wt)}]]
     [:viz:thickness {:value gexf-wt}] ; IGNORED, APPARENTLY
     [:viz:color color]]))

(defn nn-to-nodes
  "Given an PropnNet or AnalogyNet, return a seq of node specifications,
  one for each unmasked node, to pass to gexf-graph."
  [nnstru & tick-list]
  (let [activns (:activns nnstru)
        node-vec (:node-vec nnstru)
        key-to-node (fn [k]
                      (let [[idx] k]                       ; keys from non-zeros are vectors of length 1
                        (apply node 
                               (name (:id (node-vec idx))) ; node-vec is a Clojure vector of Propns
                               (mx/mget activns idx)       ; activns is a core.matrix vector of numbers
                               tick-list)))]
    (map key-to-node 
         (px/non-zero-indices (:mask nnstru)))))

;; Another way to do this would be with multiple :when clauses:
;; Do the :when test on the mask for i and j, and then the :let,
;; to store the wt, and then a separate :when test on wt.  Yes--
;; you can do that, and the clauses are executed in order; the :let
;; won't be executed if the first :when doesn't succeed.
(defn unmasked-non-zero-links
  "Returns a sequence of triplets containing indexes and wts from nnstru's wt-mat
  whenever wt is nonzero and is between unmasked nodes.  Doesn't distinguish
  between directed and undirected links, and assumes that all links can be
  found in the lower triangle (including diagonal) of wt-mat."
  [nnstru]
  (let [wt-mat (nn/wt-mat nnstru)
        mask (:mask nnstru)
        size (first (mx/shape mask))]
    (for [i (range size)
          j (range (inc i)) ; iterate through lower triangle including diagonal
          :let [wt (mx/mget wt-mat i j)]
          :when (and (not= 0.0 wt)
                     (pos? (mx/mget mask i))    ; mask values are never negative
                     (pos? (mx/mget mask j)))]  ;  (and almost always = 1)
      [i j wt])))

(defn nn-to-edges
  "Given an PropnNet or AnalogyNet, return a seq of edge specifications,
  one for each edge between unmasked nodes, to pass to gexf-graph.  Doesn't
  distinguish between one-way and two-way links, and assumes that the only
  one-way links are from the feeder node."
  [nnstru & tick-list]  ; TODO process tick-list
  (let [node-vec (:node-vec nnstru)
        link-to-edge (fn [[idx1 idx2 wt]]
                       (apply edge 
                              (name (:id (node-vec idx1))) ; node-vec is a Clojure vector of Propns
                              (name (:id (node-vec idx2))) ; node-vec is a Clojure vector of Propns
                              wt
                              tick-list))]
    (map link-to-edge (unmasked-non-zero-links nnstru))))


;; TODO: handle nnstrus from multiple ticks
(defn nn-to-graph
  "Returns a GEXF specification for a graph based on nnstru.  Second argument,
  if present, should be the tick (timestep) indexing this graph."
  [nnstru & tick-list]
  (if tick-list
    (gexf-graph (apply nn-to-nodes nnstru tick-list) (apply nn-to-edges nnstru tick-list) :dynamic)
    (gexf-graph (nn-to-nodes nnstru) (nn-to-edges nnstru) :static)))


--------------

(ns popco.io.gexf
  (:require [clojure.data.xml :as x]
            [clojure.core.matrix :as mx]
            [popco.core.population :as popn]
            [popco.nn.nets :as nn]
            [popco.nn.matrix :as px]))

; The xml declaration will be generated by emit and its cousins. i.e. <?xml version=\"1.0\" encoding=\"UTF-8\"?>")

;; IMPORTANT: During import into Gephi, uncheck "auto-scale".  Otherwise it does funny things with node sizes.

(def node-size-multiplier 50)

(defn gexf-graph
  "Generate a GEXF specification suitable for reading by clojure.data.xml
  functions such as `emit` and `indent-str`.  nodes is a sequence (not vector)
  of clojure.data.xml specifications for GEXF nodes, which can be generated by
  popco.io.gexf/node.  edges is the same sort of thing for edge specifications,
  which can be generated by popco.io.gexf/edges.  mode, if present, should be
  one of the keywords :static (default) or :dynamic, which determine the GEXF 
  graph mode.  Dynamic graphs allow time indexing.  first-tick is ignored
  if mode is :static."
  [nodes edges mode first-tick]
  (x/sexp-as-element [:gexf {:xmlns "http://www.gexf.net/1.2draft"
                             :xmlns:viz "http://www.gexf.net/1.1draft/viz"
                             :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"
                             :xsi:schemaLocation "http://www.gexf.net/1.2draft http://www.gexf.net/1.2draft/gexf.xsd"
                             :version "1.2"}
                      [:graph 
                       (cond (= mode :static) {:defaultedgetype "undirected" :mode "static"} 
                             (= mode :dynamic) {:defaultedgetype "undirected" :mode "dynamic" :timeformat "integer" :start (str first-tick)}  ; TODO Is that the correct timeformat??
                             :else (throw (Exception. (str "Bad GEXF graph mode: " mode))))
                       [:attributes {:class "node"}
                        [:attribute {:id "activn" :title "activation" :type "float"}
                         [:default {} "0.0"]]]
                       [:attributes {:class "edge"}
                        [:attribute {:id "popco-wt" :title "popco weight" :type "float"}
                         [:default {} "0.0"]]]
                       [:nodes {:count (count nodes)} nodes]
                       [:edges {:count (count edges)} edges]]]))


(defn node
  "id should be a string. It will also be used as label. 
  activn is a POPCO activation value."
  [id activn]
  (let [color (cond (or (= id "SALIENT") 
                        (= id "SEMANTIC")) {:r "255" :g "0" :b "255"}
                    (pos? activn) {:r "255" :g "255" :b "0"} ; yellow
                    (neg? activn) {:r "0" :g "0" :b "255"}
                    :else {:r "128" :g "128" :b "128"})]
    [:node {:id id :label id} 
     [:attvalues {} [:attvalue {:for "activn" :value (str activn)}]]
     [:viz:color color]
     [:viz:position {:x (str (- (rand 1000) 500)) :y (str (- (rand 1000) 500)) :z "0.0"}]
     [:viz:size {:value (str (* node-size-multiplier (mx/abs activn)))}] ] ))


(defn popco-to-gexf-wt
  "Translate a popco link weight into a string suitable for use as an edge
  weight in a GEXF specification for Gephi, by taking the absolute value and
  possibly making that absolute value larger or smaller."
  [popco-wt]
  (str (+ 5 (* 10 (mx/abs popco-wt)))))


(defn edge
  "node1-id and node2-id are strings that correspond to id's passed to the
  function node.  popco-wt should be a POPCO link weight.  It will determine
  edge thickness via the GEXF weight attribute via function popco-to-gexf-wt,
  but will also be stored as the value of attribute popco-wt."
  [node1-id node2-id popco-wt]
  (let [gexf-wt (popco-to-gexf-wt popco-wt)
        color (cond (pos? popco-wt) {:r "0" :g "255" :b "0"}
                    (neg? popco-wt) {:r "255" :g "0" :b "0"}
                    :else {:r "0" :g "0" :b "0"})]
    [:edge {:id (str node1-id "::" node2-id)
            :source node1-id
            :target node2-id
            :weight gexf-wt}
     [:attvalues {} [:attvalue {:for "popco-wt" :value (str popco-wt)}]]
     [:viz:size {:value gexf-wt}] ; IGNORED, APPARENTLY
     [:viz:color color]]))


(defn nn-to-nodes
  "Given an PropnNet or AnalogyNet, return a seq of node specifications,
  one for each unmasked node, to pass to gexf-graph."
  [nnstru]
  (let [activns (:activns nnstru)
        node-vec (:node-vec nnstru)
        key-to-node (fn [k]
                      (let [[idx] k]                      ; keys from non-zeros are vectors of length 1
                        (node (name (:id (node-vec idx))) ; node-vec is a Clojure vector of Propns
                              (mx/mget activns idx))))]   ; activns is a core.matrix vector of numbers
    (map key-to-node 
         (px/non-zero-indices (:mask nnstru)))))


;; Another way to do this would be with multiple :when clauses:
;; Do the :when test on the mask for i and j, and then the :let,
;; to store the wt, and then a separate :when test on wt.  Yes--
;; you can do that, and the clauses are executed in order; the :let
;; won't be executed if the first :when doesn't succeed.
(defn unmasked-non-zero-links
  "Returns a sequence of triplets containing indexes and wts from nnstru's wt-mat
  whenever wt is nonzero and is between unmasked nodes.  Doesn't distinguish
  between directed and undirected links, and assumes that all links can be
  found in the lower triangle (including diagonal) of wt-mat."
  [nnstru]
  (let [wt-mat (nn/wt-mat nnstru)
        mask (:mask nnstru)
        size (first (mx/shape mask))]
    (for [i (range size)
          j (range (inc i)) ; iterate through lower triangle including diagonal
          :let [wt (mx/mget wt-mat i j)]
          :when (and (not= 0.0 wt)
                     (pos? (mx/mget mask i))    ; mask values are never negative
                     (pos? (mx/mget mask j)))]  ;  (and almost always = 1)
      [i j wt])))


(defn nn-to-edges
  "Given an PropnNet or AnalogyNet, return a seq of edge specifications,
  one for each edge between unmasked nodes, to pass to gexf-graph.  Doesn't
  distinguish between one-way and two-way links, and assumes that the only
  one-way links are from the feeder node."
  [nnstru]
  (let [node-vec (:node-vec nnstru)
        link-to-edge (fn [[idx1 idx2 wt]]
                       (edge (name (:id (node-vec idx1))) ; node-vec is a Clojure vector of Propns
                             (name (:id (node-vec idx2))) ; node-vec is a Clojure vector of Propns
                             wt))]
    (map link-to-edge (unmasked-non-zero-links nnstru))))


(defn nn-to-graph
  "Returns a GEXF specification for a graph based on nnstru."
  [nnstru]
  (gexf-graph (nn-to-nodes nnstru)
              (nn-to-edges nnstru)
              :static ; TODO temp kludge
              0)) ; TODO temp kludge


;; NEW STRATEGY FOR DYNAMIC GRAPHS:
;;
;; Given a seq of popns, make a seq of persons at ticks, or rather nnstrus at ticks.
;; Run through the nnstrus:
;;
;; If a node appears for the first time at t1, it will have start: t1, and no end:.
;;      i.e. I need a hashmap of existing nodes in the graph so I can check for their existence
;;      Wait a minute.  Don't I already have that in the nnstru?  
;;      Something that does the same job?   i.e. just check the mask.
;;      uh, well, actually, what you'd need is the mask from the previous tick in order
;;      to know whether the node is new.  (cf. in popco1 where I needed to know new nodes
;;      for GUESS).
;; If an edge appears for the first time at t1, it will have start: t1, and no end:.
;;      i.e. I need a hashmap of existing edges in the graph so I can check for their existence
;;      (Again, I could use the previous nnstru to check for the change.)
;;
;; so either collect a new record of what was there in the past
;; or store structures from the previous nnstru
;;
;; If I'm mapping this through the populations-at-ticks, then I need to preserve that
;; what's getting passed on is a population, and not a population plus something else.
;; However, I can store extra data in the population.  e.g. a special hashmap, or 
;; last year's model of the mask and wt-mat.
;;
;; For each t:
;; Each node or edge will have an activn or popco-wt attribute with start: t and endopen: t+1.
;; For each activn and popco-wt, also assign attributes for Gephi weight, and viz:
;; properties, also with start: t and endopen: t+1.
;; This is done with attvalues and attvalue.
;; 
;; Attributes will have to be declared as dynamic in the header.
;;
;; Question: Can I give the Gephi weight and the viz: attributes timestamps??
;; (If not, then I'm not sure there's a point to bring a dynamic graph into Gephi.
;; Maybe in d3.)
;; p. 18 of the GEXF manual says:
;;
;;      About the weight: dynamic weight can be used with the reserved title "weight"
;;      in attributes. In dynamic mode, the static XML-attribute weight should be ig-
;;      nored if the dynamic one is provided.



;; TODO: handle nnstrus from multiple ticks
;(defn nn-to-graph
;  "Returns a GEXF specification for a graph based on nnstru.  Second argument,
;  if present, should be the tick (timestep) indexing this graph."
;  [nnstru & tick-list]
;  (if tick-list
;    (gexf-graph (apply nn-to-nodes nnstru tick-list) (apply nn-to-edges nnstru tick-list) :dynamic)
;    (gexf-graph (nn-to-nodes nnstru) (nn-to-edges nnstru) :static)))


;(defn nnstrus-with-mode-to-graph
;  [nnstrus mode first-tick]
;  (gexf-graph (mapcat nn-to-nodes nnstrus) 
;              (mapcat nn-to-edges nnstrus)
;              mode
;              first-tick))

;(defn nns-to-graph
;  [nnstrus]
;  (if-let [first-tick (:tick (first nnstrus))]
;    (nnstrus-with-mode-to-graph nnstrus :dynamic first-tick)
;    (nnstrus-with-mode-to-graph nnstrus :static nil)))


(defn net-with-tick
  [person-id net-key popn]
  (assoc (net-key (popn/get-person person-id popn))
         :tick (:tick popn)))

(defn analogy-net-with-tick
  [person-id popn]
  (net-with-tick person-id :analogy-net popn))

(defn propn-net-with-tick
  [person-id popn]
  (net-with-tick person-id :propn-net popn))
