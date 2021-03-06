;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

(ns sims.crime3.propns
  (:use popco.core.lot)
  (:require [popco.nn.propn :as pn]
            [popco.nn.analogy :as an]))

;; declare all proposition names
;(declare V-ip V-na V-ia V-ha V-ia->v-ha V-ipa V-ipa->v-ia V-ica V-ica->-v-ipa V-ica->-v-ipa->v-na V-qp V-qp->-v-ipa V-qp->-v-ipa->v-na
;         B-pp B-ab B-abp B-ab->b-abp B-hrp B-abp->b-hrp B-hlb B-abp->b-hlb B-cpb B-cpb->-b-abp B-dtp B-cpb->b-dtp 
;         CV-cp CV-na CV-ca CV-ha CV-ca->cv-ha CV-rpa CV-rpa->cv-ca CV-sa CV-sa->-cv-rpa CV-sa->-cv-rpa->cv-na CV-ip CV-ip->-cv-rpa CV-ip->-cv-rpa->cv-na 
;         CB-np CB-ap CB-vpp CB-ap->cb-vpp CB-hrp CB-vpp->cb-hrp CB-hlp CB-vpp->cb-hlp CB-cpc CB-cpc->-cb-vpp CB-dtp CB-cpc->cb-dtp)

(defpred Aggressive)
(defpred Attack)
(defpred Capture)
(defpred Causal-if)
(defpred Danger-to)
(defpred Harms)
(defpred Helps)
(defpred Human)
(defpred Imprison)
(defpred Infect)
(defpred Inoculate)
(defpred Is-criminal)
(defpred Is-infected)
(defpred Not-criminal)
(defpred Not-infected)
(defpred Preventative-if)
(defpred Quarantine)
(defpred Recruit)
(defpred Support)
(defpred Victimize)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; virus propns

(defobj ob-vpers0)
(defobj ob-vpers1)

