(ns popco.sims.crime.crime3propns
  [:use popco.core.acme]
  [:import [popco.core.acme Propn Obj]])

(def virus-propns
  [
    (->Propn :Is-infected [:ob-vpers0] :V-ip)                ; 0. Person 0 has infection.
    (->Propn :Not-infected [:ob-vpers1] :V-na)               ; 1. Person 1 lacks infection.
    (->Propn :Is-infected [:ob-vpers1] :V-ia)                ; 2. Person 1 has infection.
    (->Propn :Harms [:ob-vpers1] :V-ha)                      ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
    (->Propn :Causal-if [:V-ia :V-ha] :V-ia->v-ha)           ; 4. That person 1 has infection is harmful to person 1. [HO1]
    (->Propn :Infect [:ob-vpers0 :ob-vpers1] :V-ipa)             ; 5. Person 0, who already has infection, infects person 1.
    (->Propn :Causal-if [:V-ipa :V-ia] :V-ipa->v-ia)         ; 6. The infecting of person 1 by person 0 causes person 1 to have infection. [HO1]
    (->Propn :Inoculate [:ob-vpers1] :V-ica)                 ; 7. Person 1 gets innoculated.
    (->Propn :Preventative-if [:V-ica :V-ipa] :V-ica->-v-ipa) ; 8. That person 1 is innoculated prevents person 0 from infecting person 1. [HO1]
    (->Propn :Causal-if [:V-ica->-v-ipa :V-na] :V-ica->-v-ipa->v-na)  ; 9. That the innoculating prevents the infecting causes [preserves] person 1 lacking infection. [HO2]
    (->Propn :Quarantine [:ob-vpers0] :V-qp)                 ; 10. Person 0 is quarantined.
    (->Propn :Preventative-if [:V-qp :V-ipa] :V-qp->-v-ipa)  ; 11. That person 0 is quarantined prevents person 0 from infecting person 1.
    (->Propn :Causal-if [:V-qp->-v-ipa :V-na] :V-qp->-v-ipa->v-na) ; 12. That (quarantining 0 prevents 0 from infecting 1) causes [preserves] person 1 lacking infection. [HO2]
   ])

(def viral-crime-propns
  [
    (->Propn :Is-criminal [:ob-cpers0] :CV-cp)                  ; 0. Person 0 is a criminal.
    (->Propn :Not-criminal [:ob-cpers1] :CV-na)                 ; 1. Person 1 is not a criminal (or: is innocent).
    (->Propn :Is-criminal [:ob-cpers1] :CV-ca)                  ; 2. Person 1 is a criminal.
    (->Propn :Harms [:ob-cpers1] :CV-ha)                        ; 3. Person 1 is harmed. [PREVENTING THIS IS GOAL.]
    (->Propn :Causal-if [:CV-ca :CV-ha] :CV-ca->cv-ha)           ; 4. Person 1 being a criminal is harmful to person 1.
    (->Propn :Recruit [:ob-cpers0 :ob-cpers1] :CV-rpa)              ; 5. Person 0 recruits person 1 into crime.
    (->Propn :Causal-if [:CV-rpa :CV-ca] :CV-rpa->cv-ca)         ; 6. Person 0 recruiting person 1 causes person 1 to become a criminal. [HO1]
    (->Propn :Support [:ob-cpers1] :CV-sa)                      ; 7. Person 1 is [financially, parentally, socially, educationally, etc.] supported.
    (->Propn :Preventative-if [:CV-sa :CV-rpa] :CV-sa->-cv-rpa)  ; 8. Person 1 being supported prevents person 0 from recruiting person 1. [HO1]
    (->Propn :Causal-if [:CV-sa->-cv-rpa :CV-na] :CV-sa->-cv-rpa->cv-na)  ; 9. That being supported prevents 1 from being recruited by 0 causes [preserves] 1's innocence. [HO2]
    (->Propn :Imprison [:ob-cpers0] :CV-ip)                     ; 10. Person 0 is imprisoned.  [Alternative: is reformed]
    (->Propn :Preventative-if [:CV-ip :CV-rpa] :CV-ip->-cv-rpa)  ; 11. Person 0 being imprisoned prevents person 0 from recruiting person 1. [HO1]
    (->Propn :Causal-if [:CV-ip->-cv-rpa  :CV-na] :CV-ip->-cv-rpa->cv-na) ; 12. That O's imprisonment prevents 0 from recruiting 1 causes [preserves] 1's innocence. [HO2]
   ])

