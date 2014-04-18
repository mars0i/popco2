(ns popco.core.main
  (:use popco.core.population)
  (:require [popco.nn.settle :as st]
            [popco.nn.nets :as nn]
            [popco.core.communic :as cm]
            [utils.general :as ug]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SEE src/popco/start.md for notes. ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare communicate update-nets once many popco report-popn report-to-console inc-tick report)

(def folks (atom (->Population 0 [])))

(defn init
  ([] (init @folks))
  ([popn]
   ;; other initialization
   (st/settle-analogy-nets! popn)
   (reset! folks popn)))

(defn popco
  "Returns a lazy sequence of population states, one for each tick, with 
  between-tick reporting on each realized population state, starting from
  initial population state popn, or folks by default."
  ([] (popco folks))
  ([popn] (map report-popn (many popn))))

(defn many
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate once popn))

;; This should be obvious:
;; Any side-effects caused by code in 'once' will occur if it's
;; called on its own, but not on unrealized calls to it under 'iterate'.

(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state."
  [popn]
  (->> popn
    (update-nets)
    (doall)       ; make sure that changes to vectors, matrices made before communication [NEEDED?]
    (communicate)
    (doall)       ; make sure that changes to vectors, matrices made before settling [NEEDED?]
    (inc-tick)))

;; Keep this function here, in case we decide instead to put its components separately into once.
(defn update-nets
  "Implements a single timestep's (tick's) worth of network settling and updating of the
  proposition network from the analogy network.  Returns the population in its new state
  after these processes have been performed."
  [popn]
  (->> popn
    (st/settle-analogy-nets!)
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

;; Keep this function here, in case we decide instead to put its components separately into once.
(defn communicate
  "Implements a single timestep's (tick's) worth of communication.  Returns the population
  in it's new state after communication has been performed."
  [popn]
  (->> popn
    (cm/choose-conversers)  ; purely functional
    (cm/choose-utterances)  ; purely functional
    (cm/transmit-utterances))) ; this might not be purely functional

(defn report-popn
  "Wrapper for any between-tick reporting functions: Indicate progress to
  console, record activations to a file, update a plot, etc.  Should return
  the population unchanged.  Note that report functions on internal popco 
  processes such as communication must be inserted elsewhere."
  [popn]
  (report-to-console popn)
  ;; add other optional report functions here
  popn)

(defn report-to-console
  "Prints current tick to console after erasing the previous tick string."
  [popn]
  (let [tickstr (str (:tick popn))]
    (ug/erase-chars (count tickstr)) ; new tick string is always at least as long as previous
    (print tickstr)))

(defn inc-tick
  [popn]
  (assoc popn :tick (inc (:tick popn))))

;; Notes:

;; Maybe settle-analogy-net should be done just once at the beginning.
;; This seems reasonable, and would be faster, though will produce results
;; that differ from popco1 simply because communication can depend on
;; subtle variations.  However, in existing popco1 analogy nets,
;; there was (small) cycling behavior, which can affect propn links, and
;; therefore everything.  This won't play a role if I pull this out
;; of the main loop.  But also, then what should I do about cycles?
;; set to their average values?

;; Hmm well if I do take analogy-net settling out of the main loop,
;; then the contribution of the analogy net to the propn net links
;; would also be static, right?  Because the analogy net activations
;; don't change.  So the only thing that would change in the propn net
;; would be due to the bump that you get from receiving a propn, etc.


;; Note popco1's update-analogy-net isn't needed.  It revised the
;; structure of the analogy net in response to new propns.  That's
;; now done once and for all at the beginning.  And transmit-utterance
;; can cause the unmasking.  This is done in communic/receive-propn!.
