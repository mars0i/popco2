"Simulating" the effect of NetLogo on popco
====

On making the popco Bali sim act as in the way it would when
configured to be connected to NetLogo.
(Copied from bali/doc/ArchitectureNotes5.md)

-----

A nice result would be that when popco is influenced by NetLogo, the
distribution resulting from worldly-peasant bias is less spread out than
usual.

But this can be simulated without NetLogo.  NetLogo will send a value
that makes trust higher when the speaker is successful.  A way to
simulate this in popco alone is simply to make trust higher when the
speaker's mean activation in the spiritual-peasant direction is higher.
(Or don't do it with trust, and instead make the prob of tran higher
when spiritual-peasant mean activn is higher.)

(This might make it unnecessary to connect Clojure and NetLogo, cook
as that might be.)

-----
