Notes on parts of communic.clj
=======

### High-level plan (5/2014):

1. For each speaker, choose zero or more listeners.

2. For each such speaker/listener pair, choose a single proposition
known by the speaker, whose activation is sufficiently far from 0.
This depends on a random factor for each instance.

3. For each listener, there will be thus be zero or more instances of
<speaker, proposition, valence>.

In order to pass this information from one step to the other, the
additional information:

1. Could be stored in the Population structure.  i.e. store a collection
of <speaker, proposition, activation, listener> tuples.  That means that
when the collection is being written, we should probably be outside of
parallel processing, even if the update is purely functional.  However,
when the collection is merely being read, that can be done in parallel.

2. Or it could be stored in speakers at an earlier stage (i.e. store
<listener, proposition, activation> pairs [this can be done in
parallel]), and/or in listeners, at a later stage (either <speaker,
proposition, activation>, or simply <proposition, activation>).
Transition from one to the other data structure shouldn't be done in
parallel.  Although maybe having multiple speakers updating a collection
in a listener would be OK?  As long as there's no possibility of two
identical entries, or dupes are preserved.  The reason this could be OK,
I think, is that typically only a few speakers would talk to a single
listener.  Unless we do a simulation with everyone talking to everyone,
or if you have an anti-pundit who listens to everyone.

### add-to-analogy-net:

Background: All legal mappings between lot-elements are found by
make-analogy-net, in analogy.clj, along with their links.

By default, however, those nodes are masked.  They don't contribute to
the changes in activation values.

When a propn is added to a person, we may be able to unmask some of the
masked mapnodes--to add them into the process.

We can't add a propn-propn mapnode, though, unless its analogue, all of
its component propositions, and their analogues already exist in the
receiver person.  However, we don't need to worry about predicates and
objects.  If a matched propn exists in the receiver, then so does its
predicate and so do its argument objects.  However, if the propn sent is
higher-order, then it's possible for it to be there without its argument
propns to be in the receiver.  (This might not be what *should* happen,
but it's what popco 1 did, so its a behavior we want to be able to
produce at least as an option.)

Similarly, if a matching propn exists in the receiver, nevertheless its
arg propns might not exist in the receiver.

So: We can only unmask a mapnode involving the sent propn if the
appropriate extended family propns on both sides exist all the way down.
And in that case, they all will get corresponding mapnodes unmasked,
along with predicates and objects along the way.

Furthermore: When a propn p1 is sent, if it is an argument of some other
propn p2, it may be that p2 can now participate in mapnodes.  i.e.
suppose that when p2 was sent, p1 was not yet unmasked.  Therefore p2
could not participate in mapnodes.  Now, however, it may be that it can.
