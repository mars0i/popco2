Notes on parts of communic.clj
=======

**add-to-analogy-net:**

Background: All legal mappings between lot-elements are found by make-analogy-net,
in analogy.clj, along with their links.

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
