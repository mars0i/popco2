social-nets.clj
=======

### Notes

Use Kristen Hammack's overall design from popco1, but try to front-load
as much of the computation into initalization as possible.  i.e. use her
representation of the directed graph as group relations.

Most of the time there will be no group/social-net changes during
runtime, so we can fix some things in advance, but maybe do so using
repeated application of routines that could be used for individual
updates during runtime.

Seems like you could have a global group of

* groups that person x is in (i.e. that the person "talks from")

* groups that person x talks to, i.e. the person talks to people in those
  groups.

These could be stored in the population.

There's no reason, at runtime, that each person couldn't be associated
*only* with those to whom it talks.  Something like this.  No need to
muck with the groups at runtime, unless they are changing.  This list
could be located in a central data structure associated with the
population, or stored in each person.  But you need to know to home x
talks to, because there will need to be a computation of which person
it'll talk to this time.  And then you need to choose propositions to
be sent.

Once the conversation pairs (i.e. x talks to y this time) are formed,
the operation could be parallelized.  Everyone is talking,
potentially, but at that point they are only being *modified* by being
talked to.  So the reference to the speaker in the conversation pair
will not cause modifications--only the listener ref does that.

OK, actually it's more complicated since more than one person can
speak to a given person each time.  So to allow parallelization of
adding propositions to persons, you'd have to collect utterances *to a
given person* on a given tick, and parallelize over such collections,
i.e. listener-specific collections of utterances (and after a certain
point, you don't need the speaker any more, either).

And actually the forming of conversation pairs could be parallelized
as well.

i.e. we can separately parallelize

1. Calculation of each speaker's listeners and the proposition
uttered.  This is a map across speakers.

2. Calculation of the effect of a proposition on the listener, or
rather the calculation of the effects of all speakers sent to the
listener.  This is a map across listeners, in effect.

After step 1 you need to (a) collect utterances into per-listener
groups, and then (b) pass those collections to step 2.

