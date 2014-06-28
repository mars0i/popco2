Notes on biases, masks, communication, relations to propns
=======

## network creation in popco2:

In the initial version of `popco.core.person/make-person` (up until
June 2014, at least), I chose which analogy-net nodes to unmask by
calling `popco.communic.listen/try-add-to-analogy-net!` on every
proposition.  For each proposition, then, this function unmasks nodes
such that they are derived from matching that propn, and propns that
are already unmasked in the analogy net.

However, this makes the analogies allowed depend on the propns that
exist---i.e. that are unmasked.  Is this right?  How do I specify a
virus bias or beast bias via available analogies, in the crime3
analogy system, for example?  

## network creation in popco1:

Compare with popco 1:  The initial analogy network only involves nodes
from the specified propns.  There are no nodes or links at all for
propns that are not in one of the two analogue structures.  Then, I
think, communication might add them?  

## communication in popco1:

e.g. in crime3.lisp, I create "biases" by restricting the range of propositions
in the "source" analogue structure.  i.e. the "target" is always all of
crime-propns, but the source analogue may be restricted.  And I only allow
communication of target propns.  

So in this case, communicated propns never expand the network of any
individual: i.e. they all have all of the target propns.  What is
affected is only how they link these in the propn net in response to
what virus or beast analogues they have available.

However, in the sanday simulations, I allowed more.  I allowed propns
to be added to a network.

i.e. in theory, you could have part of a analogue structure, and then
later, communication fills in other parts of it.  This has to be allowed.

## communication and net creation in popco2:

Since communication in popco2 is supposed to involve only unmasking, and
not net restructuring (except that links in propn net can come from
activations in analogy net), it should be the case that you can have
an analogy network or propn net that includes propns that you've never
heard of, so to speak.

OK, so can't I do this (with the mechanisms I have as of June 2014)
simply by (a) creating the propn net and analogy net using *all* of the
propns, but (b) restricting the propns in the second, `propns`, arg to
`make-person`, and (c) restricting the fifth arg, `utterable-ids`?  It's
(b) that's the important part.  I was always passing all propns there.
So e.g. for the crime3.lisp behavior, always include all propns in
`crime-propns`, but include only `virus-propns` or only `beast-propns`
to get a bias, or none of these propns for the "no bias", or all of them
for the "both bias".  Yeah, looking at `make-person`, I think this will
work.  I think I've already got what I need built in---I had it right.
