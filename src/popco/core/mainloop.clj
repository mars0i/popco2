(ns popco.core.mainloop
  (:use popco.core.population)
  (:require [popco.nn.settle :as st]
            [popco.nn.nets :as nn]
            [popco.core.communic :as cm]))

(def folks (atom (->Population 0 [])))

(declare communicate update-nets once many popco report-popn report-to-console)

(defn init-popco
  []
  ;; do a lot of initialization
  ;; then:
  (settle-analogy-nets folks))

(defn popco
  [popn]
  (map report-popn (many popn)))

(defn many
  [popn]
  (iterate once popn))

(defn report-pop
  [popn]
  ;; add other optional report functions here--write to file, etc.
  (report-to-console popn))

(defn erase-prev-str
  "Erase len characters from the console."
  [len]
  (print (apply str (repeat len \backspace))))

(defn report-to-console
  [popn]
  (let [tickstr (str (:tick popn))]
    (erase-prev-str (count tickstr))
    (print tickstr)))

(defn once
  [popn]
  (->> popn
    (update-nets)
    (communicate)))

(defn communicate
  [popn]
  (->> popn
    (cm/choose-conversers)
    (cm/choose-utterances)
    (cm/transmit-utterances)))

(defn update-nets
  [popn]
  (->>
    (st/settle-analogy-nets)
    (nn/update-propn-nets-from-analogy-nets)
    (st/settle-propn-nets)))

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
