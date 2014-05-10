Numeric types in popco
=======

### Overview

By default popco2 uses the vectorz (i.e. vectorz-clj) implementation of
core.matrix.  vectorz only supports java.lang.Double numbers, which is
a good option in general.

However, for certain kinds of testing, it can be useful to switch to a
different numeric type.  To do this, you need to make sure that all
numbers used in data structures are of an appropriate type.  Doubles
anywhere are likely to spread through contagion.  In Clojure, if 
numbers of two types are combined, the result will have the rightmost
of those in the list below:
<pre>
Long,  BigInt,  Ratio,  BigDec,  Double
</pre>
Also note that a Ratio that resolves to an integer will turn into a BigInt.

### Switching numeric types

In order to switch from Doubles to something else, you need to modify
three things in the source:

1. In popco/core/popco.clj, comment out the first of these lines, and uncomment
the other one:
<pre>
    (mx/set-current-implementation :vectorz)
    ;(mx/set-current-implementation :ndarray)
</pre>
2. In popco/core/constants.clj, comment/uncomment lines so that all of
the constants are of the same numeric type.

3. There are three matrix*.clj files in popco/nn.  These define one or
two wrapper functions for core.matrix functions.  popco uses
matrix.clj, and you need to copy the appropriate matrix_*.clj file
into it.

**matrix_default.clj**
just defines popco names for the core.matrix functions.  There is no
change in behavior.  These are functions that will infect popco
with Doubles, so you don't want these versions if you're trying to use
a different numeric type.

**matrix_altnumbers.clj** wraps the core.matrix functions in code that
does something extra.  These are needed if you want to override the
default behavior of these functions.  Since they do extra work, they
should be a little bit slower than the original core.matrix functions,
so I don't want them for routine use.

*To go back to Doubles*, just undo the preceding, of course.


### How to keep this system working

To make this scheme work, you should consistently use the constants
from the file constants.clj.  Among other things, intead of using 1,
0, and -1, use +one+, +zero+, and +neg-one+.  This applies to the
files that specify particular simulations.


### Miscellaneous notes

Ratios/BigInts are gawdawful slow, after the first tick or so.  Expect
to wait at least 15 minutes to get from one tick to the next, with a
population of three persons.

BigDecimals may cause a non-terminating decimal expanation exception,
making them unusable.  This is an inherent difficult with BigDecimals:
Since they try to use as many decimal places as is necessary, any
number that requires an infinite number of decimal places (such as
1/3), won't be representable in BigDecimals.
