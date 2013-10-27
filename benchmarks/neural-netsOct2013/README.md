# benchmarks/neuralnetsOct2013

In Oct. 2013 I started exploring the possibility of rewriting my
POPCO simulation framework in Clojure.  (Currently it's in Common Lisp.
Parts of the code are messy, and parts are inefficient.)

This directory contains a first version of the network settling routine
that's central to POPCO, and benchmark times for it generated with
Criterium using the four current implementations of core.matrix, on two
different machines.

All tests were run with Clojure 1.5.0.

I ran the tests with the persistent-vector, ndarray, and
vectorz implementations from a core.matrix project 0.12.1-SNAPSHOT
downloaded on October 23, 2013.

I ran tests on the clatrix implementation from a clatrix project
0.3.0-SNAPSHOT with the "Core matrix 0.12.0 support" fixes that Mike
Anderson added on Oct. 19.  This fixed a bug in emap.

I ran all tests two computers, a MacBook Pro and a MacBook Air, both
bought new in 2011, I think.  (See below for more details.)

For each implementation and machine, I ran two tests with three
different parameter settings (different input vector sizes).

The tests involve running one cycle of settling a single-layer neural
network.  This roughly means multiplying a vector representing node
activations with a matrix representing weights of links between nodes,
once.  However, there are additional details to the algorithm I'm using,
which was published by Grossberg in 1978.

The three parameter settings I used are three different numbers of
nodes, represented by the length of the input and output vectors: 250,
500, and 1000.  This means that the weight vector is a square matrix
with sides of the same size.

The Grossberg algorithm, at least in the straightforward way that I
coded it, requires that positively-weighted links and negatively
weighted links be treated separately.  I deal with this by creating
two matrices from the main weight matrix, by emap-ing a function that
turns some nonzero weights into zeros.  

In POPCO, the agent-based simulation framework in which I want to use
this settling algorithm, there are two cases.  In one kind of situation,
the weight matrix is fixed once and for all at the beginning of the
simulation run.  In that case, I only need to extract the positive and
negative weight matrices once, and then reuse them in all subsequent
settling iterations.  In the other situation, weight matrices can
change due to communication between agents.  In that case, I may have to
create new positive and negative weight matrices whenever the main
weight matrix changes.

The two different tests in this set of benchmark runs differ as
to whether they include the step of emap'ing through the main weight
matrix to create the positive and negative weight matrices.

For these tests, I used randomly generated input activation vectors,
and weight matrices randomly generated so that roughly 0.035 of the
links (matrix elements) are nonzero.  This makes the weight matrices
a little bit like the ones that I actually use.

# Files and directories:

neural-nets-benchtimes.xlsx contains mean times reported by criterium
from all of the trials.  (There were a few outliers, but for the most
part the times for repeated runs of the same configuration were pretty
close together, so I feel OK about just using the means.  Full details
of what criterium reported can be found in transcript files in the MBP
and MBA directories.)

MBP: 

Terminal transcripts of data reported by criterium for trials on a
MacBook Pro, 2.3 GHz Intel Core i7, 8GB RAM, running OS X 10.6.8 (a quad
core, 8 simultaneous thread CPU).

MBA:

Terminal transcripts of data reported by criterium for trials on a
MacBook Air, 1.6 GHz Intel Core 2 Duo, 4GB RAM, also running OS X 10.6.8
(dual core, 4 threads).

config:

Source files run, and project.clj files from the two project
directories mentioned above.
