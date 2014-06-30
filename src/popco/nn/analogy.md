analogy.clj
=======

An implementation of Holyoak & Thagard's (1989) ACME method
of constructing a constraint satisfaction network from two sets
of simple representations of entertained propositions.
A description of the requirements for the process implemented
here can be found in the following publication.  See especially
pages 10-11.

````
@Article{Abrams:ModerateRole,
  author =	{Abrams, Marshall},
  title =    {A Moderate Role for Cognitive Models in Agent-Based
             Modeling of Cultural Change},
  journal =	{Complex Adaptive Systems Modeling},
  year =	{2013},
  volume =	{1},
  number =	{16},
  month =	{September 13},
  url =	{http://www.casmodeling.com/content/1/1/16},
  doi =	{10.1186/2194-3206-1-16},
}
````

The original procedure on which this one is based can be found in
the following publication.  See especially page 314.

````
@Article{HolyoakThagard:AnalogMapConstraintSat,
  author =	{Holyoak, Keith J. and Thagard, Paul},
  title =	{Analogical Mapping by Constraint Satisfaction},
  journal =	{Cognitive Science},
  year =	{1989},
  volume =	{13},
  pages =	{295-355},
}
````

Note that the current way in which the network's weight matrix and 
associated linear data structures are constructed is not designed
to be fast.  It's designed so that naturally distinguishable steps
of the process are separated, so that the overall shape of the
process is easier to understand.  At present, the plan is that
the functions in this file will only be run once at the beginning
of a simulation, so there's no need for speed.  If this assumption
changes, the internal processes here may need to be redesigned.

Note: The fields defined in an AnalogNet contain redundant information,
but it's all information that can be precomputed at initialization time,
and that will only be read during simulation run time.  Having the
redundant, precomputed fields makes runtime code simple and efficient.

STEPS:

Some of this may be obsolete.

1. Pair isomorphic propositions.

2. Also pair up components of propositions in pairs from step 1.
   (These elements, and propositions, are called "LOT elements"
   or "LOT items", because they are part of POPCO persons'
   simplified language of thought.)
   The result is a tree structure, since some pairings result from
   the fact that propositions' arguments are paired.  The tree
   structure preserves all of the relationships between data, and
   will be processed in different ways in subsequent steps.

3. Make flat data structures containing all such pairings, which 
   representing unique "map nodes", whose activation values will 
   represent the plausibility of various pairings within an analogy.
   (Although the vector of pairings is flat, the proposition data
   in it preserves all of the internal structure of these
   propositions, including propositions embedded in them.  This
   internal structure will probably have no function in the code
   after the process implemented in this file, but it will be
   useful to have the information available to an investigator
   running a simulation.)

4. Make a square matrix, or rather two matrices, representing neural
   network link weights that will be used to settle the "analogy
   network" of the map nodes.  The two matrices can be viewed as a
   logical single matrix, but since this matrix will typically be
   created only once for a given simulation, and since we'll always 
   need to treat the positive links and negative links separately
   in the Grossberg algorithm used to settle the network, two separate
   matrices are created instead.  (The function `wt-mat`, defined in
   nn/core.clj, can be used to return the single matrix which is the sum
   of the two hardcoded matrices.)  In particular, the two matrices:

    A. Assign positive weights between all map nodes resulting from
       each paired proposition.  Where there are multiple pairings
       (due to propositions appearing as arguments to propositions),
       there will be additional weight assigned.  (Here we use the
       clustering of map nodes by proposition embedded in the result of 
       step 2.)
    
    B. Assign negative weights between any two map nodes that share
       a "side", i.e. that share a LOT element that's paired in each.  Again,
       it can occur that there are multiple instances of competition in
       this sense, creating a weight whose value is farther from zero.

The function `make-analogy-net` wraps up all of these steps, returning
the result in a Clojure AnalogyNet record (defined in nn/core.clj`) with 
these elements: 

* The two `clojure.core.matrix`s described above.

* A standard Clojure vector of maps, each containing information about
  the two LOT elements that are paired in an ACME map node.  These maps
  also contain an id for the map node.  The order of this information
  will correspond to the order of activation values in a
  clojure.core.matrix vector representing node activation values.  The
  order will also, therefore, correspond to the order of rows and
  columns in the weight matrix.

* A Clojure map from ids of each of the map nodes to their indexes in
  the vector just mentioned, allowing reverse lookup of indexes from
  map node ids.

* A Clojure map from pairs of ids of the paired elements that were the
  origin of each of the map nodes, to their indexes in the vector 
  mentioned above, allowing reverse lookup of indexes from pairs of 
  LOT element (see core/lot.clj) map node ids.

(This function might also return a matrix of 1's and 0's representing
the presence or absence of a link.  This isn't essential for the analogy
network, which has no weights of value zero, but will be needed for
the proposition network, which can have zero-valued weights.)


NOTE The way that `nn.settle/next-activns` does matrix multiplication
using `(mmul <matrix> <vector>)`, `<vector>` is 1D and is treated as a
column vector.  This means that the weight at index i,j represents the
directional link from node j to node i, since j is the column (input)
index, and i is the row index.  (This doesn't matter for symmetric
links, since for them there will be identical weights at i,j and j,i,
but it matters for assymetric, directional links.)

## A note on `+analogy-max-wt+`:

In popco1, I clipped analogy net weights to be less than or equal to 0.5, in order to reduce extreme cycling of activation values.

Here's the variable from POPCO 1, with its comment:

(defconstant +acme-max-weight+ .5L0) ; Used in make-symlink to tamp down on cyclic non-settling in analogy networks.  A bit of a kludge--should be reworked if POPCO starts using ECHO, for example.

This is used in make-symlink to cause make-link to not sum weights past 0.5.
I don't think this is an issue in the crime3 networks.  There is only one weight at 0.5,
from :Causal-if=Causal-if to :CB-vpp=B-abp (indexes 81 and 185, respectively)
(i.e. two weights, really--it's a symlink),
and it's 0.5 even in popco2 at this point, even though I don't believe I've got any
clamping to 0.5 yet.  So in crime3 there are apparently no weights that would naturally
exceed 0.5.  This was apparently an issue in the sanday network.

Note that though the comment says this is about analogy nets, it seems like it did 
the propn nets in popco1.  Ah, but links in popco were never that high.  They
don't sum.  Wait, not even links to SALIENT?  No: The links to SALIENT were
created by a call to raw-make-symlink in update-salient-link, in popco.lisp.
By calling raw-make-symlink with no optional arguments, we got the default
max of 1.0.  i.e. calling raw-make-symlink rather than make-symlink bypasses
make-symlink's clipping to +acme-max-weight+.  Therefore, if I want to clip
to weights 0.5 in the analogy net to damp oscillations, I can just apply
that rule in analogy.clj, and ignore the propn net.

SO: analogy.clj now has var `+analogy-max-wt+`, which is set to either 1.0
or 0.5, `and add-wts-to-mat!` prevents weights from exceeding this value.
