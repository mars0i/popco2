Notes on update.clj
=======

### Overview

update.clj contains functions for updating neural networks in popco.

"Update" in this sense usually means changing activation values of
nodes, which means updating a core.matrix vector containing those
activation values.  There are matrices that are used to calculate these
activation values from the activation values on the previous timestep
(tick).  The matrices represent weights on links between the nodes
whose activation values are represented in the vector.

However, popco2 also updates link weights in the proposition network in
each tick--i.e. the proposition weight matrix is changed.  Functions for
this purpose are in update.clj as well.

Link weights in the analogy network aren't changed after initialization.

Whether a node "exists" is governed by the analogy and proposition mask vectors.
See person.clj, propn.clj, analogy.clj, and communic.clj.


### Directional links

The matrix multiplication in the main network settling fucntion,
`next-activns`,  uses matrix multiplication in the form `(mmul
<matrix> <vector>)`. Here `<vector>` is 1D and is treated as a column
vector.  This means that the weight at index i,j represents the
directional link from node j to node i, since j is the column (input)
index, and i is the row index.  Doesn't matter for symmetric links,
since for the there will be identical weights at i,j and j,i, but
matters for assymetric, directional links. 

For example, to cause the 0-index, SEMANTIC or SALIENT node to send input to
other nodes, but to never receive inputs, there should be nonzero
weights in column 0 but not row 0, and this is how the weight matrix
should be set up. i.e. if the index of the other node is i, the
indexes should be i,0.

However: `next-activns` would still change the SEMANTIC node over time,
because it decays all nodes, wehther they get input or not.  To undo
this, we put a special value > 1.0 in the analogy mask in `make-person`,
i.e. 1/decay, that will undo the decay.  (This is a kludge that is split
between two different files.  Maybe there's a better way.)


### Some notes on core.matrix functions

Note the distinction in clojure.core.matrix between:

`emul`: Multiply together corresponding elements of matrices,
which should have the same shape.

`mul`:  Same as emul.

`mmul`: Regular matrice multiplication A * B:

`emul`: (see above) each row i of A with each column j of B,
summing the result each time to produce element <i,j> of
the result matrix.  (For vectors, this is inner product,
with A as a row vector and B as a column vector.)

Also:

`emap`: Maps a function over each element of a matrix to produce a new
matrix.

In any core.matrix implementation, there are three kinds of 1-D vectors:

n-element vectors, which have shape = [n], and can be treated as column or
row vectors during multiplication, depending on the order of arguments.
They can be created by e.g. `(matrix [1 2 3])`.
POPCO vectors are typically of this kind.

True row vectors, which have shape = [1 n], and which can be created
e.g. by `(matrix [[1 2 3]])`.

True column vectors, which have shape = [n 1], and which can be created
e.g. by `(matrix [[1] [2] [3]])`.


### Network settling (with Grossberg algorithm)

The Grossberg algorithm implemented by `next-activns` does no
normalization in anything like the probabilistic sense: That is, outputs
are outputs; they're not scaled relative to other signals coming into
the same node:  The only "averaging" comes from the weighting across
links due to the network, and the scaling by distance from max and min.
Moreover, the link weights are absolute numbers; they are not themselves
scaled relative to other link weights.  (In essence, it's the job of the
neural net settling process to do something analogous to probabilistic
normalization.)

(Qualification: For the proposition network there is a kludgey method of
scaling negative links relative to positive links since there are more
negative links: We just give the negative links a lower abs weight.)

WHAT THIS MEANS is that you can effectively "remove" links from the 
network simply by masking the input vector, forcing some values to zero.
If a node sends no activation over the wires, then it's just as if links
from that node had weight zero--i.e. as if these links didn't exist.
Nothing *else* about the network needs to change.

AND this means that since the analogy network has certain links to a 
map node if and only if the node exists, and never adds or removes links
to a map node once it exists, we can create the analogy network once,
including all map nodes possible given the set of propositions in the
two analog structures.  Then we can "remove" map nodes and their
links from the network simply by zeroing the map node activations in
the input vector.  Or rather, we can add map nodes *and their links*
to the analogy network simply by beginning to put nonzero activations 
in the corresponding map nodes.  In this way, different persons can
have effectively different analogy networks, corresponding to their
different repertoires of propositions, while using a single set of
analogy network weight matrices (which needn't slow down run time
due to being modified later).

HOWEVER, the masking must occur on *every* iteration of next-activn, to
prevent output into "nonexistent" nodes from the previous iteration
having an effect on the next iteration.

AND this means that you can't pre-settle the analogy network activations
once and for all and then unmask them as propositions are added, because
that would mean that activations of unmasked nodes would have been
affected by activations of nodes that were supposed to be masked.  So
even if it turns out that the analogy net doesn't *always* have to go
through a resettling process (questionable, because of cycling), it will
have to be resettled whenever beliefs are added to a person.

Also, because the settling algorithm is a bit complex, I don't think we can
take the shortcut of simply multipling the weight matrix by itself many
times.  (Maybe this is incorrect, though.)


Note: The proposition networks, by contrast, can't be treated this way,
because they acquire new links between old nodes.


### References

1. network.lisp in POPCO, which is based on Thagard's ACME network.lisp.

2. "HT": Holyoak & Thagard 1989, "Analogue Retrieval by Constraint
Satisfaction", Cognitive Science 13, pp. 295-355. See pp. 313, 315.

3. "MR": Abrams 2013, "A Moderate Role for Cognitive Models in Agent-Based 
Modeling of Cultural Change", Complex Adaptive Systems Modeling 2013.

4. Grossberg. S. (1978). A theory of visual coding, memory, and development.
In E.L.J. Leeuwenberg & H.F.J. Buffart (Eds.), Formal theories of visual 
perception. New York: Wiley.
