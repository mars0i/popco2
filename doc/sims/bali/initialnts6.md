initialnts6.md
====

Maybe everyone has farming analogues fixed.  Or rather, maybe, peasants
as centers of power wrt rice growing.  And the religious variants are
what are communicated.

So the first popco experiment is to show that you can get division of
what spreads depending on whether royal or peasant, i.e. without any
interaction with NetLogo.

And maybe that's enough.

To integrate this with NetLogo, the idea would be to set the bias on
transmission due to successful analogy very low.  (Maybe by changing
the function that determines the probability of transmission.)  So
that although you get a bias, it's extremely noisy.  (Or maybe the
default level noisiness is enough.)  But then you add in success bias.
So that just by the default bias, you get a very noisy tendency to
adopt the personal-farmer religious ideas, but with success bias you
get more.

In practice what my hypothesis is, is that the conceptions of the
brahmins was known to the peasants, but it was also known that it wasn't
for them.  But then some people adapted it (by substitution, which I'm
not modeling), and considered it as a possibility, and some adopted it,
because it could fit, analogically.  And then it spread through
success bias.

So with the default bias, with very clear, simple analogical structures
like those in crime3, I still get a distribution of outcomes.  Maybe
when success bias is added, I'll instead get a narrower distribution.
But maybe the analogies in the bali sim won't be as clear.

It would be interesting and valuable to make it go the other way: To
show that success bias can overcome the subtleties of analogy bias.  And
then push back on the original analogies.  Maybe that's another project,
or maybe I've already illustrated that with the sanday simulations in
the CASM paper.

Maybe this is the way to do my sim:   Show that the brahmin
conceptions don't spread--not by analogy, and not by success bias.
Then show that the peasant variation spreads, but not that reliably.
But then success bias tunes it up, because it's only when you get lots
of the components of the analogy that you get suppression of noisy
cropplan choices.

So for that last plan, I'll need a measure of how close it is to the
correct analogy, and use that to drive the noise-suppressor variable.
Maybe this could just be a domain mean, like what I plot in the bias
scatterplots.
