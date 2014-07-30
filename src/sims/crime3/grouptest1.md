grouptest1.clj
====

grouptest1.clj is modeled on popco/crime/crime3socnet?.lisp .  The
following are notes on how the popco1 models in this family are
structured and created in popco1.

The popco1 models have a pundit, `AA`, made with `make-no-bias-crime-talker`,
who is a member of pundits, and who talks to members of the two main
groups (e.g. Vulcans and Bajorans).  The pundit "perceives" all crime
propns (i.e. they are passed as the `given` parameter`).  (It's named
"AA" so that it will sort first.)

Vulcans are created with `make-virus-bias-crime-talker`.  They talk to
Vulcans, and possibly one other group.  They perceive no propositions.

Bajorans are created with `make-beast-bias-crime-talker`.  They talk to Bajorans, and
possibly one other group.  They perceive no propositions.

The correspondence between biases and group names is chosen to be mnemonic
through alphetic similarities: Vulcan/virus, Bajoran/beast.

Where there is a second group that Vulcans and Bajorans talk to, it's
the group of individuals who are members of both.  i.e. this is the
bottleneck though which the Vulcans and Bajorans can transmit
information to each other.  (In one model I called the overlap group
"DS9". In another case, "Federation".  In a third case, it was just
folks, i.e. everybody.)  For example, in crime3socnet3.lisp, Bajorans
included one person, Kira, who was a staff member of Deep Space 9, and
Vulcans included one person, Worf (I typed "Whorf", however), who was
also a staff member of DS9.

`make-*-bias-crime-talker` functions create persons who utter crime
propns, and no other propns.

`make-virus-bias-crime-talker` has crime propns in one analogue, and
virus propns in the other.

`make-beast-bias-crime-talker` has crime propns in one analogue, and
beast propns in the other.

`make-no-bias-crime-talker` creates persons with an empty analogue
structure.  i.e there are no analogies to the crime propns.

The number of people talked to on each tick (i.e. `num-listeners` in
popco1) is 1.  This applies to the pundit AA along with everyone else.
(Note however that one might want to allow the pundit to speak to
everyone on every tick, especially if it represents a common environment
that everyone in the group can perceive.)

I believe that what's supposed to happen is that, in the propn net, all
propns have zero activations initially, and net structure as provided by
the analogies.  Then utterances from the pundit gradually build up
activations, as modified by the proposition network structure created by
the analogies.

(In case you're wondering, the names are taken from the TV show "Star
Trek: Deep Space 9".  Deep Space 9 is a space station that's run by the
Federation near the planet Bajor, which is populated mostly by Bajorans.
Vulcans are another humanoid race.)
