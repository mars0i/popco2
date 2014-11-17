anomalies.md
====

Notes on anomalies in the analogies occurring during the development of bali/propns.clj.

I. In git commits up to 2a1b956, sophie, i.e. the person with spiritual
peasant bels pegged `WP-water-rice-ordered` was going low, which is not
what's supposed to happen.

`WP-water-rice-ordered`: (A) has neg links to pos nodes:

	SP-water-peasant1-ordered
	SP-water-peasant2-ordered
	SP-subak-succeeds-against-demon
	SP-subak-fails-against-demon


(B) and also neg links to neg nodes:

	SB-king-fails-against-demon
	SB-king-succeeds-against-demon

Oh--well (A) is a problem.  Because sophie is the person in which all of
the SP propns are pegged to near 1.  So neg links to them will of course
make WP-water-rice-ordered low.  Why are there neg links here?

WP-water-riced-ordered is:

	Is-ordered [WP-water-nourishes-rice]

WP-water-nourishes-rice is:

	Nourishes [water rice]

That's positive in sophie, but only barely.

SP-water-nourishes-peasant1 is:

	Is-ordered [SP-water-nourishes-peasant1]

where SP-water-nourishes-peasant1 is:

	Nourishes [water peasant1]

So `WP-water-rice-ordered` should map to
`SP-water-nourishes-peasant1` if `rice` maps to `peasant1`.
So let's look at *rice=peasant1*. i.e. *peasant1=rice*.

*peasant1=rice* has activn about 0.03.

I think it's just that peasants aren't enough like rice.
Which makes sense.

I wonder what would happen if I used only one peasant?
Or said water-nourishes-subak instead.

Instead, what I did was to comment out these in git commit f54492e:

	; (defpropn WP-water-nourishes-peasant1 Nourishes [water peasant1])
	; (defpropn WP-water-nourishes-peasant2 Nourishes [water peasant2])
	; (defpropn WP-water-peasant1-ordered Is-ordered [WP-water-nourishes-peasant1])
	; (defpropn WP-water-peasant2-ordered Is-ordered [WP-water-nourishes-peasant2])

That did it.  sophie looks good, wilfred looks good.

siobhan and wilbur are ok in that the primary effect--of the pegged
propns on their analog--is right.  In the anti effects, there are some
anomalies in these persons.  Note, however, that the anomalies are the
same in both siobhan and wilbur, and in both of the anti domains.  So
fixing one domain and person will probably fix the others.


II. In git commits up to at least f54492e, proposition activations are
doing the right thing in response to pegging analog propositions high
in every one of the four persons in hermits.clj, but there are a few
propositions that are not going negative in response to the competing
analog propositions being high. The propositions involved seem
related.

These propositions are going high in siobhan (spiritual brahmanic pegged) and wilbur
(worldly brahmanic pegged), but you'd expect them to go low.

	(defpropn WP-subak-against-rat Struggles [subak rat])

	(defpropn SP-subak-against-demon Struggles [subak demon])

Also, the following propositions have close to zero activation in
siobhan (spiritual brahmanic pegged) and wilbur (worldly brahmanic
pegged).  Maybe that's OK, but these propositions are clearly related to
the preceding ones.

	(defpropn WP-peasants-against-rat Struggles-together [peasant1 peasant2 rat])
	(defpropn WP-peasants->subak-against-rat Causal-if [WP-peasants-against-rat WP-subak-against-rat])

	(defpropn SP-peasants-against-demon Struggles-together [peasant1 peasant2 demon])
	(defpropn SP-peasants->subak-against-demon Causal-if [SP-peasants-against-demon SP-subak-against-demon])

I suspect this behavior is occurring because of the following propositions that get
pegged in siobhan or wilbur:

	(defpropn WB-king-against-enemy Struggles [king enemy])

	(defpropn SB-king-against-demon Struggles [king demon])

And maybe that's how it should be?  Or should I remove some of these
propositions?

Well, on one hand, maybe this should be fixed, since the Brahmanic folk
would have no direct dealings with rats in the sense that I intend it
here.  So that they can map propns having to do with rats and subaks it
doesn't seem right.  And there are no kings among the peasants, so that
ought to be a problem.  On the other hand, the point is that the
Brahmanic ideas have to be available to the peasants, and they have to
reject

OK: One point is to show that the Spiritual Peasant propositions, will
generate "good" beliefs about rice growing, and that the Spiritual
Brahmanic propositions will not, or will produce the wrong ones, or will
bias to make the Worldly Peasant propositions low.  So I want that the
SB propositions make the WP propositions low.  All low--would be nice.
But perhaps it's more realistic if it's messier?

But in any event, it *is* a significant thing for me if in siobhan
(pegging the SP propns), some WP propositions go high.  That's not
optimal, and is acceptable only if I want to allow such suboptimality.
(And my current hypothesis is that it's SB-king-against-demon that's
causing this result.)

I thought that maybe adding back the `Is-king`, `Is-peasant[12]`, and
`Is-subak` propns, and uncommenting or adding negative semantic specs
for those predicates, would solve the problem.  but it doesn't change
anything.

Well: One option is to simply take out WP-subak-against-rat and the
corresponding SP proposition, and just leave the individual subaks doing
it using e.g.  WP-peasants-against-rat.  Because it's this one that's
mapping with the Brahmanic propns, I believe.  But leave the
"struggles-together" ones involving peasants.  But also I'd have to leave
out the causal/logical connection between peasant struggle and subak struggle.

OK! I have a new solution.  Seems OK.  I got rid of (commented) the following:

	;(defpropn SP-subak-against-demon Struggles [subak demon])
	;(defpropn SP-peasants->subak-against-demon Causal-if [SP-peasants-against-demon SP-subak-against-demon])
	;(defpropn SP-subak-succeeds-against-demon Succeeds [SP-subak-against-demon])

	;(defpropn WP-subak-against-rat Struggles [subak rat])
	;(defpropn WP-peasants->subak-against-rat Causal-if [WP-peasants-against-rat WP-subak-against-rat])
	;(defpropn WP-subak-succeeds-against-rat Succeeds [WP-subak-against-rat])

and made these deal directly with the subak propns rather than via the
subak propns above:

	(defpropn SP-subak-succeeds-against-demon Succeeds [SP-peasants-against-demon])
	(defpropn SP-subak-fails-against-demon Fails [SP-peasants-against-demon])

	(defpropn WP-subak-succeeds-against-rat Succeeds [WP-peasants-against-rat])
	(defpropn WP-subak-fails-against-rat Fails [WP-peasants-against-rat])

This got rid of e.g. 

	(defpropn WP-subak-against-rat Struggles [subak rat])

which was mapping (I believe) 

	(defpropn SB-king-against-demon Struggles [king demon])

and the only thing left to map would be e.g.

	(defpropn WP-peasants-against-rat Struggles-together [peasant1 peasant2 rat])

which won't map `SB-king-against-demon` because the arities are
different, reflecting the fact that a subak is a bunch of individuals,
while a king is just one.

The result is that all propns in "anti" analogies are either negative or 
near zero.  That's good--how it should be.
