Notes on different versions of the bali propositions
====

See also anomalies.md and initialnts*.md.

propns1121_15_15_16_18.clj, which contains code that was run on 11/21
and 11/22/2014, and then rerun on 11/26, has these proposition counts
in the four domains:

Numbers of propositions:
worldly brahman:  15
spiritual brahman:  15
worldly peasant:  16
spiritual peasant:  18


propns1123_16_16_16_16.clj makes relatively small modificiations in the
preceding clj file in order to give each domain the same number of propositions.
I thought that differences in the number of propositions was causing
oddities in the run mean scatter plots, so I tried this.

Although the hermits barcharts for both versions are similar, the scatter
plot for the 16/16/16/16 version is odd--not coming out right (which is
disturbing, given that the barcharts are similar).  Moreover, the
15/15/16/18 version, which is coming out OK--the directions of effect
are as espected, though not very strong--makes more sense, intuitively
when you look at the propositions.

So I'm going back to the 15/15/16/18 version, and will modify it.

I now suspect that negative relationships between analogies--
i.e. getting low values when the competing analogy is emphasized--
are crucial to getting the expected bias.  So I'm going to modify
to help produce that, i.e. so that in the barcharts, the contrary
analogue propns go negative and not just <= 0.
