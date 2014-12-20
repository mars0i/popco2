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

------------

How to allow success/prestige/similarity/skill biases, i.e.
model-based biases?

For both frequency and model-based biases, there should be a function
called from `receive-utterance` or somewhere else in `communic.listen`
that decides which speaker(s) to listen to.  For frequency biases, this
should probably be computed on the set of utterances received in a given
tick.  (Or you could implement some kind of memory to allow
multiple-tick biases, but that should be avoided unless it's important.)

But for the model biases, how to communicate the relevant information to
this function?

Currently (up to Mid-December 2014), Utterances contain a proposition
id, a valence (1 or -1), and a person id.  The latter was originally
included just for debugging, etc.

I. One option is to calculate a value that's sent as a fourth component of
the Utterance.  This could be some calculated or assigned prestige or
success value, for example--calculated on the `speak` side.  Call this
`model-quality` for now.  (Though if it's constant, it might be
implemented by other means.)

Since a person will normally have the same model-quality value, in a
given tick, for all of its utterances, this value could be memoized
in some way--maybe by `assoc`ing it into the person.

Note that it might be possible to interpose the model-quality
calculation without any hooks simply by creating an alternative version
of `main/many-times` that intersperses mapping a special function
between each call to `once`.

II. Another method would be to replace the speaker-id field of the Utterance
with the speaker itself at the time of the utterance.  Clojure would
implement this, presumably, but simply including a pointer to the person
in the utterance, so it wouldn't be wasteful of memory.  This method
would allow the model-evaluation function on the listener side to
examine arbitrary aspects of the person in order to decide whether to
accept its utterances.  

Obviously, this could be used to do things that are unrealistic--the
listener could plumb everything about the internal state of the
speaker--so it would be up to the modeler to write evaluation functions
that are reasonable.

However, I think it would be more difficult to memoize the model-quality
calculations.  Well, the listener could memoize them in its own
structures, but each listener would have to perform the calculations
anew.

-----

Method II provides a superset of functionality of method I, if the
model-quality calculation is stored in the person.  i.e. the person
would be passed in the utterance, but would then be used only as the
repository for the model-quality variable.

Method I provides the functionality of method II, as well, since we
need make no restriction on what goes into the model-quality variable,
or on what the function that evaluates it expects.  So you could just
pass the person in the model-quality variable (along with a separate
person-id value).

In a sense, the two methods differ only in what the default expectation
is about what's in the extra Utterance field, and in whether or not
there is a separate `speaker-id` field.

I'm going to go with "method I".  i.e. I'm going to start by keeping
`speaker-id`, and exploring passing only a float in an extra field.
