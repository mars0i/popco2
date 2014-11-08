initialnts9.md
====

## Revision of initialnts8.md with `:envy` removed.  

#### Explanation of revision:

That peasants and subaks have to deal with an inner "demon" is not
something that kings don't also have to deal with.  So it's not really
reasonable to create a lack of parallellism between the Brahamanic and
peasant domains in this way.  Moreover, the "struggle against" demons,
etc. is really an internal struggle, \ie struggle against things like
envy is the medium of struggle against demons.  Something like that.

So the main differentiators between the Brahmanic and peasant domains
will be (a) the individual/autocratic vs corporate/democratic decision
structure, and (b) whether the subak/state persists after failure to
control the bhutakala.

I guess I could also add facts about rats etc.  e.g that they invade
fields, whereas enemies of the negara do something else.  Invade the
negara, maybe.  Or maybe say that rats and insects destroy from
within.

See previous initialnts*.md for comments deleted here.

----------------------

There are two analogue domains, each with two analogues in it, as in the
Sanday and crime analogy systems.  I call the higher-level domains
"source/target domains", and sometimes call the lower-level, embedded
domains "subdomains".

One goal to be able to get popco to select one subdomain from a
source/target domain by including only one of the two domains in the
other source/target domain.

Let's say that there are "spiritual" analogues and the "worldly"
analogues (although that distorts Balinese thinking).

There are two kinds of spiritual analogues: Brahmanic/negara/state and
peasant/subak.

There are two kinds of worldy analogues: Those that concern rice
farming, water use, and local pest infestations; and those that concern
events that befall negaras (states): Attacks by enemies, large-scale
natural disasters, rodent plagues, etc.

(And note that even though for the Balinese these must be the same,
there had to be historical processes in which slippage occurred.  It's
not that the domains that I'm calling spiritual and worldly are *the*
domains within which different shifts occurred, and between which
analogies occurred.  These terms merely distinguish domains that might
in some cases have been relevant to such processes.  So I am not
actually treating Balinese culture in terms of Western distinctions
between spiritual and the world.)

So we have:

<table>
<tr align="center"><td>spiritual</td><td> worldly</td></tr>
<tr>
  <td><table><tr><td>brahmanic</td><td>peasant</td></tr></table></td>
  <td><table><tr><td>negara</td><td>rice</td></tr></table></td>
</tr>
</table>
</table>

To represent the fact that subaks have members, I simply give the
subak two members.

-------------

Question: What are the effects below of:

1. Sharing the various predicates such as `Is-bhutakala` between the
spiritual and worldly domains--i.e. between the source/target domains?

	Answer: This will cause the analogy net to generate a link from
	SEMANTIC to `Blah-blah=Blah-blah`.  So this is fine.

2. Sharing entire propositions between the spiritual and worldly source/taget domains?

	In the analogy net, I don't think anything special happens
	except via the SEMANTIC link to the predicates.  And other than that, they will be
	as if different propositions, since they're in different analogue domains.

	But in the proposition net, maybe this will cause confusion?
	Because what are, on the analogy net side, distinct
	propositions, will be on the proposition net side, the same
	proposition.  So maybe this would be a *bad thing*.  (??)

2. Sharing the proposition `Is-bhutakala [:demon]` between the Brahmanic
and subak domains?

	Answer: Not sure.  It might cause confusion in analogy settling.
	There's only one proposition there, but they'll be involved with
	propositions in both subdomains of the spiritual source/target
	domain.  An option is to arbitrarily distinguish the two kinds
	of demon.  Maybe it's not arbitary?  A demon involved with kings
	is a different kind than that gets involved with peasants??

------------------

### Explicit semantic relationships:

	(def conceptual-relats 
	  [[-1.0 :Causal-if :Preventative-if]
	   [-1.0 :Is-ordered :Is-disordered]
	   [-0.9 :Subject-of :Member-of] ; probably not needed
	   [-1.0 :Persists :Ceases]
	   [-1.0 :Succeeds :Fails]])

