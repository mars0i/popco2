;;; This software is copyright 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.netlogo
  (:require [popco.core.main :as mn]
            [popco.core.person :as prs]
            [popco.core.population :as pp]
            ;[popco.communic.listen :as cl]
            [popco.nn.analogy :as an]
            [sims.bali.collections :as c]))

(def num-subaks 172)

;; I adopt the convention of naming variables containing atoms with a trailing ampersand:
(def popn& (atom "not-yet-defined"))

(defn bali-init 
  "Create a population of popco persons representing subaks, storing it in popn&."
  []
               ;;               ID   UNMASKED         PROPN-NET               ANALOGY-NET UTTERABLE-IDS         GROUPS      TALK-TO-GROUPS MAX-TALK-TO BIAS-FILTER QUALITY-FN
  (let [aat   (prs/make-person :aat  c/worldly-propns c/worldly-perc-pnet     c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        aaf   (prs/make-person :aaf  c/worldly-propns c/worldly-neg-perc-pnet c/anet      c/worldly-propn-ids   [:pundits]  [:subaks]      1           nil         prs/constantly1)
        subak (prs/make-person :temp c/all-propns     c/no-perc-pnet          c/anet      c/spiritual-propn-ids [:subaks]   ["ignored"]    num-subaks  nil         prs/constantly1)]
    (reset! popn&
            (concat [aat aaf]
                    (map (partial prs/new-person-from-old subak)
                         (map double (range num-subaks))))))) ; subak ids: Doubles from 0 to num-subaks-1. that's what NetLogo will send.

;; NOTE RE THE ZIPMAP from two sequences:
;; Maybe I can construct the map in NetLogo. ??
;; The NetLogo table extension makes tables, which extends java.util.LinkedHashMap,
;; which implements interface java.util.Map, as do clojure.lang.PersistentHashMap
;; and clojure.lang.PersistentArrayMap.  So ... I wonder whether I might be able to
;; just pass the NetLogo table over to Clojure....  Which would be cool.
;; Note the official main interface for these guys in Clojure is IPersistentMap,
;; which doesn't directly/indirectly extend java.util.map.
;; cf. http://david-mcneil.com/post/16535755677/clojure-custom-map
;; OH MAN see this:
;; http://stackoverflow.com/a/1666053/1455243
;; i.e. even though the Java maps are not directly usable as if they were 
;; Clojure maps, you easily can make one into the other using `(into {} <a java.util.HashMap)` 
;; and `(java.util.HashMap. {:a 1 :b 2})`.
;; SO I SHOULD TRY SIMPLY PASSING A NETLOGO TABLE INTO CLOJURE, AND THEN MAKING
;; A CLOJURE MAP USING into.  (Question: Is that faster or slower then the whole
;; zipmap version passing sequences?  Note that either way, I'm building a new
;; table every 12 NetLogo ticks.  It's just a question of whether I build it in
;; NetLogo or Clojure.  Well, also, either way I'm creating a new data structure
;; in both places.  Either sequences in NetLogo, and then a map in Clojure,
;; or a HashMap in NetLogo, and then converting it to a Clojure map.
(defn update-talk-to-persons
  "Update talk-to-persons fields in persons in popn based on args:
  speaker-ids and listener-id-seqs are sequences of the same length.  Each
  sequence of speaker ids in listener-id-seqs provides the ids to be used to
  fill talk-to-persons in the person with the corresponding id in speaker-ids."
  [popn speaker-listener-map]
  (assoc popn :persons  ; replace persons in popn
         (map #(assoc % :talk-to-persons (speaker-listener-map (:id %))) ; with old persons but with talk-to-persons updated from appropriate value in speaker-listener-map
              (:persons popn))))

(defn avg-worldly-activns
  [popn]
  )

(defn bali-once
  "Run once on population after updating its members' talk-to-persons fields.
  speaker-listener-hashtable is a java.util.HashTable in which keys are person
  ids and values are sequences of ids of persons to talk to."
  [speaker-listener-hashtable]
  (let [speaker-listener-map (into {} speaker-listener-hashtable)]
  (swap! popn& 
         (mn/once 
           (update-talk-to-persons @popn& speaker-listener-map)))
  (avg-worldly-activns @popn&) ;; return per-subak average worldly activn vals
  (reverse subak-ids)) ; for testing only - delete this line
