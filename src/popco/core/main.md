main.clj
=======

### How to turn off conversation

Conversation can be disabled simply by setting talk-to-groups to an
empty collection.

(It should be possible to change group membership over time, too.  Not
sure if this works.)

(I see no need pressing to provide a switch variable that turns
conversation functions on and off, as there was in popco1.  (However, if
in the future, it's desirable to allow a naive user to turn conversation
off and then back on, the current scheme would require saving and
restoring the old group memberships.))

### `many-times`

Note that from experiments, it appears that actually seems to happen in
many-times is that the iterate goes through each 32-person chunk of the
per-tick map call separately.  i.e. first it iterates through mapping
through 32 persons.  Then it iterates through mapping through the next
32 persons.  And so on.  Whereas pmap does each of those 32-person
chunks in a separate thread (rather than in 32 threads, I believe).  The
evidence for this is that you can show that each 32-person chunk with
map seems to take just about the same time as doing the whole thing with
pmap.

Note: There's no need to provide for the possibility of turning off the
conversation functions.  They can be disabled simply by putting each
individual in a distinct group.  (It should be possible to change group
membership over time, too.)

### `once`
It's not clear that it will ever be necessary to use 'map' rather than
'pmap' for mapfn, except for testing (which should be done, e.g. on Cheaha).
If mapfn = map, it's lazy; iterating through once calls won't realize
the persons in each tick (until later). Why would we want that, even if
it's useful to iterate through ticks/generations lazily? (Will this become
irrelevant when transmit-utterances no longer simply passes the population
through?  Without doall, would it be possible to look only
once person in generation 500, let's say, and then realize only
its communicative ancestors?  Maybe that would be efficient.  But then
you'd have to wait a while to realize that one person.)


### Misc notes

Maybe settle-analogy-net should be done just once at the beginning.
This seems reasonable, and would be faster, though will produce results
that differ from popco1 simply because communication can depend on
subtle variations.  However, in existing popco1 analogy nets,
there was (small) cycling behavior, which can affect propn links, and
therefore everything.  This won't play a role if I pull this out
of the main loop.  But also, then what should I do about cycles?
set to their average values?

Hmm well if I do take analogy-net settling out of the main loop,
then the contribution of the analogy net to the propn net links
would also be static, right?  Because the analogy net activations
don't change.  So the only thing that would change in the propn net
would be due to the bump that you get from receiving a propn, etc.


Note popco1's update-analogy-net isn't needed.  It revised the
structure of the analogy net in response to new propns.  That's
now done once and for all at the beginning.  And transmit-utterance
can cause the unmasking.  This is done in communic/receive-propn!.
Notes:

Maybe settle-analogy-net should be done just once at the beginning.
This seems reasonable, and would be faster, though will produce results
that differ from popco1 simply because communication can depend on
subtle variations.  However, in existing popco1 analogy nets,
there was (small) cycling behavior, which can affect propn links, and
therefore everything.  This won't play a role if I pull this out
of the main loop.  But also, then what should I do about cycles?
set to their average values?

Hmm well if I do take analogy-net settling out of the main loop,
then the contribution of the analogy net to the propn net links
would also be static, right?  Because the analogy net activations
don't change.  So the only thing that would change in the propn net
would be due to the bump that you get from receiving a propn, etc.


Note popco1's update-analogy-net isn't needed.  It revised the
structure of the analogy net in response to new propns.  That's
now done once and for all at the beginning.  And transmit-utterance
can cause the unmasking.  This is done in communic/receive-propn!.
