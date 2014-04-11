(ns popco.core.mainloop
  (:require [popco.nn.settle :as st]
            [popco.nn.nets :as nn]
            [popco.core.communic :as cm]))

(def tick (atom 0))
(def folks (atom []))

(declare communicate update-nets once popco)

(defn popco
  [iters]
  (settle-analogy-nets folks)
  (loop [i 0
         population folks]
    (when (< i iters)
      (report-progress-to-console)
      (swap! tick inc)
      (recur (inc i) (once population)))))

(defn once
  [population]
  (->> population
    (update-nets)
    (communicate)))

(defn communicate
  [population]
  (->> population
    (cm/choose-conversers)
    (cm/choose-utterances)
    (cm/transmit-utterances)))

(defn update-nets
  [population]
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
