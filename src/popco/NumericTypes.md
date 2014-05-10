Numeric types in popco
=======

By default popco2 uses the vectorz (i.e. vectorz-clj) implementation of
core.matrix.  vectorz only supports java.lang.Double numbers, which is
a good option in general.

However, for certain kinds of testing, it can be useful to switch to a
different numeric type.  In order to do this, you need to modify three things 
in the source:

1. In popco/core/popco.clj, comment out the first of these lines, and uncomment
the other one:

<pre>
    (mx/set-current-implementation :vectorz)
    ;(mx/set-current-implementation :ndarray)
</pre>

2. In popco/core/constants.clj, comment/uncomment lines so that all of
the constants are of the same numeric type.

