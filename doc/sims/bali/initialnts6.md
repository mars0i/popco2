initialnts6.md
====

	(def conceptual-relats 
	  [[-1.0 :Causal-if :Preventative-if]
	   [-1.0 :Is-ordered :Is-disordered]
	   [-1.0 :Succeeds :Fails]])

Objects:

	Is-king [:king]

	Is-peasant [:peasant1]
	Is-peasant [:peasant2]
	Is-peasant [:peasant3]
	Is-peasant [:peasant4]

	Is-subak [:subak1]
	Is-subak [:subak2]

	Is-bhutakala [:rat]
	Is-bhutakala [:insect]
	Is-bhutakala [:demon]
	Is-bhutakala [:foes]
	Is-bhutakala [:dutch]
	Is-bhutakala [:jealousy]
	Is-bhutakala [:greed]

	Is-negara [:state]
	Is-world [:field] ; i.e. the natural world

	Is-ordered [:state]
	Is-disordered [:state] :B-state-disordered

	Is-ordered [:field]
	Is-disordered [:field]

	
Brahmanic:

	Subject-of [:king :ob-peasant1] :B-subject-peas1
	Subject-of [:king :ob-peasant2] :B-subject-peas2
	Subject-of [:king :ob-peasant3] :B-subject-peas3
	Subject-of [:king :ob-peasant4] :B-subject-peas4

	Struggles-against [:king :demon] :B-king-against-demon
	Struggles-against [:king :dutch] :B-king-against-dutch
	Struggles-against [:king :foes]  :B-king-against-foes

        Fails [:B-king-against-demon] :B-fails-against-demon
        Fails [:B-king-against-dutch] :B-fails-against-dutch
        Fails [:B-king-against-foes]  :B-fails-against-foes

	:Causal-if [:B-fails-against-demon :B-state-disordered]
	:Causal-if [:B-fails-against-dutch :B-state-disordered]
	:Causal-if [:B-fails-against-foes :B-state-disordered]
	

Peasant:

	Member-of [:peasant1 :ob-subak1]
	Member-of [:peasant2 :ob-subak1]

	Member-of [:peasant3 :ob-subak2]
	Member-of [:peasant4 :ob-subak2]

	Struggles-against [:subak1 :demon]
	Struggles-against [:subak1 :rat]
	Struggles-against [:subak1 :insect]
	Struggles-against [:subak1 :jealousy]
	Struggles-against [:subak1 :greed]

	Struggles-against [:subak2 :demon]
	Struggles-against [:subak2 :rat]
	Struggles-against [:subak2 :insect]
	Struggles-against [:subak2 :jealousy]
	Struggles-against [:subak2 :greed]

	[do something similar to above causal-ifs here]
