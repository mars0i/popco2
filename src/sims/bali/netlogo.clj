;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.netlogo
  (:require [popco.core.main :as mn]
            [popco.core.person :as prs]
            [popco.core.population :as pp]
            [popco.nn.matrix :as px]
            [utils.random :as ran]
            [sims.bali.collections :as c]
            [clojure.core.matrix :as mx]
            [clojure.algo.generic.functor :as gf]))

;; NOTE: I adopt the convention of naming variables containing atoms with a trailing ampersand,
;; and naming namespace-global variables that don't contain atoms with a trailing $.
;; (Elsewhere I used initial and terminal stars, but that actually has a more specific meaning.)

(declare pest-neighbor-map
         rand-activn rand-node-vec double-randomize-propn-activns add-id-as-group scaled-worldly-peasant-activn scaled-worldly-peasant-activns
         replace-subaks-talk-to-persons many-times-repl-ttp talk)

;; Override definition of trust in popco.core.constants.  Why it's OK during init, what to watch out for: http://stackoverflow.com/questions/5181367/is-defn-thread-safe .
(in-ns 'popco.core.constants) (def trust 1.0) (in-ns 'sims.bali.netlogo)
(println "popco.core.constants/trust now =" popco.core.constants/trust)

(def num-subaks$ 172)
(def ticks-per-year$ 1) ; number of popco ticks every time NetLogo calls, which should be once per year, i.e every 12 NetLogo ticks

(def current-popn& (atom nil)) ; filled in later

(def randomize-propn-activns prs/randomize-unif-propn-activns)

(def num-pundits 2) ; used in defs below to treat pundits and subaks differently.

;; not sure why this has to be defined before its used even though in declare above
(defn add-id-as-group
  "Returns a person that's just like pers, but with an additional group identity
  whose name is identical to pers's id."
  [pers]
  (update pers :groups conj (:id pers)))

;; NOTES
;; Both pundits only utter spiritual propns if we are adopting the hypothesis that religious patterns spread
;; randomly, and were only selected through success bias.
;; In addition, we could add in some worldly-peasant analogy bias.
;; NOTE speakers only send a valence (-1 or 1), although whether uttered is random.
;; Then there's a constant trust multiplier in listen.clj.  An option is to replace
;; this with a normally distributed number.  Or maybe send more than just valence.
;; Hmm.  This is different from the pure NetLogo version.  Then again, sending a bunch
;; of propns is already noisy.  Not noisy enough, maybe, though.
;; 
;; The only place that I use random numbers in popco, after initialization, is in speak.clj:
;; Randomness is used to decide who speaks to whom, which propns are worth saying, and which
;; of them get uttered to whom.
;; 
;; What's transmitted from speaker to listener as the value of a proposition is only a valence,
;; i.e. 1 or -1, along with (possibly) a speaker-quality value.
;; The speaker quality is at present a simple value stored in the speaker; it's not
;; calculated during speaking.
;; Speaker quality is intended for listeners to decide who to listen to using success bias, etc.
;;
;; The valence is used in listen.clj to produce the numeric effect on the listener's propn
;; using a fixed trust multiplier.  i.e. this is not randomized in any way.  At present.

