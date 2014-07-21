grouptest1.clj
====

grouptest1.clj is modeled on popco/crime/crime3socnet?.lisp .

Those models have a pundit, `AA`, made with `make-no-bias-crime-talker`,
who is a member of pundits, and who talks to members of the two main
groups (e.g. Vulcans and Bajorans).  The pundit "perceives" all crime
propns (i.e. they are passed as the `given` parameter`).

(In case you're wondering, the names are taken from the TV show "Star
Trek: Deep Space 9".  Deep Space 9 is a space station that's run by the
Federation near the planet Bajor, which is populated mostly by Bajorans.
Vulcans are another humanoid race.)

Vulcans are created with `make-virus-bias-crime-talker`.  They talk to
Vulcans, and possibly one other group.  They perceive no propositions.

Bajorans are created with `make-beast-bias-crime-talker`.  They talk to Bajorans, and
possibly one other group.  They perceive no propositions.

Where there is a second group that Vulcans and Bajorans talk to, it's
the group of individuals who are members of both.  i.e. this is the
bottleneck though which the Vulcans and Bajorans can transmit
information between each other.  (In one case I called the overlap group
"DS9". In another case, "Federation".  In a third case, it was just
folks, i.e. everybody.)  For example, in crime3socnet3.lisp, Bajorans
included one person, Kira, who was a staff member of Deep Space 9, and
Vulcans included one person, Worf (actually, "Whorf"), who was also a
staff member of DS9.

`make-*-bias-crime-talker` functions create persons who utter crime
propns, and no other propns.

`make-no-bias-crime-talker` creates persons with an empty analogue
structure.  i.e there are no analogies to the crime propns.

`make-virus-bias-crime-talker` has crime propns in one analogue, and
virus propns in the other.

`make-beast-bias-crime-talker` has crime propns in one analogue, and
beast propns in the other.

The number of people talked to on each tick (i.e. `num-listeners` in
popco1) is 1.

I believe that what's supposed to happen is that, in the propn net, all
propns have zero activations initially, and net structure as provided by
the analogies.  Then utterances from the pundit gradually build up
activations, as modified by the proposition network structure created by
the analogies.

(The correspondence between biases and group names is chosen to be mnemonic
though alphetic similarities: Vulcan/virus, Bajoran/beast.)
