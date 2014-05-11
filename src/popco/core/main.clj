(ns popco.core.main
  (:use popco.core.population)
  (:require [popco.nn.update :as up]
            [popco.nn.nets :as nn]
            [popco.core.communic :as cm]
            [utils.general :as ug]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SEE src/popco/start.md and src/popco/core/main.md for notes. ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Note: There's no need to provide for the possibility of turning off
;; the conversation functions.  They can be disabled simply by putting
;; each individual in a distinct group.  (It should be possible to
;; change group membership over time, too.)

(declare once many-times unparalleled-many-times ticker inc-tick)

(def folks (atom (->Population 0 []))) ; ok to use Long for tick

;; An earlier version included a report-fn argument.  I now think it's
;; better to just map or doseq such functions, externally, over the
;; lazy sequence returned by this function.
(defn many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate once popn))

;; It's not clear that it will ever be necessary to use 'map' rather than
;; 'pmap' for mapfn, except for testing (which should be done, e.g. on Cheaha).
;; See comment on many-times about earlier, more complicated versions.
(defn unparalleled-many-times
  "Returns a lazy sequence of population states, one for each tick.
  No between-tick reporting is done when the sequence is realized."
  [popn]
  (iterate (partial once map) popn))

(def per-person-fns (comp cm/choose-conversations up/update-person-nets))

;; It's not clear that it will ever be necessary to use 'map' rather than
;; 'pmap' for mapfn, except for testing (which should be done, e.g. on Cheaha).
;;
;; If mapfn = map, it's lazy; iterating through once calls won't realize
;; the persons in each tick (until later). Why would we want that, even if
;; it's useful to iterate through ticks/generations lazily? (Will this become
;; irrelevant when transmit-utterances no longer simply passes the population
;; through?  Without doall, would it be possible to look only
;; at once person in generation 500, let's say, and then realize only
;; its communicative ancestors?  Maybe that would be efficient.  But then
;; you'd have to wait awhile to realize that one person.)
(defn once
  "Implements a single timestep's (tick's) worth of evolution of the population.
  Returns the population in its new state.  Supposed to be purely functional. (TODO: Is it?)"
  ([popn] (once pmap popn))
  ([mapfn popn]
   (->Population
     (inc (:tick popn))
     (doall
       (cm/transmit-utterances 
         (mapfn per-person-fns (:members popn)))))))

(defn once-no-communic-popco1-style
  "Implements a single timestep's (tick's) worth of evolution of the population,
  without communication.  Returns the population in its new state.  Implements 
  non-communication steps in the same order as popco1 for comparison with it."
  [popn]
  (->Population
    (inc (:tick popn))
    (doall
      (pmap up/update-person-nets-popco1-style (:members popn)))))


(defn ticker
  "Prints tick number to console, erasing previous tick number, and returns
  the population unchanged."
  [popn]
  (let [tickstr (str (:tick popn))]
    (ug/erase-chars (count tickstr)) ; new tick string is always at least as long as previous
    (print tickstr)
    (flush))
  popn)

(defn dotter
  "Prints dots to console, and returns the population unchanged."
  [popn]
  (print ".")
  (flush)
  popn)

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