(def beast-propns
  [
    (->Propn :Human [:ob-bpers] :B-pp)                        ; 0. Person is human. [should match cb-np]
    (->Propn :Aggressive [:ob-beast] :B-ab)                   ; 1. Beast is agressive.
    (->Propn :Attack [:ob-beast :ob-bpers] :B-abp)                ; 2. Beast attacks person.
    (->Propn :Causal-if [:B-ab :B-abp] :B-ab->b-abp)          ; 3. Beast's agressiveness causes it to attack person. [HO1]
    (->Propn :Harms [:ob-bpers] :B-hrp)                        ; 4. Person is harmed. [PREVENTING THIS IS GOAL.]
    (->Propn :Causal-if [:B-abp :B-hrp] :B-abp->b-hrp)          ; 5. Beast attacking human harms person. [HO1]
    (->Propn :Helps [:ob-beast] :B-hlb)                        ; 6. Beast is benefited.
    (->Propn :Causal-if [:B-abp :B-hlb] :B-abp->b-hlb)          ; 7. Beast attacking person benefits beast. [HO1]
    (->Propn :Capture [:ob-bpers :ob-beast] :B-cpb)               ; 8. Person captures beast.
    (->Propn :Preventative-if [:B-cpb :B-abp] :B-cpb->-b-abp) ; 9. Person capturing beast prevents beast attacking person. [HO1]
    (->Propn :Danger-to [:ob-bpers] :B-dtp)                   ; 10. Person is subject to danger.
    (->Propn :Causal-if [:B-cpb :B-dtp] :B-cpb->b-dtp)        ; 11. Person capturing beast is dangerous to person. [HO1]
   ])

(def beastly-crime-propns
  [
    (->Propn :Not-criminal [:ob-cpers] :CB-np)                   ; 0. Person is not a crinimal.
    (->Propn :Aggressive [:ob-crim-pers] :CB-ap)                 ; 1. Person who's already a criminal is aggressive.
    (->Propn :Victimize [:ob-crim-pers :ob-cpers] :CB-vpp)           ; 2. Criminal victimizes non-criminal.
    (->Propn :Causal-if [:CB-ap :CB-vpp] :CB-ap->cb-vpp)          ; 3. Criminal's aggressiveness causes himer to victimize non-criminal. [HO1]
    (->Propn :Harms [:ob-cpers] :CB-hrp)                         ; 4. Non-criminal is harmed. [PREVENTING THIS IS GOAL.]
    (->Propn :Causal-if [:CB-vpp :CB-hrp] :CB-vpp->cb-hrp)        ; 5. Criminal victimizing non-criminal harms non-criminal. [HO1]
    (->Propn :Helps [:ob-crim-pers] :CB-hlp)                      ; 6. Criminal is benefited.
    (->Propn :Causal-if [:CB-vpp :CB-hlp] :CB-vpp->cb-hlp)          ; 7. Criminal victimizing non-criminal benefits criminal. [HO1]
    (->Propn :Capture [:ob-cpers :ob-crim-pers] :CB-cpc)             ; 8. Non-criminal captures criminal. [notation "cp" has another use]
    (->Propn :Preventative-if [:CB-cpc :CB-vpp] :CB-cpc->-cb-vpp) ; 9. Non-criminal capturing criminal prevents criminal from victimizing non-criminal. [HO1]
    (->Propn :Danger-to [:ob-cpers] :CB-dtp)                     ; 10. Non-criminal is subject to danger.
    (->Propn :Causal-if [:CB-cpc :CB-dtp] :CB-cpc->cb-dtp)        ; 11. Non-criminal capturing criminal is dangerous to non-criminal. [HO1]
   ])

(def living-propns (concat virus-propns beast-propns))
(def crime-propns (concat viral-crime-propns beastly-crime-propns))
(def all-propns (concat living-propns crime-propns))