(def virus-propns
  [(defpropn V-ip Is-infected [ob-vpers0])                ; 0. Person 0 has infection.
   (defpropn V-na Not-infected [ob-vpers1])               ; 1. Person 1 lacks infection.
   (defpropn V-ia Is-infected [ob-vpers1])                ; 2. Person 1 has infection.
   (defpropn V-ha Harms [ob-vpers1])                      ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
   (defpropn V-ia->v-ha Causal-if [V-ia V-ha])           ; 4. That person 1 has infection is harmful to person 1. [HO1]
   (defpropn V-ipa Infect [ob-vpers0 ob-vpers1])             ; 5. Person 0, who already has infection, infects person 1.
   (defpropn V-ipa->v-ia Causal-if [V-ipa V-ia])         ; 6. The infecting of person 1 by person 0 causes person 1 to have infection. [HO1]
   (defpropn V-ica Inoculate [ob-vpers1])                 ; 7. Person 1 gets innoculated.
   (defpropn V-ica->-v-ipa Preventative-if [V-ica V-ipa]) ; 8. That person 1 is innoculated prevents person 0 from infecting person 1. [HO1]
   (defpropn V-ica->-v-ipa->v-na Causal-if [V-ica->-v-ipa V-na])  ; 9. That the innoculating prevents the infecting causes [preserves] person 1 lacking infection. [HO2]
   (defpropn V-qp Quarantine [ob-vpers0])                 ; 10. Person 0 is quarantined.
   (defpropn V-qp->-v-ipa Preventative-if [V-qp V-ipa])  ; 11. That person 0 is quarantined prevents person 0 from infecting person 1.
   (defpropn V-qp->-v-ipa->v-na Causal-if [V-qp->-v-ipa V-na]) ; 12. That (quarantining 0 prevents 0 from infecting 1) causes [preserves] person 1 lacking infection. [HO2]
  ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; preventative ("viral") crime propns

(defobj ob-cpers0)
(defobj ob-cpers1)

(def viral-crime-propns
  [
   (defpropn CV-cp Is-criminal [ob-cpers0])                  ; 0. Person 0 is a criminal.
   (defpropn CV-na Not-criminal [ob-cpers1])                 ; 1. Person 1 is not a criminal (or is innocent).
   (defpropn CV-ca Is-criminal [ob-cpers1])                  ; 2. Person 1 is a criminal.
   (defpropn CV-ha Harms [ob-cpers1])                        ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
   (defpropn CV-ca->cv-ha Causal-if [CV-ca CV-ha])           ; 4. Person 1 being a criminal is harmful to person 1.
   (defpropn CV-rpa Recruit [ob-cpers0 ob-cpers1])              ; 5. Person 0 recruits person 1 into crime.
   (defpropn CV-rpa->cv-ca Causal-if [CV-rpa CV-ca])         ; 6. Person 0 recruiting person 1 causes person 1 to become a criminal. [HO1]
   (defpropn CV-sa Support [ob-cpers1])                      ; 7. Person 1 is [financially, parentally, socially, educationally, etc.] supported.
   (defpropn CV-sa->-cv-rpa Preventative-if [CV-sa CV-rpa])  ; 8. Person 1 being supported prevents person 0 from recruiting person 1. [HO1]
   (defpropn CV-sa->-cv-rpa->cv-na Causal-if [CV-sa->-cv-rpa CV-na])  ; 9. That being supported prevents 1 from being recruited by 0 causes [preserves] 1's innocence. [HO2]
   (defpropn CV-ip Imprison [ob-cpers0])                     ; 10. Person 0 is imprisoned.  [Alternatively is reformed]
   (defpropn CV-ip->-cv-rpa Preventative-if [CV-ip CV-rpa])  ; 11. Person 0 being imprisoned prevents person 0 from recruiting person 1. [HO1]
   (defpropn CV-ip->-cv-rpa->cv-na Causal-if [CV-ip->-cv-rpa  CV-na]) ; 12. That O's imprisonment prevents 0 from recruiting 1 causes [preserves] 1's innocence. [HO2]
  ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; beast propns

(defobj ob-beast)
(defobj ob-bpers)

(def beast-propns
  [
   (defpropn B-pp Human [ob-bpers])                        ; 0. Person is human. [should match cb-np]
   (defpropn B-ab Aggressive [ob-beast])                   ; 1. Beast is agressive.
   (defpropn B-abp Attack [ob-beast ob-bpers])                ; 2. Beast attacks person.
   (defpropn B-ab->b-abp Causal-if [B-ab B-abp])          ; 3. Beast's agressiveness causes it to attack person. [HO1]
   (defpropn B-hrp Harms [ob-bpers])                        ; 4. Person is harmed. [PREVENTING THIS IS GOAL.]
   (defpropn B-abp->b-hrp Causal-if [B-abp B-hrp])          ; 5. Beast attacking human harms person. [HO1]
   (defpropn B-hlb Helps [ob-beast])                        ; 6. Beast is benefited.
   (defpropn B-abp->b-hlb Causal-if [B-abp B-hlb])          ; 7. Beast attacking person benefits beast. [HO1]
   (defpropn B-cpb Capture [ob-bpers ob-beast])               ; 8. Person captures beast.
   (defpropn B-cpb->-b-abp Preventative-if [B-cpb B-abp]) ; 9. Person capturing beast prevents beast attacking person. [HO1]
   (defpropn B-dtp Danger-to [ob-bpers])                   ; 10. Person is subject to danger.
   (defpropn B-cpb->b-dtp Causal-if [B-cpb B-dtp])        ; 11. Person capturing beast is dangerous to person. [HO1]
  ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; containment ("beastly") crime propns

(defobj ob-cpers)
(defobj ob-crim-pers)

(def beastly-crime-propns
  [
   (defpropn CB-np Not-criminal [ob-cpers])                   ; 0. Person is not a crinimal.
   (defpropn CB-ap Aggressive [ob-crim-pers])                 ; 1. Person who's already a criminal is aggressive.
   (defpropn CB-vpp Victimize [ob-crim-pers ob-cpers])           ; 2. Criminal victimizes non-criminal.
   (defpropn CB-ap->cb-vpp Causal-if [CB-ap CB-vpp])          ; 3. Criminal's aggressiveness causes himer to victimize non-criminal. [HO1]
   (defpropn CB-hrp Harms [ob-cpers])                         ; 4. Non-criminal is harmed. [PREVENTING THIS IS GOAL.]
   (defpropn CB-vpp->cb-hrp Causal-if [CB-vpp CB-hrp])        ; 5. Criminal victimizing non-criminal harms non-criminal. [HO1]
   (defpropn CB-hlp Helps [ob-crim-pers])                      ; 6. Criminal is benefited.
   (defpropn CB-vpp->cb-hlp Causal-if [CB-vpp CB-hlp])          ; 7. Criminal victimizing non-criminal benefits criminal. [HO1]
   (defpropn CB-cpc Capture [ob-cpers ob-crim-pers])             ; 8. Non-criminal captures criminal. [notation "cp" has another use]
   (defpropn CB-cpc->-cb-vpp Preventative-if [CB-cpc CB-vpp]) ; 9. Non-criminal capturing criminal prevents criminal from victimizing non-criminal. [HO1]
   (defpropn CB-dtp Danger-to [ob-cpers])                     ; 10. Non-criminal is subject to danger.
   (defpropn CB-cpc->cb-dtp Causal-if [CB-cpc CB-dtp])        ; 11. Non-criminal capturing criminal is dangerous to non-criminal. [HO1]
  ])

;; from the Common Lisp version for popco1
;(defvar semantic-relations
;  '(
;    (similar 'cause 'prevent (* -1L0 *ident-weight*)) ; avoid mapping cause to prevent
;    (semantic-iff 'cb-vpp 'v-ipa -.1L0)
;    (semantic-iff 'cv-rpa 'b-abp -.1L0)
;   ))

(def semantic-iffs [[-0.1 :CB-vpp :V-ipa] [-0.1 :CV-rpa :B-abp]])
;; If this is included in the definition of a person, it will cause there to be links in the proposition network.

(def conceptual-relats [[-1.0 :Causal-if :Preventative-if]])
;; This specifies mapnodes that should be provided with a link from SEMANTIC.
;; Note order of the last two arguments doesn't matter: Both orders are tried.
;; The number will be multiplied by the default semantic link weight of
;; popco.nn.analogy/+sem-similarity-link-value+.  NOTE that the traditional 
;; ACME-based POPCO behavior allows this to happen *only* for mapnodes that
;; are between predicates.  ALSO note that these directives are NOT needed
;; when the two mapped predicates are identical.  In that case, a link of
;; weight +sem-similarity-link-value+ to SEMANTIC is added automatically.
;; Note further: 
;; These have no effect if there is no map node that maps the two predicates.
;; There can only be a map node if there exist propositions using one predicate
;; in one of the major analogues (i.e. "source" and "target", formerly), and one
;; in the other.  Here the major analogues are "worldly" and "spiritual".

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Convenient summary collections:

(def virus-propn-ids (map :id virus-propns)) 
(def beast-propn-ids (map :id beast-propns))
(def viral-crime-propn-ids (map :id viral-crime-propns)) 
(def beastly-crime-propn-ids (map :id beastly-crime-propns))

(def living-propns (concat virus-propns beast-propns))
(def crime-propns (concat viral-crime-propns beastly-crime-propns))
(def all-propns (concat crime-propns living-propns))

(def living-propn-ids (concat virus-propn-ids beast-propn-ids))
(def crime-propn-ids (concat viral-crime-propn-ids beastly-crime-propn-ids))
(def all-propn-ids (concat crime-propn-ids living-propn-ids))

;; PECEPTION-IFS
;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def crime-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) crime-propns))
(def beastly-crime-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) beastly-crime-propns))
(def viral-crime-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) viral-crime-propns))

