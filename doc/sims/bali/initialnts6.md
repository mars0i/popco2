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
	:Is-bhutakala [:insect]
	:Is-bhutakala [:demon]
	:Is-bhutakala [:foes]
	:Is-bhutakala [:dutch]
	:Is-bhutakala [:envy]
	:Is-bhutakala [:greed]

	:Is-negara [:state]
	:Is-world [:field] ; i.e. the natural world

	
Brahmanic:

	:Is-ordered [:state]    :B-state-ordered
	:Is-disordered [:state] :B-state-disordered

	:Subject-of [:king :ob-peasant1] :B-subject-peas1
	:Subject-of [:king :ob-peasant2] :B-subject-peas2
	:Subject-of [:king :ob-peasant3] :B-subject-peas3
	:Subject-of [:king :ob-peasant4] :B-subject-peas4

	:Decides-alone [:king] :B-king-autocrat

	:Struggles-against [:king :demon] :B-king-against-demon
	:Struggles-against [:king :dutch] :B-king-against-dutch
	:Struggles-against [:king :foes]  :B-king-against-foes

	:Succeeds [:B-king-against-demon] :B-succeeds-against-demon
	:Succeeds [:B-king-against-dutch] :B-succeeds-against-dutch
	:Succeeds [:B-king-against-foes]  :B-succeeds-against-foes

	:Fails [:B-king-against-demon] :B-fails-against-demon
	:Fails [:B-king-against-dutch] :B-fails-against-dutch
	:Fails [:B-king-against-foes]  :B-fails-against-foes

	:Causal-if [:B-fails-against-demon :B-state-disordered] :B-fail-demon-disorder
	:Causal-if [:B-fails-against-dutch :B-state-disordered] :B-fail-dutch-disorder
	:Causal-if [:B-fails-against-foes :B-state-disordered]  :B-fail-foes-disorder

	:Persists [:state] :B-state-persists
	:Ceases [:state] :B-state-ceases

	:Causal-if [:B-state-ordered :B-state-persists]

	;; If the state becomes disordered, it ceases to exist:
	:Causal-if [:B-state-disordered :B-state-ceases]
	

Peasant:

	:Is-ordered [:subak1]   :B-subak1-ordered
	:Is-ordered [:subak2]   :B-subak2-ordered
	:Is-disordered [:subak1] :B-subak1-disordered
	:Is-disordered [:subak2] :B-subak2-disordered

	:Is-ordered [:field]
	:Is-disordered [:field]

	:Member-of [:peasant1a :ob-subak1]
	:Member-of [:peasant1b :ob-subak1]

	:Member-of [:peasant2a :ob-subak2]
	:Member-of [:peasant2b :ob-subak2]

	:Decides-with [:peasant1a :peasant1a] :P-subak1-democracy
	:Decides-with [:peasant2a :peasant2a] :P-subak2-democracy

	:Struggles-against [:subak1 :demon]   :P-subak1-against-demon
	:Struggles-against [:subak1 :rat]     :P-subak1-against-rat
	:Struggles-against [:subak1 :insect]  :P-subak1-against-insect
	:Struggles-against [:subak1 :envy]    :P-subak1-against-envy
	:Struggles-against [:subak1 :greed]   :P-subak1-against-greed

	:Struggles-against [:subak1 :demon]   :P-subak1-against-demon
	:Struggles-against [:subak1 :rat]     :P-subak1-against-rat
	:Struggles-against [:subak1 :insect]  :P-subak1-against-insect
	:Struggles-against [:subak1 :envy]    :P-subak1-against-envy
	:Struggles-against [:subak1 :greed]   :P-subak1-against-greed

	;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	;; NEED PROPNS HERE ON SUCCESS, FAILURE, ORDER, DISORDER
	;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

	:Persists [:subak1] :B-subak1-persists
	:Persists [:subak2] :B-subak2-persists

	:Causal-if [:B-subak1-ordered :B-subak1-persists]

	;; If a subak becomes disordered, it nevertheless persists:
	:Causal-if [:B-subak1-disordered :B-subak1-persists]

	:Causal-if [:B-subak2-ordered :B-subak2-persists]
	:Causal-if [:B-subak2-disordered :B-subak2-persists]
