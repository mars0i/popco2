;; doesn't work
(defn strict-get-1 [rec key]
  (get rec key (throw (Exception. "No value."))))

;; doesn't work
(defmacro strict-get-2 [rec key]
  `(get ~rec ~key (throw (Exception. "No value."))))

;; works but has extra work
(defn strict-get-3 [rec key]
  (let [unique-thing (gensym)
        value (get rec key unique-thing)]
    (if (= value unique-thing)
      (throw (Exception. "No value."))
      value)))

;; works but in theory could be thwarted by inadvertent use of same value
(defn strict-get-4 [rec key]
  (let [unique-thing :i_WoUlD_nEvEr_nAmE_aNyThInG_lIkE_tHiS_649933464187646
        value (get rec key unique-thing)]
    (if (= value unique-thing)
      (throw (Exception. "No value."))
      value)))

;; The rest of these are answers to my question
;; http://stackoverflow.com/questions/24443985/get-replacement-that-throws-exception-on-not-found/24460443#24460443

;; amalloy's
(defn strict-get-a [m k]
  (if-let [[k v] (find m k)]
    v
    (throw (Exception. "Just leave me alone!"))))

;; noisesmith's
(defn strict-get-n
  [place key]
  {:pre [(contains? place key)]}
  (get place key))

;; mange's
;; i.e. basically same as strict-get-4, but with an additional colon
(defn strict-get-m1 [coll key]
  (let [result (get coll key ::not-found)]
    (if (= result ::not-found)
      (throw (Exception. "No value."))
      result)))

;; mange's
(defn strict-get-m2 [coll key]
  (if (contains? coll key)
    (get coll key)
    (throw (Exception. "No value."))))
