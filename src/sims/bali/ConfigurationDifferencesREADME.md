ConfigurationDifferencesREADME.md
====

#### Note:

"15-15-16-18" refers to a set of propositions for the Bali sim that I
ultimately settled on.  So this same set of propns is used for all of
the simulations under data/popco2/bali/twopundits.

----------------------------

#### 15-15-16-18 

15-15-16-18/ contains the original simulations with the base
configuration.  The main runs (there is a set of separate runs
underneath this) used sims/bali/threegroups2pundits.clj.  This gives
three groups of 40 persons each that don't communicate between groups.
i.e. this could have been done in three separate runs.  The three
groups are:
	Worldly Peasant bias
	Worldly Brahman bias
	No bias

----------------------------

#### randomWPbias

randomWPbias/, which is the result of sims/bali/randomWPbias.clj,
should--and does--give results similar to the peasant (Worldly Peasant)
group in 15-15-16-18, but randomWPbias does so in a different way.

threegroups2pundits.clj uses no bias filter (in fact I don't believe I
had added this option yet), in randomWPbias.clj, listeners decide
which speaker to copy, of those attempting to speak to the listener, 
by deciding which of the speakers has higher quality.  However, in
randomWPbias.clj, this quality is determined randomly.
randomWPbias.clj also differs from the peasant group in
threegroups2pundits.clj in that max-talk-to is 1 for each regular
person (not a pundit), whereas in randomWPbias.clj, max-talk-to is 5.
That means that each speaker tries to talk to five listeners.  That
way, each listener gets more speakers trying to talk to it, and thus
will generally have a few listeners to choose between.

----------------------------

#### successWPbias 

successWPbias/ is like randomWPbias/, except that in determining which
speaker is of higher "quality", successWPbias.clj uses a success
function--rather than a random function.  This success function is
sims.bali.succes/spiritual-peasant-ness, whose value is a function of
the mean activation of the Spiritual Peasant propositions.

i.e. in successWPbias.clj, there is a Worldly Peasant bias on
propositions, which increases the likelihood that persons will adopt
Spiritual Peasant propns.  But then success bias amplifies this effect
(sort of--see below) by treating those speakers with higher Spiritual
Peasant activations as more worthy of being listened to.

----------------------------

#### successNoWbias 

successNoWbias/ is like successWPbias, except that successNoWbias.clj
gives no role to Worldly Peasant bias.  i.e. this uses pure Spiritual
Peasant success bias.

And the interesting thing is that this produces *more* extreme
effects on Spiritual Peasant propositions than when there was an
analogy bias.  The results from SuccessWPbias are intermediate between
those of RandomWPbias (and the original threegroups peasant group),
and results of SuccessNoWbias.  The analogy bias in effect produces a
braking effect.  Maybe because it leads to fixation too quickly.

----------------------------

#### successWPbiasTrust025

Same as successWPbias (using same main clj file), but also redefines the
"global" constant variable `trust` to 0.025, i.e. half of its usual
value 0.05, using `sims.bali.trust025`.

When a listener receives an utterance from a speaker (after choosing it,
if there is a speaker bias process such as success bias), the effect on
the listener's degrees of belief is either negative or positive, but the
magnitude of the effect is the same for each utterance.  Reducing
`trust` reduces the magnitude of the effect.  (The magnitude of the
speaker's degree of belief affects whether it utters the utterance, but
not its effect on the listener.)

Reducing the value of `trust` means that speakers have less of an effect
on the degrees of belief of listeners.

I thought that reducing `trust` might allow success bias to exhibit a
stronger effect.  I thought that it might move the scatter cluster to
the right, i.e. closer to what you see in the `successNoWbias` scatter
plot, as opposed to what's in the `successWPbias` scatter plot.

However, the result for `successWPbiasTrust025` appears to be almost
identical to that for `sucessWPbias`.  In fact, the mean for the latter
is closer to the pure success bias case.  (It may be that the low-trust
config results are more spread out in the Brahmanic direction.  But that
might just be a sampling artifact.)

(More specifically, the effect of an utterance on the proposition net of
the listener is (a) to add the proposition, is if it's missing, and (b)
to add or modify a link from `SALIENT` to the proposition node.  In
particular, in popco1 and in (the initial configuration, at least, of)
popco2, the weight of this link is modified by adding or subtracting
`trust` to/from it, depending on whether the speaker believed or
disbelieved the proposition uttered.)

----------------------------

#### successWPbiasHalfAnalogy2Propn

Same as successWPbias (using same main clj file), but also redefines the
"constants" `analogy-to-propn-pos-multiplier` and
`analogy-to-propn-neg-multiplier` to half their usual values using
`sims.bali.analogy_to_propn_halved`.  These variables control the
strength of the effect of analogy-net activation values on
proposition-net link weights.  Higher values mean that analogies between
propositions generate stronger links between the propositions in the
propn net.  (These variables were called `*propn-excit-weight*` and
`*propn-inhib-weight*` in popco1.  See section "Belief network concepts
and initialization", page 12, item #1 in the "Moderate Role" paper about
popco1 for details.)

Usual values:

	analogy-to-propn-pos-multiplier: 0.2
	analogy-to-propn-neg-multiplier: 0.025

Redefined values:

	analogy-to-propn-pos-multiplier: 0.1
	analogy-to-propn-neg-multiplier: 0.0125

Unlike `successWPbiasTrust025`, this experiment *does* produce an effect
that's intermediate between success bias + analogy effects with normal
parameters (`successWPbias`) and success bias with no analogy influences
(`successNoWbias`).

----------------------------

#### randomWPbiasHalfAnalogy2Propn

Same as `randomWPbias` (using same main clj file), but also redefines the
"constants" `analogy-to-propn-pos-multiplier` and
`analogy-to-propn-neg-multiplier` to half their usual values using
`sims.bali.analogy_to_propn_halved`.  See previous entry for
explanation of these variables.

This gives a different result from `randomWPbias`, but I'm not sure I
understand why at this point.  It doesn't appear to be significantly
different from in the peasant direction, but looks much more negative
in the Brahmanic direction.
