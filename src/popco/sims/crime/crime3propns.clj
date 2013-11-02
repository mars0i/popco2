(ns popco.sims.crime.crime3propns
  [:use popco.core.acme]
  [:import [popco.core.acme Propn Obj]])

;; declare all proposition names
(declare V-ip V-na V-ia V-ha V-ia->v-ha V-ipa V-ipa->v-ia V-ica V-ica->-v-ipa V-ica->-v-ipa->v-na V-qp V-qp->-v-ipa V-qp->-v-ipa->v-na
         B-pp B-ab B-abp B-ab->b-abp B-hrp B-abp->b-hrp B-hlb B-abp->b-hlb B-cpb B-cpb->-b-abp B-dtp B-cpb->b-dtp 
         CV-cp CV-na CV-ca CV-ha CV-ca->cv-ha CV-rpa CV-rpa->cv-ca CV-sa CV-sa->-cv-rpa CV-sa->-cv-rpa->cv-na CV-ip CV-ip->-cv-rpa CV-ip->-cv-rpa->cv-na 
         CB-np CB-ap CB-vpp CB-ap->cb-vpp CB-hrp CB-vpp->cb-hrp CB-hlp CB-vpp->cb-hlp CB-cpc CB-cpc->-cb-vpp CB-dtp CB-cpc->cb-dtp)

;; define propositions:
(def V-ip (->Propn :Is-infected [(->Obj :ob-vpers0)] :V-ip))                ; 0. Person 0 has infection.
(def V-na (->Propn :Not-infected [(->Obj :ob-vpers1)] :V-na))               ; 1. Person 1 lacks infection.
(def V-ia (->Propn :Is-infected [(->Obj :ob-vpers1)] :V-ia))                ; 2. Person 1 has infection.
(def V-ha (->Propn :Harms [(->Obj :ob-vpers1)] :V-ha))                      ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
(def V-ia->v-ha (->Propn :Causal-if [V-ia V-ha] :V-ia->v-ha))           ; 4. That person 1 has infection is harmful to person 1. [HO1]
(def V-ipa (->Propn :Infect [(->Obj :ob-vpers0) (->Obj :ob-vpers1)] :V-ipa))             ; 5. Person 0, who already has infection, infects person 1.
(def V-ipa->v-ia (->Propn :Causal-if [V-ipa V-ia] :V-ipa->v-ia))         ; 6. The infecting of person 1 by person 0 causes person 1 to have infection. [HO1]
(def V-ica (->Propn :Inoculate [(->Obj :ob-vpers1)] :V-ica))                 ; 7. Person 1 gets innoculated.
(def V-ica->-v-ipa (->Propn :Preventative-if [V-ica V-ipa] :V-ica->-v-ipa)) ; 8. That person 1 is innoculated prevents person 0 from infecting person 1. [HO1]
(def V-ica->-v-ipa->v-na (->Propn :Causal-if [V-ica->-v-ipa V-na] :V-ica->-v-ipa->v-na))  ; 9. That the innoculating prevents the infecting causes [preserves] person 1 lacking infection. [HO2]
(def V-qp (->Propn :Quarantine [(->Obj :ob-vpers0)] :V-qp))                 ; 10. Person 0 is quarantined.
(def V-qp->-v-ipa (->Propn :Preventative-if [V-qp V-ipa] :V-qp->-v-ipa))  ; 11. That person 0 is quarantined prevents person 0 from infecting person 1.
(def V-qp->-v-ipa->v-na (->Propn :Causal-if [V-qp->-v-ipa V-na] :V-qp->-v-ipa->v-na)) ; 12. That (quarantining 0 prevents 0 from infecting 1) causes [preserves] person 1 lacking infection. [HO2]

