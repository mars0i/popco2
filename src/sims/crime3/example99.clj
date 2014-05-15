(ns sims.crime3.example99
  (:require [popco.nn.analogy :as an]
            [popco.nn.propn :as pn]
            [popco.core.person :as pers]
            [popco.core.population :as pp]
            [popco.core.constants :as cn]
            [sims.crime3.propns :as pns]))

(def propns (concat pns/crime-propns pns/living-propns)) 

;; Directional activation flows from j to i, i.e. here from salient to the crime propn node
(def perception-ifs (map #(vector cn/+one+ (:id %) :SALIENT) pns/crime-propns))

(def pnet (pn/make-propn-net propns pns/semantic-iffs perception-ifs)) ; second arg is bidirectional link, third is unidirectional

(def anet (an/make-analogy-net pns/crime-propns 
                               pns/living-propns 
                               pns/conceptual-relats))

(def jo (pers/make-person :jo 
                          (concat pns/crime-propns pns/living-propns) pnet anet))

(def job (pers/make-person :job 
                           (concat pns/crime-propns pns/beast-propns) pnet anet))

(def jov (pers/make-person :jov 
                           (concat pns/crime-propns pns/virus-propns) pnet anet))


(def popn (pp/->Population 0 
                           (vec
                             (concat
                               [jo job jov]
                               (repeatedly 32 #(pers/new-person-from-old jo))
                               (repeatedly 32 #(pers/new-person-from-old job))
                               (repeatedly 32 #(pers/new-person-from-old jov))))))

;(reset! mn/folks popn)
;(swap! mn/folks assoc :persons [jo job jov])

;(mn/init popn) ; note popn is unchanged, but @folks has been updated.

;(def popn-evol (mn/many-times popn))
;(def popn-evol (mn/popco popn))


;; POPULATIONS FOR TESTING HYPOTHESIS THAT pmap BREAKS SEQUENCES INTO 32-ELEMENT CHUNKS,
;; so that up to the level of available cores, map/pmap speed will be equal to the
;; number of chunks.
;; On MBP:
;; The hypothesis seems to hold only for 2 or 3 chunks.  The overall speed using
;; pmap with more chunks is not the same; there appears to be more overhead from
;; combining the chunks, or other parts of the program.  So while the
;; map version scales linearly with the number of chunks, the pmap version increases,
;; but at a slower rate.  The result is that the ratio is less than the number
;; of chunks for larger numbers of chunks.

; (def popn2chunk (pp/->Population 0 
;                            (vec
;                              (concat
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))))))
; 
; (def popn3chunk (pp/->Population 0 
;                            (vec
;                              (concat
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))))))
; 
; (def popn4chunk (pp/->Population 0 
;                            (vec
;                              (concat
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))))))
; 
; (def popn5chunk (pp/->Population 0 
;                            (vec
;                              (concat
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))))))
; 
; (def popn6chunk (pp/->Population 0 
;                            (vec
;                              (concat
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))
;                                (repeatedly 32 #(pers/new-person-from-old jov))))))
; 
; ;; More than the 8 cores on my MBP:
; (def popn12chunk (pp/->Population 0 
;                            (vec
;                              (concat
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))
;                                (repeatedly 32 #(pers/new-person-from-old jov))
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old jo))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old job))
;                                (repeatedly 32 #(pers/new-person-from-old jov))
;                                (repeatedly 32 #(pers/new-person-from-old jov))))))
