;;; This software is copyright 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.bali.epicureans
  (:require [popco.core.person :as pers]
            [popco.core.population :as pp]
            [sims.bali.propns :as p]))

;; One kind of person, other than the pundit: An "Epicurean god"--someone who knows of the spiritual, and neither cares nor knows about anything worldly.

;; args:                        ID  UNMASKED        PROPN-NET             ANALOGY-NET UTTERABLE-IDS          GROUPS            TALK-TO-GROUPS     MAX-TALK-TO
aa     (pers/make-person :aa    p/spiritual-propns  p/spiritual-perc-pnet p/anet      p/spiritual-propn-ids  [:pundits]        [:epicurean-gods]  1)
nina   (pers/make-person :nina  p/spiritual-propns  p/no-perc-pnet        p/anet      p/spiritual-propn-ids  [:epicurean-gods] [:epicurean-gods]  1)

(def popn (pp/make-population (vec
                                (concat
                                  [aa]
                                  (take 40 (pers/new-person-seq-from-old nina))))))
