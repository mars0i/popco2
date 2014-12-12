Notes on success bias
====

See also ArchitectureNotes7.md in the bali git repo.

------------------

What is success bias in popco?  It's some function of a set of beliefs.
For example, if we take the world to be a certain way, and so some
beliefs are true while others false, success could be a function of the
degree to which activations match the true values.  This might for
example be a function of the mean activation in each person of those
specified beliefs.  If the world is changing, then the specified set of
beliefs would have to change, too.  It might be necessary to have some
truths be that certain beliefs are false, so more complicated distance
measure than a simple mean (which can be thought of as a distance from
1) would be needed.  

There are other kinds of success: In the Bali sims, success should be,
strictly, speaking harvest yield in NetLogo.  However, since strong
spiritual-peasant beliefs in popco should lead to greater harvest
yields, on average, a simple popco-only kind of success would be some
measure strength of spiritual-peasant beliefs--not because they are
true, but because they are useful, in effect.

Note that since I can implement whatever success function I want,
it can equally well be considered a prestige bias, for example.

A frequency-bias would maybe be implemented solely on the listener
side, with lots of utterances (max-talk-to > 1), and allowing the
filtering function on the listener side to examine all of the
utterances (or a sample from them) and calculate frequencies.

------------------

It's not hard to implement some kind of probabilistic success bias.  As
of mid December 2012, I have hooks for that that I'm working on.  This
is most easily implemented as a modification of the probability of
transmission of propositions that is a function of something about the
speaker.  i.e. my initial strategy is to allow modification of
`speak/worth-saying-ids` by replacing a `worth-saying` function that's
called by it.

But that's probably not the right strategy.  What it does is affects
*which* propositions are most likely to be uttered.  A listener is
thereby (a) more likely to have something said to it, and (b) more
likely to get one of the success-biased propositions said, rather than
others.  But that's not success bias.  Success bias is an influence on
which speaker the listener listens to.

What's harder, at this moment (12/2014), is to implement anything like
an imitate-the-best success bias.  Because this can require more global
information, or information about the listener.  For example, if the
listener is to choose from everyone who speaks to it--or speaks the same
thing to it--the speakers won't necessarily know who is speaking to the
listener on a given tick.

From one perspective, what success bias is, is an on-the-fly
modification of the social network structure.  My social net becomes
whoever is most successful at present.  This would involve editing the
talk-to group variables in persons and in the population.  That should
be avoided.

i.e. at present, I can calculate some success function within a person,
but it's not easy to calculate comparative success.  To do so, maybe the
simplest thing would be to send a degree-of-success variable across from
speaker to listener in the utterance.  In that case, maybe I could add
something inside/called from `listen/receive-utterances` that applies a
success-bias filter on propositions, so that only some are left to be
received.  If I'm putting a success variable into the utterances, I'd
still need to calculate it in the speaker.

Note that if I do this sort of thing, it might be best to increase the
`max-talk-to` in persons so that listeners get more utterances per tick.

------------

I've got a prototype success function illustration in
sims/bali/functions.clj.

To add that to utterances, I could alter communic/utterance.clj, or
not--since records are maps, and can have arbitrary fields added--and
need to alter `speak/make-utterances`.  However, it would be natural to
modify `utterance/make-utterance`, since that's what's called in
`speak/make-utterances`.  Maybe send `nil` when there's no success bias?
Or a constant--0, maybe.  And then I'd have to add an extra filtering
step in `listen/receive-utterances`.