(def CV-cp (->Propn :Is-criminal [(->Obj :ob-cpers0)] :CV-cp))                  ; 0. Person 0 is a criminal.
(def CV-na (->Propn :Not-criminal [(->Obj :ob-cpers1)] :CV-na))                 ; 1. Person 1 is not a criminal (or: is innocent).
(def CV-ca (->Propn :Is-criminal [(->Obj :ob-cpers1)] :CV-ca))                  ; 2. Person 1 is a criminal.
(def CV-ha (->Propn :Harms [(->Obj :ob-cpers1)] :CV-ha))                        ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
(def CV-ca->cv-ha (->Propn :Causal-if [CV-ca CV-ha] :CV-ca->cv-ha))           ; 4. Person 1 being a criminal is harmful to person 1.
(def CV-rpa (->Propn :Recruit [(->Obj :ob-cpers0) (->Obj :ob-cpers1)] :CV-rpa))              ; 5. Person 0 recruits person 1 into crime.
(def CV-rpa->cv-ca (->Propn :Causal-if [CV-rpa CV-ca] :CV-rpa->cv-ca))         ; 6. Person 0 recruiting person 1 causes person 1 to become a criminal. [HO1]
(def CV-sa (->Propn :Support [(->Obj :ob-cpers1)] :CV-sa))                      ; 7. Person 1 is [financially, parentally, socially, educationally, etc.] supported.
(def CV-sa->-cv-rpa (->Propn :Preventative-if [CV-sa CV-rpa] :CV-sa->-cv-rpa))  ; 8. Person 1 being supported prevents person 0 from recruiting person 1. [HO1]
(def CV-sa->-cv-rpa->cv-na (->Propn :Causal-if [CV-sa->-cv-rpa CV-na] :CV-sa->-cv-rpa->cv-na))  ; 9. That being supported prevents 1 from being recruited by 0 causes [preserves] 1's innocence. [HO2]
(def CV-ip (->Propn :Imprison [(->Obj :ob-cpers0)] :CV-ip))                     ; 10. Person 0 is imprisoned.  [Alternative: is reformed]
(def CV-ip->-cv-rpa (->Propn :Preventative-if [CV-ip CV-rpa] :CV-ip->-cv-rpa))  ; 11. Person 0 being imprisoned prevents person 0 from recruiting person 1. [HO1]
(def CV-ip->-cv-rpa->cv-na (->Propn :Causal-if [CV-ip->-cv-rpa  CV-na] :CV-ip->-cv-rpa->cv-na)) ; 12. That O's imprisonment prevents 0 from recruiting 1 causes [preserves] 1's innocence. [HO2]

(def B-pp (->Propn :Human [(->Obj :ob-bpers)] :B-pp))                        ; 0. Person is human. [should match cb-np]
(def B-ab (->Propn :Aggressive [(->Obj :ob-beast)] :B-ab))                   ; 1. Beast is agressive.
(def B-abp (->Propn :Attack [(->Obj :ob-beast) (->Obj :ob-bpers)] :B-abp))                ; 2. Beast attacks person.
(def B-ab->b-abp (->Propn :Causal-if [B-ab B-abp] :B-ab->b-abp))          ; 3. Beast's agressiveness causes it to attack person. [HO1]
(def B-hrp (->Propn :Harms [(->Obj :ob-bpers)] :B-hrp))                        ; 4. Person is harmed. [PREVENTING THIS IS GOAL.]
(def B-abp->b-hrp (->Propn :Causal-if [B-abp B-hrp] :B-abp->b-hrp))          ; 5. Beast attacking human harms person. [HO1]
(def B-hlb (->Propn :Helps [(->Obj :ob-beast)] :B-hlb))                        ; 6. Beast is benefited.
(def B-abp->b-hlb (->Propn :Causal-if [B-abp B-hlb] :B-abp->b-hlb))          ; 7. Beast attacking person benefits beast. [HO1]
(def B-cpb (->Propn :Capture [(->Obj :ob-bpers) (->Obj :ob-beast)] :B-cpb))               ; 8. Person captures beast.
(def B-cpb->-b-abp (->Propn :Preventative-if [B-cpb B-abp] :B-cpb->-b-abp)) ; 9. Person capturing beast prevents beast attacking person. [HO1]
(def B-dtp (->Propn :Danger-to [(->Obj :ob-bpers)] :B-dtp))                   ; 10. Person is subject to danger.
(def B-cpb->b-dtp (->Propn :Causal-if [B-cpb B-dtp] :B-cpb->b-dtp))        ; 11. Person capturing beast is dangerous to person. [HO1]
 
