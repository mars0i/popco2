(ns popco.core.mainloop )

(defn once
  [tick population]
  (report-progress-to-console)
  [(inc tick)
   (->> population
     (update-propn-nets-from-analogy-nets)
     (settle-nets)
     (choose-conversers)
     (choose-utterances)
     (transmit-utterances)
     (update-analogy-nets))])
