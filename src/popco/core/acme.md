acme.clj
=======

An implementation of Holyoak & Thagard's (1989) ACME method
of constructing a constraint satisfaction network from two sets
of simple representations of entertained propositions.
A description of the requirements for the process implemented
here can be found in the following publication.  See especially
pages 10-11.

```latex
@Article{Abrams:ModerateRole,
  author =	{Abrams, Marshall},
  title =    {A Moderate Role for Cognitive Models in Agent-Based
             Modeling of Cultural Change},
  journal =	{Complex Adaptive Systems Modeling},
  year =	{2013},
  volume =	{1},
  number =	{16},
  pages =	{1--33},
  month =	{September 13},
  url =	{http://www.casmodeling.com/content/1/1/16},
  doi =	{10.1186/2194-3206-1-16},
}
```

The original procedure on which this one is based can be found in
the following publication.  See especially page 314.

```latex
@Article{HolyoakThagard:AnalogMapConstraintSat,
  author =	{Holyoak, Keith J. and Thagard, Paul},
  title =	{Analogical Mapping by Constraint Satisfaction},
  journal =	{Cognitive Science},
  year =	{1989},
  volume =	{13},
  pages =	{295-355},
}
```

Note that the current way in which the network's weight matrix and 
associated linear data structures are constructed is not designed
to be fast.  It's designed so that naturally distinguishable steps
of the process are separated, so that the overall shape of the
process is easier to understand.  At present, the plan is that
the functions in this file will only be run once at the beginning
of a simulation, so there's no need for speed.  If this assumption
changes, the internal processes here may need to be redesigned.

STEPS:

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

4. Make a square matrix representing neural network link weights
   that will be used to settle the "analogy network" of the map
   nodes:

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


The function `make-acme-nn-strus` wraps up all of these steps, returning
the result in a Clojure map with three elements: 

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

* A clojure.core.matrix weight matrix representing link weights in the
  analogy network.

(This function might also return a matrix of 1's and 0's representing
the presence or absence of a link.  This isn't essential for the analogy
network, which has no weights of value zero, but will be needed for
the proposition network, which can have zero-valued weights.)