(def CB-np (->Propn :Not-criminal [(->Obj :ob-cpers)] :CB-np))                   ; 0. Person is not a crinimal.
(def CB-ap (->Propn :Aggressive [(->Obj :ob-crim-pers)] :CB-ap))                 ; 1. Person who's already a criminal is aggressive.
(def CB-vpp (->Propn :Victimize [(->Obj :ob-crim-pers) (->Obj :ob-cpers)] :CB-vpp))           ; 2. Criminal victimizes non-criminal.
(def CB-ap->cb-vpp (->Propn :Causal-if [CB-ap CB-vpp] :CB-ap->cb-vpp))          ; 3. Criminal's aggressiveness causes himer to victimize non-criminal. [HO1]
(def CB-hrp (->Propn :Harms [(->Obj :ob-cpers)] :CB-hrp))                         ; 4. Non-criminal is harmed. [PREVENTING THIS IS GOAL.]
(def CB-vpp->cb-hrp (->Propn :Causal-if [CB-vpp CB-hrp] :CB-vpp->cb-hrp))        ; 5. Criminal victimizing non-criminal harms non-criminal. [HO1]
(def CB-hlp (->Propn :Helps [(->Obj :ob-crim-pers)] :CB-hlp))                      ; 6. Criminal is benefited.
(def CB-vpp->cb-hlp (->Propn :Causal-if [CB-vpp CB-hlp] :CB-vpp->cb-hlp))          ; 7. Criminal victimizing non-criminal benefits criminal. [HO1]
(def CB-cpc (->Propn :Capture [(->Obj :ob-cpers) (->Obj :ob-crim-pers)] :CB-cpc))             ; 8. Non-criminal captures criminal. [notation "cp" has another use]
(def CB-cpc->-cb-vpp (->Propn :Preventative-if [CB-cpc CB-vpp] :CB-cpc->-cb-vpp)) ; 9. Non-criminal capturing criminal prevents criminal from victimizing non-criminal. [HO1]
(def CB-dtp (->Propn :Danger-to [(->Obj :ob-cpers)] :CB-dtp))                     ; 10. Non-criminal is subject to danger.
(def CB-cpc->cb-dtp (->Propn :Causal-if [CB-cpc CB-dtp] :CB-cpc->cb-dtp))        ; 11. Non-criminal capturing criminal is dangerous to non-criminal. [HO1]

(def virus-propns
  [
   V-ip                ; 0. Person 0 has infection.
   V-na               ; 1. Person 1 lacks infection.
   V-ia                ; 2. Person 1 has infection.
   V-ha                      ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
   V-ia->v-ha           ; 4. That person 1 has infection is harmful to person 1. [HO1]
   V-ipa             ; 5. Person 0, who already has infection, infects person 1.
   V-ipa->v-ia         ; 6. The infecting of person 1 by person 0 causes person 1 to have infection. [HO1]
   V-ica                 ; 7. Person 1 gets innoculated.
   V-ica->-v-ipa ; 8. That person 1 is innoculated prevents person 0 from infecting person 1. [HO1]
   V-ica->-v-ipa->v-na  ; 9. That the innoculating prevents the infecting causes [preserves] person 1 lacking infection. [HO2]
   V-qp                 ; 10. Person 0 is quarantined.
   V-qp->-v-ipa  ; 11. That person 0 is quarantined prevents person 0 from infecting person 1.
   V-qp->-v-ipa->v-na ; 12. That (quarantining 0 prevents 0 from infecting 1) causes [preserves] person 1 lacking infection. [HO2]
   ])

