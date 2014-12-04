;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.epicureans
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.collections :as c]))

;; One kind of person, other than the pundit: An "Epicurean god"--someone who knows of the spiritual, and neither cares nor knows about anything worldly.

;; args:               ID    UNMASKED           PROPN-NET                 ANALOGY-NET UTTERABLE-IDS          GROUPS            TALK-TO-GROUPS     MAX-TALK-TO
(let [aat  (pers/make-person :aat  c/spiritual-propns c/spiritual-perc-pnet     c/anet      c/spiritual-propn-ids  [:pundits]        [:epicurean-gods]  1)
      aaf  (pers/make-person :aaf  c/spiritual-propns c/spiritual-neg-perc-pnet c/anet      c/spiritual-propn-ids  [:pundits]        [:epicurean-gods]  1)
      nina (pers/make-person :nina c/spiritual-propns c/no-perc-pnet            c/anet      c/spiritual-propn-ids  [:epicurean-gods] [:epicurean-gods]  1)]

  (def popn (pp/make-population (vec
                                  (concat
                                    [aat aaf]
                                    (take 40 (pers/new-person-seq-from-old nina)))))))