(def living-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) living-propns))
(def virus-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) virus-propns))
(def beast-perception-ifs (map #(vector 1.0 (:id %) :SALIENT) beast-propns))


;; PROPOSITION NETS (TEMPLATES FOR INDIVIDUAL NETS--i.e. clone from these rather than using them directly)
;; second arg is bidirectional links; third is unidirectional
;; more specific versions can be made in specific model files

(def no-perc-pnet (pn/make-propn-net all-propns semantic-iffs nil)) ; nothing perceived

(def crime-perc-pnet (pn/make-propn-net all-propns semantic-iffs crime-perception-ifs))                 ; crime propns perceived
(def beastly-crime-perc-pnet (pn/make-propn-net all-propns semantic-iffs beastly-crime-perception-ifs)) ; beastly crime propns perceived
(def viral-crime-perc-pnet (pn/make-propn-net all-propns semantic-iffs viral-crime-perception-ifs))     ; viral crime propns perceived

(def living-perc-pnet (pn/make-propn-net all-propns semantic-iffs living-perception-ifs)) ; living propns perceived
(def virus-perc-pnet (pn/make-propn-net all-propns semantic-iffs virus-perception-ifs))   ; virus propns perceived
(def beast-perc-pnet (pn/make-propn-net all-propns semantic-iffs beast-perception-ifs))   ; beast propns perceived

;; STANDARD ANALOGY NET (CAN BE SHARED BY EVERYONE)
(def anet (an/make-analogy-net crime-propns living-propns conceptual-relats))
