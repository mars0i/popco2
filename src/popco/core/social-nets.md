social-nets.clj
=======

**The social network and communication group code is derived from
Kristen Hammack's excellent design for similar functionality in
popco1.**

### General ideas

The following principles come from Kristen Hammack's code for popco1:

Each person is a member of one or more groups.  These group memberships
determines who can talk to that person.

Each person has zero or more groups that it talks to, which is to say
that it may talk to any member of those groups, but not to anyone else.

A directed graph between persons representing directions in which
utterances are sent from person to person (perhaps including
bidirectional links) can be defined by putting each person in its own
group, and then letting each person talk to zero or more of those
groups.

Such directed graph schemes can be mixed with schemes involving larger
groups.

### Implementation notes

To construct a person's list of persons that they will talk to, I need:

1. A list of groups that the person will talk to.

2. A map from group ids to persons.

On the one hand, it's convenient to define the group map explicitly.
Then you can see up front what the groups are.  

On the other hand, it may be inconvenient to define the group map before
the persons are defined, since some of the person ids may be
autogenerated.

But if `make-person` uses 1 and 2 to generate the talks-to list of persons, 
then the group map is needed to define persons.

Solution: Have a separate init-popn step after filling the popn with
persons, and pass each person's (talked-to, i.e. member of) groups to
`make-person`, as in popco1.  Also, as in popco1, pass in talks-to-groups
and num-listeners.  This makes it easy to clone a person to create additional
similar persons.

### An old proposal for implementing what's described below

**Persons to group membership (i.e. *talked*-to) is many-to-many.**
It's convenient to see who's in a group, so maybe a group-to-person-set
hashmap would be useful, but to look up a person's groups, it's easier
to use a person-to-group-set hashmap.  So maybe do both (and not a real
database table, of course).

**Persons to talked-to groups is many-to-many.**  So same ideas apply.

These structures--some of them--could go in the persons, but don't put them,
there, since they won't be used at run-time.  Just give each person a
list of persons that it talks to.

**So store the group structures in the Population**.  Might as well put them
there.

**During initalization, create the group structures first, using id's of
planned persons.  Then pass into `make-person`:**

1. A group-membership group-to-persons hashmap

2. A list of groups that that person can talk to.  (Or an entire
hashmap, and then make-person will find that person's entry?)

And then `make-person` will construct the list of persons that the person
can talk to, and fill the `talks-to` field.  The person won't have any
other info about who it talks to.

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
