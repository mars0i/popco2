src/popco/nn/
=======

Directory for neural net creation, settling, etc.  

There are two kinds of neural nets in POPCO: analogy networks, and
proposition networks, but they operate similarly.

NOTE: All uses of clojure.core.matrix/mset! should be confined to
general-purpose functions defined in popco/nn/nets.clj, and to
make-person in person.clj.  This allows experimenting with mset by
replacing functions in those files.  (For example, if you want to see
the effects of BigDecimals, as of 5/2014 the only core.matrix
implementation that fully supports them is persistent-vector, which
doesn't allow destructive operations.)
