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

(def num-subaks$ 172)
(def ticks-per-year$ 1) ; number of popco ticks every time NetLogo calls, which should be once per year, i.e every 12 NetLogo ticks

(def current-popn& (atom nil)) ; filled in later

;; EXPERIMENTAL
(def person-sd 0.02)

;; EXPERIMENTAL
(defn rand-activn
  [rng mean sd]
  (ran/truncate -1.0 1.0 ran/next-gaussian rng mean sd))

;; EXPERIMENTAL
(defn rand-node-vec
  "Returns a node vector of length n with activations initialized to
  random values from random number generator rng."
  [rng mean sd n]
  (mx/matrix (repeatedly n #(rand-activn rng mean sd))))

;; EXPERIMENTAL
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
(def randomize-propn-activns prs/randomize-unif-propn-activns)


(defn add-id-as-group
  "Returns a person that's just like pers, but with an additional group identity
  whose name is identical to pers's id."
  [pers]
  (update pers :groups conj (:id pers)))


;; PUNDITS MUST BE FIRST
(def num-pundits 2) ; used in defs below to treat pundits and subaks differently.

(reset! current-popn&
  ;;                           ID    UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS                  MAX-TALK-TO  BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/worldly-perc-pnet     c/anet      c/spiritual-propn-ids [:pundits]  [:subaks]                       1            nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/worldly-neg-perc-pnet c/anet      c/spiritual-propn-ids [:pundits]  [:subaks]                       1            nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["set at runtime from NetLogo"] 
                               num-subaks$
                               nil         prs/constantly1)]
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
  [popn speaker-listener-map]
  (let [persons (:persons popn)
        replace-ttp (fn [pers] (assoc pers :talk-to-persons (speaker-listener-map (:id pers))))]
    (assoc popn :persons
           (concat (take num-pundits persons) ; leave pundits as is
                   (map replace-ttp (drop num-pundits persons)))))) ; replace subaks' talk-to-persons from speaker-listener-map

(defn talk
  "Run popco.core.main/once on population, after updating its members'
  talk-to-persons fields from speaker-listener-hashtable, which is a
  java.util.HashTable in which keys are person ids and values are sequences
  of ids of persons the key person should talk to.  Returns a sequence of
  per-subak average activations (currently of worldly peasant propns only)
  that will be used in place of relig-type in BaliPlus.nlogo.  Values in this
  sequence are in subak order, i.e. the order in (:persons @current-popn&)."
  [speaker-listener-hashtable]
  (let [speaker-listener-map 
        (gf/fmap (partial into [])  ; values are org.nlogo.api.LogoLists, which are Collections, but we need also nth in random.clj via speak.clj, so make vecs
                 (into {} speaker-listener-hashtable))
        next-popn-fn (fn [popn] (nth (mn/many-times
                                       (replace-subaks-talk-to-persons popn speaker-listener-map))
                                     ticks-per-year$))]
    (scaled-worldly-peasant-activns (swap! current-popn& next-popn-fn))))


(def local-speaker-listener-hashtable
  "Defines a speaker-listener hashtable that encodes all and only those
  relationships specified by pest and imitation links (green lines) in
  the NetLogo simulation BaliPlus (based on Janssen's version of the
  Lansing-Kremer model). The data comes from subaksubakdata.txt
  in bali/src/LKJplus."
  (sort ; sort for programmer's convenience
   (merge-with into
               (zipmap (range 172) (repeat [])) ; Make sure every subak has an entry; some subaks are isolated, and thus don't appear in list below. 
               {1 [2]} ; The rest of this data is from subaksubakdata.txt
               {2 [1]}
               {4 [6]}
               {5 [7]}
               {5 [6]}
               {6 [4]}
               {6 [8]}
               {6 [5]}
               {7 [5]}
               {7 [10]}
               {7 [8]}
               {8 [6]}
               {8 [9]}
               {8 [7]}
               {9 [8]}
               {9 [12]}
               {9 [10]}
               {10 [7]}
               {10 [11]}
               {10 [9]}
               {11 [10]}
               {11 [13]}
               {11 [12]}
               {12 [9]}
               {12 [11]}
               {13 [11]}
               {13 [14]}
               {14 [13]}
               {14 [53]}
               {15 [16]}
               {16 [15]}
               {18 [20]}
               {20 [18]}
               {20 [21]}
               {20 [22]}
               {21 [20]}
               {21 [25]}
               {21 [23]}
               {22 [20]}
               {22 [24]}
               {23 [24]}
               {23 [21]}
               {24 [23]}
               {24 [25]}
               {24 [22]}
               {25 [21]}
               {25 [24]}
               {26 [27]}
               {27 [26]}
               {29 [30]}
               {30 [29]}
               {32 [34]}
               {32 [33]}
               {33 [35]}
               {33 [32]}
               {34 [32]}
               {34 [37]}
               {34 [35]}
               {35 [33]}
               {35 [36]}
               {35 [34]}
               {36 [35]}
               {36 [39]}
               {36 [37]}
               {37 [34]}
               {37 [38]}
               {37 [36]}
               {38 [37]}
               {38 [40]}
               {38 [39]}
               {39 [36]}
               {39 [41]}
               {39 [38]}
               {40 [38]}
               {40 [41]}
               {41 [39]}
               {41 [40]}
               {46 [49]}
               {46 [47]}
               {47 [48]}
               {47 [46]}
               {48 [47]}
               {48 [49]}
               {48 [50]}
               {49 [46]}
               {49 [48]}
               {49 [51]}
               {50 [48]}
               {50 [51]}
               {50 [52]}
               {51 [49]}
               {51 [50]}
               {52 [50]}
               {53 [14]}
               {53 [65]}
               {54 [64]}
               {54 [80]}
               {54 [62]}
               {55 [56]}
               {55 [61]}
               {56 [55]}
               {56 [57]}
               {57 [61]}
               {57 [56]}
               {57 [58]}
               {58 [57]}
               {59 [60]}
               {59 [64]}
               {60 [59]}
               {60 [67]}
               {61 [57]}
               {61 [55]}
               {62 [54]}
               {62 [71]}
               {62 [77]}
               {62 [81]}
               {63 [80]}
               {63 [82]}
               {64 [54]}
               {64 [59]}
               {64 [71]}
               {65 [53]}
               {65 [66]}
               {65 [68]}
               {65 [70]}
               {66 [67]}
               {66 [65]}
               {67 [60]}
               {67 [66]}
               {67 [68]}
               {68 [65]}
               {68 [67]}
               {68 [74]}
               {69 [74]}
               {69 [75]}
               {70 [65]}
               {70 [74]}
               {71 [62]}
               {71 [64]}
               {71 [73]}
               {72 [74]}
               {73 [71]}
               {73 [75]}
               {73 [77]}
               {74 [68]}
               {74 [69]}
               {74 [70]}
               {74 [72]}
               {75 [69]}
               {75 [73]}
               {75 [76]}
               {76 [75]}
               {76 [77]}
               {76 [79]}
               {77 [62]}
               {77 [73]}
               {77 [76]}
               {77 [78]}
               {78 [77]}
               {78 [79]}
               {78 [81]}
               {79 [76]}
               {79 [78]}
               {80 [54]}
               {80 [63]}
               {80 [81]}
               {80 [83]}
               {81 [62]}
               {81 [78]}
               {81 [80]}
               {82 [63]}
               {82 [83]}
               {83 [80]}
               {83 [82]}
               {83 [84]}
               {84 [83]}
               {84 [85]}
               {85 [84]}
               {85 [86]}
               {86 [85]}
               {86 [87]}
               {87 [86]}
               {87 [90]}
               {88 [90]}
               {88 [92]}
               {89 [90]}
               {89 [92]}
               {89 [96]}
               {90 [87]}
               {90 [88]}
               {90 [89]}
               {91 [97]}
               {92 [88]}
               {92 [89]}
               {93 [96]}
               {93 [98]}
               {94 [95]}
               {94 [97]}
               {95 [94]}
               {95 [98]}
               {96 [89]}
               {96 [93]}
               {96 [97]}
               {97 [91]}
               {97 [94]}
               {97 [96]}
               {97 [98]}
               {98 [93]}
               {98 [95]}
               {98 [97]}
               {99 [100]}
               {100 [99]}
               {102 [118]}
               {107 [108]}
               {108 [107]}
               {108 [109]}
               {109 [108]}
               {110 [111]}
               {111 [110]}
               {112 [113]}
               {113 [112]}
               {113 [114]}
               {114 [113]}
               {115 [116]}
               {116 [115]}
               {117 [118]}
               {118 [102]}
               {118 [117]}
               {120 [121]}
               {120 [122]}
               {121 [120]}
               {121 [123]}
               {122 [120]}
               {122 [123]}
               {122 [124]}
               {123 [121]}
               {123 [122]}
               {123 [125]}
               {124 [122]}
               {124 [125]}
               {124 [127]}
               {125 [123]}
               {125 [124]}
               {125 [126]}
               {126 [125]}
               {126 [127]}
               {126 [129]}
               {127 [124]}
               {127 [126]}
               {127 [128]}
               {128 [127]}
               {128 [129]}
               {128 [130]}
               {129 [126]}
               {129 [128]}
               {130 [128]}
               {137 [138]}
               {138 [137]}
               {144 [145]}
               {144 [148]}
               {144 [151]}
               {145 [144]}
               {145 [150]}
               {146 [147]}
               {146 [148]}
               {146 [149]}
               {147 [146]}
               {147 [152]}
               {148 [144]}
               {148 [146]}
               {148 [152]}
               {149 [146]}
               {150 [145]}
               {150 [151]}
               {151 [144]}
               {151 [150]}
               {151 [152]}
               {152 [147]}
               {152 [148]}
               {152 [151]}
               {153 [154]}
               {153 [155]}
               {154 [153]}
               {154 [163]}
               {155 [153]}
               {155 [156]}
               {156 [155]}
               {156 [157]}
               {157 [156]}
               {157 [158]}
               {157 [160]}
               {158 [157]}
               {158 [159]}
               {158 [161]}
               {159 [158]}
               {159 [162]}
               {160 [157]}
               {160 [161]}
               {161 [158]}
               {161 [160]}
               {162 [159]}
               {163 [154]}
               {163 [164]}
               {163 [165]}
               {163 [166]}
               {164 [163]}
               {164 [166]}
               {165 [163]}
               {165 [166]}
               {166 [164]}
               {166 [165]}
               {166 [167]}
               {167 [166]}
               {167 [168]}
               {167 [169]}
               {168 [167]}
               {168 [170]}
               {169 [167]}
               {169 [170]}
               {170 [168]}
               {170 [169]}
               {170 [171]}
               {171 [170]})))