;; PUNDITS MUST BE FIRST
(reset! current-popn&
  ;;                           ID    UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS                  MAX-TALK-TO  BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/spiritual-perc-pnet     c/anet      c/spiritual-propn-ids [:pundits]  [:subaks]                       1            nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/spiritual-neg-perc-pnet c/anet      c/spiritual-propn-ids [:pundits]  [:subaks]                       1            nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["set at runtime from NetLogo"] num-subaks$  nil         prs/constantly1)]
    (pp/make-population
      (vec (concat [aat aaf] ; pundits are first 
                   (map (comp randomize-propn-activns
                              add-id-as-group         ; give it a group name identical to its id
                              (partial prs/new-person-from-old subak))
                        (map double (range num-subaks$)))))))) ; subak ids are doubles from 0 to num-subaks$ - 1. (That's what NetLogo will send.)

;; To get the mean, we divide by num propns; to scale result from [-1,1] to [-0.5,0.5], we also divide by 2.
(def num-worldly-peasant-propns-2x (* 2 (count c/worldly-peasant-propn-idxs)))

(defn scaled-worldly-peasant-activn
  "Computes mean of activations of worldly-peasant propns in person pers and scales
  the result to lie in [0,1]."
  [pers]
  ;(println (matrix :persistent-vector (:activns (:propn-net pers))))  ; DEBUG
  (+ 0.5    ; shift [-0.5,0.5] to [0,1]
     (/ (mx/esum
          (mx/select (:activns (:propn-net pers)) 
                     c/worldly-peasant-propn-idxs))
        num-worldly-peasant-propns-2x))) ; to get the mean, we divide by num propns; to scale result from [-1,1] to [-0.5,0.5], we also divide by 2

(defn scaled-worldly-peasant-activns
  "Returns sequence of mean activations of worldly-peasant propns for each subak,
  in the order in which subaks appear in (:persons popn)."
  [popn]
  (map scaled-worldly-peasant-activn 
       (drop num-pundits
             (:persons popn))))

(defn replace-subaks-talk-to-persons
  "Replace talk-to-persons fields in persons in popn speaker-listener-map,
  in which keys are person ids and values are sequences of ids of persons 
  the key person should talk to."
  [speaker-listener-map popn]
  (let [persons (:persons popn)
        replace-ttp (fn [pers] (assoc pers :talk-to-persons (speaker-listener-map (:id pers))))]
    (assoc popn :persons
           (concat (take num-pundits persons) ; leave pundits as is
                   (map replace-ttp (drop num-pundits persons)))))) ; replace subaks' talk-to-persons from speaker-listener-map

(defn many-times-repl-ttp
  "Run many-times on popn (@current-popn& by default) after calling 
  replace-subaks-talk-to-persons on it with speaker-listener-map."
  ([] 
   (println "Using 'fake NetLogo' all-clojure pest-neighbor-map to set talk-to relationships.")
   (many-times-repl-ttp pest-neighbor-map @current-popn&))
  ([speaker-listener-map] (many-times-repl-ttp speaker-listener-map @current-popn&))
  ([speaker-listener-map popn]
   (mn/many-times (replace-subaks-talk-to-persons speaker-listener-map popn))))

(defn talk
  "Run popco.core.main/once on population, after updating its members'
  talk-to-persons fields from speaker-listener-hashtable, which is a
  java.util.HashTable in which keys are person ids and values are sequences
  of ids of persons the key person should talk to.  Returns a sequence of
  per-subak average activations (currently of worldly peasant propns only)
  that will be used in place of relig-type in BaliPlus.nlogo.  Values in this
  sequence are in subak order, i.e. the order in (:persons @current-popn&).
  (Additional args are designed for experimentation at the repl.)"
  ([speaker-listener-hashtable] (talk speaker-listener-hashtable ticks-per-year$))
  ([speaker-listener-hashtable ticks]
   (let [speaker-listener-map 
         (gf/fmap (partial into [])  ; values are org.nlogo.api.LogoLists, which are Collections, but we need also nth in random.clj via speak.clj, so make vecs
                  (into {} speaker-listener-hashtable))
         next-popn-fn (fn [popn] (nth 
                                   (many-times-repl-ttp speaker-listener-map popn)
                                   ticks))]
     (scaled-worldly-peasant-activns (swap! current-popn& next-popn-fn)))))

(def pest-neighbor-map
  "Defines a speaker-listener map (hashtable) that encodes all and only 
  those relationships specified by pest and imitation links (green lines)
  in the NetLogo simulation BaliPlus (based on Janssen's version of the
  Lansing-Kremer model). The data comes from subaksubakdata.txt
  in bali/src/LKJplus."
  (merge-with into
              (zipmap (range 172) (repeat [])) ; Make sure every subak has an entry; some subaks are isolated, and thus don't appear in list below. 
              {1.0 [2.0]} ; The rest of this data is from subaksubakdata.txt
              {2.0 [1.0]}
              {4.0 [6.0]}
              {5.0 [7.0]}
              {5.0 [6.0]}
              {6.0 [4.0]}
              {6.0 [8.0]}
              {6.0 [5.0]}
              {7.0 [5.0]}
              {7.0 [10.0]}
              {7.0 [8.0]}
              {8.0 [6.0]}
              {8.0 [9.0]}
              {8.0 [7.0]}
              {9.0 [8.0]}
              {9.0 [12.0]}
              {9.0 [10.0]}
              {10.0 [7.0]}
              {10.0 [11.0]}
              {10.0 [9.0]}
              {11.0 [10.0]}
              {11.0 [13.0]}
              {11.0 [12.0]}
              {12.0 [9.0]}
              {12.0 [11.0]}
              {13.0 [11.0]}
              {13.0 [14.0]}
              {14.0 [13.0]}
              {14.0 [53.0]}
              {15.0 [16.0]}
              {16.0 [15.0]}
              {18.0 [20.0]}
              {20.0 [18.0]}
              {20.0 [21.0]}
              {20.0 [22.0]}
              {21.0 [20.0]}
              {21.0 [25.0]}
              {21.0 [23.0]}
              {22.0 [20.0]}
              {22.0 [24.0]}
              {23.0 [24.0]}
              {23.0 [21.0]}
              {24.0 [23.0]}
              {24.0 [25.0]}
              {24.0 [22.0]}
              {25.0 [21.0]}
              {25.0 [24.0]}
              {26.0 [27.0]}
              {27.0 [26.0]}
              {29.0 [30.0]}
              {30.0 [29.0]}
              {32.0 [34.0]}
              {32.0 [33.0]}
              {33.0 [35.0]}
              {33.0 [32.0]}
              {34.0 [32.0]}
              {34.0 [37.0]}
              {34.0 [35.0]}
              {35.0 [33.0]}
              {35.0 [36.0]}
              {35.0 [34.0]}
              {36.0 [35.0]}
              {36.0 [39.0]}
              {36.0 [37.0]}
              {37.0 [34.0]}
              {37.0 [38.0]}
              {37.0 [36.0]}
              {38.0 [37.0]}
              {38.0 [40.0]}
              {38.0 [39.0]}
              {39.0 [36.0]}
              {39.0 [41.0]}
              {39.0 [38.0]}
              {40.0 [38.0]}
              {40.0 [41.0]}
              {41.0 [39.0]}
              {41.0 [40.0]}
              {46.0 [49.0]}
              {46.0 [47.0]}
              {47.0 [48.0]}
              {47.0 [46.0]}
              {48.0 [47.0]}
              {48.0 [49.0]}
              {48.0 [50.0]}
              {49.0 [46.0]}
              {49.0 [48.0]}
              {49.0 [51.0]}
              {50.0 [48.0]}
              {50.0 [51.0]}
              {50.0 [52.0]}
              {51.0 [49.0]}
              {51.0 [50.0]}
              {52.0 [50.0]}
              {53.0 [14.0]}
              {53.0 [65.0]}
              {54.0 [64.0]}
              {54.0 [80.0]}
              {54.0 [62.0]}
              {55.0 [56.0]}
              {55.0 [61.0]}
              {56.0 [55.0]}
              {56.0 [57.0]}
              {57.0 [61.0]}
              {57.0 [56.0]}
              {57.0 [58.0]}
              {58.0 [57.0]}
              {59.0 [60.0]}
              {59.0 [64.0]}
              {60.0 [59.0]}
              {60.0 [67.0]}
              {61.0 [57.0]}
              {61.0 [55.0]}
              {62.0 [54.0]}
              {62.0 [71.0]}
              {62.0 [77.0]}
              {62.0 [81.0]}
              {63.0 [80.0]}
              {63.0 [82.0]}
              {64.0 [54.0]}
              {64.0 [59.0]}
              {64.0 [71.0]}
              {65.0 [53.0]}
              {65.0 [66.0]}
              {65.0 [68.0]}
              {65.0 [70.0]}
              {66.0 [67.0]}
              {66.0 [65.0]}
              {67.0 [60.0]}
              {67.0 [66.0]}
              {67.0 [68.0]}
              {68.0 [65.0]}
              {68.0 [67.0]}
              {68.0 [74.0]}
              {69.0 [74.0]}
              {69.0 [75.0]}
              {70.0 [65.0]}
              {70.0 [74.0]}
              {71.0 [62.0]}
              {71.0 [64.0]}
              {71.0 [73.0]}
              {72.0 [74.0]}
              {73.0 [71.0]}
              {73.0 [75.0]}
              {73.0 [77.0]}
              {74.0 [68.0]}
              {74.0 [69.0]}
              {74.0 [70.0]}
              {74.0 [72.0]}
              {75.0 [69.0]}
              {75.0 [73.0]}
              {75.0 [76.0]}
              {76.0 [75.0]}
              {76.0 [77.0]}
              {76.0 [79.0]}
              {77.0 [62.0]}
              {77.0 [73.0]}
              {77.0 [76.0]}
              {77.0 [78.0]}
              {78.0 [77.0]}
              {78.0 [79.0]}
              {78.0 [81.0]}
              {79.0 [76.0]}
              {79.0 [78.0]}
              {80.0 [54.0]}
              {80.0 [63.0]}
              {80.0 [81.0]}
              {80.0 [83.0]}
              {81.0 [62.0]}
              {81.0 [78.0]}
              {81.0 [80.0]}
              {82.0 [63.0]}
              {82.0 [83.0]}
              {83.0 [80.0]}
              {83.0 [82.0]}
              {83.0 [84.0]}
              {84.0 [83.0]}
              {84.0 [85.0]}
              {85.0 [84.0]}
              {85.0 [86.0]}
              {86.0 [85.0]}
              {86.0 [87.0]}
              {87.0 [86.0]}
              {87.0 [90.0]}
              {88.0 [90.0]}
              {88.0 [92.0]}
              {89.0 [90.0]}
              {89.0 [92.0]}
              {89.0 [96.0]}
              {90.0 [87.0]}
              {90.0 [88.0]}
              {90.0 [89.0]}
              {91.0 [97.0]}
              {92.0 [88.0]}
              {92.0 [89.0]}
              {93.0 [96.0]}
              {93.0 [98.0]}
              {94.0 [95.0]}
              {94.0 [97.0]}
              {95.0 [94.0]}
              {95.0 [98.0]}
              {96.0 [89.0]}
              {96.0 [93.0]}
              {96.0 [97.0]}
              {97.0 [91.0]}
              {97.0 [94.0]}
              {97.0 [96.0]}
              {97.0 [98.0]}
              {98.0 [93.0]}
              {98.0 [95.0]}
              {98.0 [97.0]}
              {99.0 [100.0]}
              {100.0 [99.0]}
              {102.0 [118.0]}
              {107.0 [108.0]}
              {108.0 [107.0]}
              {108.0 [109.0]}
              {109.0 [108.0]}
              {110.0 [111.0]}
              {111.0 [110.0]}
              {112.0 [113.0]}
              {113.0 [112.0]}
              {113.0 [114.0]}
              {114.0 [113.0]}
              {115.0 [116.0]}
              {116.0 [115.0]}
              {117.0 [118.0]}
              {118.0 [102.0]}
              {118.0 [117.0]}
              {120.0 [121.0]}
              {120.0 [122.0]}
              {121.0 [120.0]}
              {121.0 [123.0]}
              {122.0 [120.0]}
              {122.0 [123.0]}
              {122.0 [124.0]}
              {123.0 [121.0]}
              {123.0 [122.0]}
              {123.0 [125.0]}
              {124.0 [122.0]}
              {124.0 [125.0]}
              {124.0 [127.0]}
              {125.0 [123.0]}
              {125.0 [124.0]}
              {125.0 [126.0]}
              {126.0 [125.0]}
              {126.0 [127.0]}
              {126.0 [129.0]}
              {127.0 [124.0]}
              {127.0 [126.0]}
              {127.0 [128.0]}
              {128.0 [127.0]}
              {128.0 [129.0]}
              {128.0 [130.0]}
              {129.0 [126.0]}
              {129.0 [128.0]}
              {130.0 [128.0]}
              {137.0 [138.0]}
              {138.0 [137.0]}
              {144.0 [145.0]}
              {144.0 [148.0]}
              {144.0 [151.0]}
              {145.0 [144.0]}
              {145.0 [150.0]}
              {146.0 [147.0]}
              {146.0 [148.0]}
              {146.0 [149.0]}
              {147.0 [146.0]}
              {147.0 [152.0]}
              {148.0 [144.0]}
              {148.0 [146.0]}
              {148.0 [152.0]}
              {149.0 [146.0]}
              {150.0 [145.0]}
              {150.0 [151.0]}
              {151.0 [144.0]}
              {151.0 [150.0]}
              {151.0 [152.0]}
              {152.0 [147.0]}
              {152.0 [148.0]}
              {152.0 [151.0]}
              {153.0 [154.0]}
              {153.0 [155.0]}
              {154.0 [153.0]}
              {154.0 [163.0]}
              {155.0 [153.0]}
              {155.0 [156.0]}
              {156.0 [155.0]}
              {156.0 [157.0]}
              {157.0 [156.0]}
              {157.0 [158.0]}
              {157.0 [160.0]}
              {158.0 [157.0]}
              {158.0 [159.0]}
              {158.0 [161.0]}
              {159.0 [158.0]}
              {159.0 [162.0]}
              {160.0 [157.0]}
              {160.0 [161.0]}
              {161.0 [158.0]}
              {161.0 [160.0]}
              {162.0 [159.0]}
              {163.0 [154.0]}
              {163.0 [164.0]}
              {163.0 [165.0]}
              {163.0 [166.0]}
              {164.0 [163.0]}
              {164.0 [166.0]}
              {165.0 [163.0]}
              {165.0 [166.0]}
              {166.0 [164.0]}
              {166.0 [165.0]}
              {166.0 [167.0]}
              {167.0 [166.0]}
              {167.0 [168.0]}
              {167.0 [169.0]}
              {168.0 [167.0]}
              {168.0 [170.0]}
              {169.0 [167.0]}
              {169.0 [170.0]}
              {170.0 [168.0]}
              {170.0 [169.0]}
              {170.0 [171.0]}
              {171.0 [170.0]}))

;;;;;;;;;;;;;;;;;;;;;;;;
;; EXPERIMENTAL CODE
(def person-sd 0.02)

(defn rand-activn
  [rng mean sd]
  (ran/truncate -1.0 1.0 ran/next-gaussian rng mean sd))

(defn rand-node-vec
  "Returns a node vector of length n with activations initialized to
  random values from random number generator rng."
  [rng mean sd n]
  (mx/matrix (repeatedly n #(rand-activn rng mean sd))))

(defn double-randomize-propn-activns
  "Accepts a single argument, a person pers, and returns a person containing
  a fresh proposition network with random activation values.  
  THIS EXPERIMENTAL VERSION ARRANGES NORMALLY DISTRIBUTED ACTIVNS IN A PERSON 
  AROUND THE SAME RANDOM MEAN, WITH A UNIFORMLY DISTRIBUTED DIFFERENT MEAN IN 
  EACH PERSON THE NORMALLY DISTRIBUTED ACTIVNS ARE TRUNCATED to [-1,1]. 
  (THEIR MEANS ARE CLOSER TO 0 THAN THEIR MODES.)"
  [pers]
  (let [rng (:rng pers)
        num-nodes (px/vec-count (:activns (:propn-net pers))) ; redundant to do every time, but ok for initialization
        person-mean (- (* (ran/next-double rng) 2.0) 1.0)] ; a double in [-1,1.0)
    (assoc-in pers [:propn-net :activns]
              (rand-node-vec rng person-mean person-sd num-nodes)))) ;  person-sd is from global

;(def randomize-propn-activns double-randomize-propn-activns)
;; END EXPERIMENTAL CODE
;;;;;;;;;;;;;;;;;;;;;;;;
