main.clj
=======


Note that from experiments, it appears that actually seems 
to happen in many-times is that the iterate goes through each 32-person chunk of
the per-tick map call separately.  i.e. first it iterates through
mapping through 32 persons.  Then it iterates through mapping through
the next 32 persons.  And so on.  Whereas pmap does each of those
32-person chunks in a separate thread (rather than in 32 threads,
I believe).  The evidence for this is that you can show that each
32-person chunk with map seems to take just about the same time
as doing the whole thing with pmap.