(def viral-crime-propns
  [
   CV-cp                  ; 0. Person 0 is a criminal.
   CV-na                 ; 1. Person 1 is not a criminal (or: is innocent).
   CV-ca                  ; 2. Person 1 is a criminal.
   CV-ha                        ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
   CV-ca->cv-ha           ; 4. Person 1 being a criminal is harmful to person 1.
   CV-rpa              ; 5. Person 0 recruits person 1 into crime.
   CV-rpa->cv-ca         ; 6. Person 0 recruiting person 1 causes person 1 to become a criminal. [HO1]
   CV-sa                      ; 7. Person 1 is [financially, parentally, socially, educationally, etc.] supported.
   CV-sa->-cv-rpa  ; 8. Person 1 being supported prevents person 0 from recruiting person 1. [HO1]
   CV-sa->-cv-rpa->cv-na  ; 9. That being supported prevents 1 from being recruited by 0 causes [preserves] 1's innocence. [HO2]
   CV-ip                     ; 10. Person 0 is imprisoned.  [Alternative: is reformed]
   CV-ip->-cv-rpa  ; 11. Person 0 being imprisoned prevents person 0 from recruiting person 1. [HO1]
   CV-ip->-cv-rpa->cv-na ; 12. That O's imprisonment prevents 0 from recruiting 1 causes [preserves] 1's innocence. [HO2]
   ])

(def beast-propns
  [
   B-pp                        ; 0. Person is human. [should match cb-np]
   B-ab                   ; 1. Beast is agressive.
   B-abp                ; 2. Beast attacks person.
   B-ab->b-abp          ; 3. Beast's agressiveness causes it to attack person. [HO1]
   B-hrp                        ; 4. Person is harmed. [PREVENTING THIS IS GOAL.]
   B-abp->b-hrp          ; 5. Beast attacking human harms person. [HO1]
   B-hlb                        ; 6. Beast is benefited.
   B-abp->b-hlb          ; 7. Beast attacking person benefits beast. [HO1]
   B-cpb               ; 8. Person captures beast.
   B-cpb->-b-abp ; 9. Person capturing beast prevents beast attacking person. [HO1]
   B-dtp                   ; 10. Person is subject to danger.
   B-cpb->b-dtp        ; 11. Person capturing beast is dangerous to person. [HO1]
   ])

(def beastly-crime-propns
  [
   CB-np                   ; 0. Person is not a crinimal.
   CB-ap                 ; 1. Person who's already a criminal is aggressive.
   CB-vpp           ; 2. Criminal victimizes non-criminal.
   CB-ap->cb-vpp          ; 3. Criminal's aggressiveness causes himer to victimize non-criminal. [HO1]
   CB-hrp                         ; 4. Non-criminal is harmed. [PREVENTING THIS IS GOAL.]
   CB-vpp->cb-hrp        ; 5. Criminal victimizing non-criminal harms non-criminal. [HO1]
   CB-hlp                      ; 6. Criminal is benefited.
   CB-vpp->cb-hlp          ; 7. Criminal victimizing non-criminal benefits criminal. [HO1]
   CB-cpc             ; 8. Non-criminal captures criminal. [notation "cp" has another use]
   CB-cpc->-cb-vpp ; 9. Non-criminal capturing criminal prevents criminal from victimizing non-criminal. [HO1]
   CB-dtp                     ; 10. Non-criminal is subject to danger.
   CB-cpc->cb-dtp        ; 11. Non-criminal capturing criminal is dangerous to non-criminal. [HO1]
   ])

(def living-propns (concat virus-propns beast-propns))
(def crime-propns (concat viral-crime-propns beastly-crime-propns))
(def all-propns (concat living-propns crime-propns))