Maybe add that bhutakala aren't negaras, etc.


----------

### Objects:

#### Brahmanic:

##### both:

	:Is-king [:king]
	:Is-negara [:state]

##### spiritual:

	:Is-bhutakala [:demon]   ; shared with subak domain

##### worldly:

	:Is-bhutakala [:enemy]

#### Subak:

##### both:

	:Is-peasant [:peasant1]
	:Is-peasant [:peasant2]
	:Is-subak [:subak]

##### spiritual:

	:Is-bhutakala [:demon]  ; shared with Brahmanic domain

##### worldly:

	:Is-bhutakala [:rat]

----------

### Relations
	
#### Brahmanic:

##### both:

	:Is-ordered [:state]    :B-state-ordered
	:Is-disordered [:state] :B-state-disordered

	:Persists [:state] :B-state-persists
	:Causal-if [:B-state-ordered :B-state-persists]

	:Ceases [:state] :B-state-ceases
	:Causal-if [:B-state-disordered :B-state-ceases]

##### spiritual:

	;; STRUGGLE
	:Struggles-against [:king :demon] :B-king-against-demon

	:Succeeds [:B-king-against-demon] :B-king-succeeds-against-demon
	:Causal-if [:B-king-succeeds-against-demon :B-state-ordered] :B-state-succeed-demon->order

	:Fails [:B-king-against-demon] :B-fails-against-demon
	:Causal-if [:B-king-fails-against-demon :B-state-disordered] :B-king-fail-demon->disorder

##### worldly:

	;; STRUGGLE
	:Struggles-against [:king :enemy] :B-king-against-enemy

	:Succeeds [:B-king-against-enemy] :B-king-succeeds-against-enemy
	:Causal-if [:B-king-succeeds-against-enemy :B-state-ordered] :B-succeed-enemy-order

	:Fails [:B-king-against-enemy] :B-king-fails-against-enemy
	:Causal-if [:B-king-fails-against-enemy :B-state-disordered] :B-state-fail-enemy->disorder

----------

#### Subak:

##### both:

	:Member-of [:peasant1 :subak]
	:Member-of [:peasant2 :subak]

	:Is-ordered [:subak]   :P-subak-ordered
	:Is-disordered [:subak] :P-subak-disordered

	:Persists [:subak] :B-subak-persists
	:Causal-if [:B-subak-ordered :B-subak-persists]

	:Ceases [:subak] :B-subak-ceases  ; should always receive negative activation
	:Causal-if [:B-subak-disordered :B-subak-persists] ; NOTE this differs from Brahmanic

##### spiritual:

	;; STRUGGLE
	:Struggles-together-against [:peasant1 :peasant2 :demon]   :P-peasants-against-demon
	:Struggles-against [:subak :demon]   :P-subak-against-demon
	:Causal-if [:P-peasants-against-demon :P-subak-against-demon] :P-peasants->subak-against-demon

	:Succeeds [:P-subak-against-demon] :P-subak-succeeds-against-demon
	:Causal-if [:P-succeeds-against-demon :P-subak-ordered] :P-subak-succeed-demon->order

	:Fails [:P-subak-against-demon] :P-subak-fails-against-demon
	:Causal-if [:P-subak-fails-against-demon :P-subak-disordered] :P-subak-fail-demon->disorder

##### worldly:

	;; STRUGGLE
	:Struggles-together-against [:peasant1 :peasant2 :rat]     :P-peasants-against-rat
	:Struggles-against [:subak :rat]     :P-subak-against-rat
	:Causal-if [:P-peasants-against-rat   :P-subak-against-rat]   :P-peasants->subak-against-rat

	:Succeeds [:P-subak-against-rat]   :P-subak-succeeds-against-rat
	:Causal-if [:P-subak-succeeds-against-rat :P-subak-disordered] :P-subak-succeed-rat->disorder

	:Fails [:P-subak-against-rat]   :P-subak-fails-against-rat
	:Causal-if [:P-subak-fails-against-rat :P-subak-disordered] :P-subak-fail-rat->disorder


