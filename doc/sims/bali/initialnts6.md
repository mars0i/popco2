initialnts6.md
====

	(def conceptual-relats 
	  [[-1.0 :Causal-if :Preventative-if]
	   [-1.0 :Is-ordered :Is-disordered]
	   [-0.9 :Subject-of :Member-of] ; probably not needed
	   [-1.0 :Persists :Ceases]
	   [-1.0 :Succeeds :Fails]])

Objects:

	:Is-king [:king]

	:Is-peasant [:peasant1]
	:Is-peasant [:peasant2]
	:Is-peasant [:peasant3]
	:Is-peasant [:peasant4]

	:Is-subak [:subak1]
	:Is-subak [:subak2]

	:Is-bhutakala [:rat]
	; :Is-bhutakala [:insect]
	:Is-bhutakala [:demon]
	:Is-bhutakala [:dutch]
	;:Is-bhutakala [:foes]
	:Is-bhutakala [:envy]
	; :Is-bhutakala [:greed]

	:Is-negara [:state]
	:Is-world [:field] ; i.e. the natural world

	
Brahmanic:

	:Is-ordered [:state]    :B-state-ordered
	:Is-disordered [:state] :B-state-disordered

	:Subject-of [:king :peasant1] :B-subject-peas1
	:Subject-of [:king :peasant2] :B-subject-peas2
	:Subject-of [:king :peasant3] :B-subject-peas3
	:Subject-of [:king :peasant4] :B-subject-peas4

	:Decides-alone [:king] :B-king-autocrat

	;;;;;;;;;;;;;;;;;;;;;;;;;;
	;; POSSIBLY ADD PROPOSITIONS WITH s/dutch/foes/g, BELOW etc.
	;;;;;;;;;;;;;;;;;;;;;;;;;;

	:Struggles-against [:king :demon] :B-king-against-demon
	:Struggles-against [:king :dutch] :B-king-against-dutch

	:Succeeds [:B-king-against-demon] :B-succeeds-against-demon
	:Succeeds [:B-king-against-dutch] :B-succeeds-against-dutch

	:Causal-if [:B-succeeds-against-demon :B-state-ordered] :B-succeed-demon-order
	:Causal-if [:B-succeeds-against-dutch :B-state-ordered] :B-succeed-dutch-order

	:Fails [:B-king-against-demon] :B-fails-against-demon
	:Fails [:B-king-against-dutch] :B-fails-against-dutch

	:Causal-if [:B-fails-against-demon :B-state-disordered] :B-fail-demon-disorder
	:Causal-if [:B-fails-against-dutch :B-state-disordered] :B-fail-dutch-disorder

	:Persists [:state] :B-state-persists
	:Ceases [:state] :B-state-ceases

	:Causal-if [:B-state-ordered :B-state-persists]

	;; If the state becomes disordered, it ceases to exist:
	:Causal-if [:B-state-disordered :B-state-ceases]
	;; THIS IS A POINT AT WHICH THE PEASANT ANALOGIES WILL DIFFER.
	

Peasant:

	:Is-ordered [:subak1]   :P-subak1-ordered
	:Is-ordered [:subak2]   :P-subak2-ordered
	:Is-disordered [:subak1] :P-subak1-disordered
	:Is-disordered [:subak2] :P-subak2-disordered

	:Is-ordered [:field]
	:Is-disordered [:field]

	:Member-of [:peasant1a :subak1]
	:Member-of [:peasant1b :subak1]

	:Member-of [:peasant2a :subak2]
	:Member-of [:peasant2b :subak2]

	:Decide-together [:peasant1a :peasant1b] :P-subak1-democracy
	:Decide-together [:peasant2a :peasant2b] :P-subak2-democracy

	;;;;;;;;;;;;;;;;;;
	;; TODO:
	;; QUESTION: HOW CAN DEMOCRACY VS AUTOCRACY BE CONNECTED TO THE REST OF THE PROCESS?
	;; MAYBE INSTEAD OF Decide- PREDICATES, MERGE THIS IDEA INTO THE Struggle- PREDICATES,
	;; CAPTURING INDIVIDUAL VS COLLECTIVE STRUGGLE.  (?)
	;;;;;;;;;;;;;;;;;;

	;;;;;;;;;;;;;;;;;;;;;;;;;;
	;; POSSIBLY ADD PROPOSITIONS WITH s/rat/insect/g, s/envy/greed/g BELOW etc.
	;;;;;;;;;;;;;;;;;;;;;;;;;;

	:Struggles-against [:subak1 :demon]   :P-subak1-against-demon
	:Struggles-against [:subak1 :rat]     :P-subak1-against-rat
	:Struggles-against [:subak1 :envy]    :P-subak1-against-envy
	:Struggles-against [:subak2 :demon]   :P-subak2-against-demon
	:Struggles-against [:subak2 :rat]     :P-subak2-against-rat
	:Struggles-against [:subak2 :envy]    :P-subak2-against-envy

	:Succeeds [:P-subak1-against-demon] :P-subak1-succeeds-against-demon
	:Succeeds [:P-subak1-against-rat]   :P-subak1-succeeds-against-rat
	:Succeeds [:P-subak1-against-envy]  :P-subak1-succeeds-against-envy
	:Succeeds [:P-subak2-against-demon] :P-subak2-succeeds-against-demon
	:Succeeds [:P-subak2-against-rat]   :P-subak2-succeeds-against-rat
	:Succeeds [:P-subak2-against-envy]  :P-subak2-succeeds-against-envy

	:Fails [:P-subak1-against-demon] :P-subak1-fails-against-demon
	:Fails [:P-subak1-against-rat]   :P-subak1-fails-against-rat
	:Fails [:P-subak1-against-envy]  :P-subak1-fails-against-envy
	:Fails [:P-subak2-against-demon] :P-subak2-fails-against-demon
	:Fails [:P-subak2-against-rat]   :P-subak2-fails-against-rat
	:Fails [:P-subak2-against-envy]  :P-subak2-fails-against-envy

	:Causal-if [:P-subak1-fails-against-demon :P-subak1-subak1-disordered] :P-subak1-fail-demon-disorder
	:Causal-if [:P-subak1-fails-against-rat   :P-subak1-subak1-disordered] :P-subak1-fail-rat-disorder
	:Causal-if [:P-subak1-fails-against-envy  :P-subak1-subak1-disordered] :P-subak1-fail-envy-disorder
	:Causal-if [:P-subak2-fails-against-demon :P-subak2-subak2-disordered] :P-subak2-fail-demon-disorder
	:Causal-if [:P-subak2-fails-against-rat   :P-subak2-subak2-disordered] :P-subak2-fail-rat-disorder
	:Causal-if [:P-subak2-fails-against-envy  :P-subak2-subak2-disordered] :P-subak2-fail-envy-disorder

	:Persists [:subak1] :B-subak1-persists
	:Persists [:subak2] :B-subak2-persists
	:Ceases [:subak1] :B-subak1-ceases  ; should always receive negative activation
	:Ceases [:subak2] :B-subak2-ceases  ; should always receive negative activation

	;; HERE there is a big difference with the Brahmanic system:
	;; IF A SUBAK BECOMES DISORDERED, IT NEVERTHELESS PERSISTS:
	:Causal-if [:B-subak1-ordered :B-subak1-persists]
	:Causal-if [:B-subak1-disordered :B-subak1-persists]
	:Causal-if [:B-subak2-ordered :B-subak2-persists]
	:Causal-if [:B-subak2-disordered :B-subak2-persists]
