initialnts8.md
====

## Revision of initialnts8.md: Split into source and target analogues.

There are two analogue realms, each with two analogues in it, as in the
Sanday and crime analogy systems.

Let's say that there are "spiritual" analogues and the "worldly"
analogues (although that distorts Balinese thinking, I suppose).

There are two kinds of spiritual analogues: Brahmanic/negara/state and
peasant/subak.

There are two kinds of worldy analogues: Those that concern rice
farming, water use, and local pest infestations; and those that concern
events that befall negaras (states): Attacks by enemies, large-scale
natural disasters, rodent plagues, etc.

So we have:

<table>
<tr align="center"><td>spiritual</td><td> worldly</td></tr>
<tr>
  <td><table><tr><td>brahmanic</td><td>peasant</td></tr></table></td>
  <td><table><tr><td>negara</td><td>rice</td></tr></table></td>
</tr>
</table>
</table>


### Explicit semantic relationships:

	(def conceptual-relats 
	  [[-1.0 :Causal-if :Preventative-if]
	   [-1.0 :Is-ordered :Is-disordered]
	   [-0.9 :Subject-of :Member-of] ; probably not needed
	   [-1.0 :Persists :Ceases]
	   [-1.0 :Succeeds :Fails]])

### Objects:

	;; POSSIBLY ADD PROPOSITIONS WITH s/rat/insect/g, s/envy/greed/g, s/enemy/foes/g BELOW etc.
	;; Also for Brahmans, maybe natural disasters--earthquakes, storms, etc.  Even plagues of rodents.

##### both spiritual and worldly:

	:Is-king [:king]

	:Is-peasant [:peasant1]
	:Is-peasant [:peasant2]

	:Is-subak [:subak]

	:Is-bhutakala [:rat]
	:Is-bhutakala [:demon]
	:Is-bhutakala [:enemy]
	:Is-bhutakala [:envy]

	:Is-negara [:state]
	:Is-world [:field] ; i.e. the natural world

	
#### Brahmanic:

	:Is-ordered [:state]    :B-state-ordered
	:Is-disordered [:state] :B-state-disordered

	:Subject-of [:king :peasant1] :B-subject-peas1
	:Subject-of [:king :peasant2] :B-subject-peas2

	:Decides-alone [:king] :B-king-autocrat

	;;;;;;;;;;;;;;;;;;;;;;;;;;
	;; POSSIBLY ADD PROPOSITIONS WITH s/enemy/foes/g, BELOW etc.
	;; Also maybe natural disasters--earthquakes, storms, etc.  Even plagues of rodents.
	;;;;;;;;;;;;;;;;;;;;;;;;;;

	:Struggles-against [:king :demon] :B-king-against-demon
	:Struggles-against [:king :enemy] :B-king-against-enemy

	:Succeeds [:B-king-against-demon] :B-succeeds-against-demon
	:Succeeds [:B-king-against-enemy] :B-succeeds-against-enemy

	:Causal-if [:B-succeeds-against-demon :B-state-ordered] :B-succeed-demon-order
	:Causal-if [:B-succeeds-against-enemy :B-state-ordered] :B-succeed-enemy-order

	:Fails [:B-king-against-demon] :B-fails-against-demon
	:Fails [:B-king-against-enemy] :B-fails-against-enemy

	:Causal-if [:B-fails-against-demon :B-state-disordered] :B-state-fail-demon->disorder
	:Causal-if [:B-fails-against-enemy :B-state-disordered] :B-state-fail-enemy->disorder

	:Persists [:state] :B-state-persists
	:Ceases [:state] :B-state-ceases

	:Causal-if [:B-state-ordered :B-state-persists]

	;; If the state becomes disordered, it ceases to exist:
	:Causal-if [:B-state-disordered :B-state-ceases]
	;; THIS IS A POINT AT WHICH THE PEASANT ANALOGIES WILL DIFFER.
	

#### Subak:

	:Is-ordered [:subak]   :P-subak-ordered
	:Is-disordered [:subak] :P-subak-disordered

	;;;;;;;;;;;;;;;;;;;;;;;;;;
	;; QUESTION: CONNECT THESE WITH OTHER ORDER? OR DELETE.
	;;;;;;;;;;;;;;;;;;;;;;;;;;;;
	:Is-ordered [:field]
	:Is-disordered [:field]

	:Member-of [:peasant1 :subak]
	:Member-of [:peasant2 :subak]

	;; :Decide-together [:peasant1 :peasant2] :P-subak-democracy

	;;;;;;;;;;;;;;;;;;;;;;;;;;
	;; POSSIBLY ADD PROPOSITIONS WITH s/rat/insect/g, s/envy/greed/g BELOW etc.
	;;;;;;;;;;;;;;;;;;;;;;;;;;

	;; These combine collective effort and democracy into one predicate:

	:Struggles-together-against [:peasant1 :peasant2 :demon]   :P-peasants-against-demon
	:Struggles-together-against [:peasant1 :peasant2 :rat]     :P-peasants-against-rat
	:Struggles-together-against [:peasant1 :peasant2 :envy]    :P-peasants-against-envy

	;; This is a subak-level predicate:

	:Struggles-against [:subak :demon]   :P-subak-against-demon
	:Struggles-against [:subak :rat]     :P-subak-against-rat
	:Struggles-against [:subak :envy]    :P-subak-against-envy

	;; These connect the peasant-level and subak-level struggles, which are identical,
	;; since the subak acts only collectively (except when disordered):

	:Causal-if [:P-peasants-against-demon :P-subak-against-demon] :P-peasants->subak-against-demon
	:Causal-if [:P-peasants-against-rat   :P-subak-against-rat]   :P-peasants->subak-against-rat
	:Causal-if [:P-peasants-against-envy  :P-subak-against-envy]  :P-peasants->subak-against-envy
	;; Note These causal ifs are really iffs.  If I start processing the causal predicates in 
	;; special ways-- e.g. adding one-way links in the proposition network--then I might want to 
	;; do something different here.

	:Succeeds [:P-subak-against-demon] :P-subak-succeeds-against-demon
	:Succeeds [:P-subak-against-rat]   :P-subak-succeeds-against-rat
	:Succeeds [:P-subak-against-envy]  :P-subak-succeeds-against-envy

	:Fails [:P-subak-against-demon] :P-subak-fails-against-demon
	:Fails [:P-subak-against-rat]   :P-subak-fails-against-rat
	:Fails [:P-subak-against-envy]  :P-subak-fails-against-envy

	:Causal-if [:P-subak-fails-against-demon :P-subak-subak-disordered] :P-subak-fail-demon->disorder
	:Causal-if [:P-subak-fails-against-rat   :P-subak-subak-disordered] :P-subak-fail-rat->disorder
	:Causal-if [:P-subak-fails-against-envy  :P-subak-subak-disordered] :P-subak-fail-envy->disorder

	:Persists [:subak] :B-subak-persists
	:Ceases [:subak] :B-subak-ceases  ; should always receive negative activation

	;; HERE there is a big difference with the Brahmanic system:
	;; IF A SUBAK BECOMES DISORDERED, IT NEVERTHELESS PERSISTS:
	:Causal-if [:B-subak-ordered :B-subak-persists]
	:Causal-if [:B-subak-disordered :B-subak-persists]
