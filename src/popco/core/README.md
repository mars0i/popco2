src/popco/core/
=======

Directory for starting points, central functions, and things that
don't fit elsewhere.

**popco.clj**

Main entry point for the entire simulations framework.

**main.clj**

Contains top-level functions (e.g. `once`, `many-times`) for running a
simulation from a definition of a population.  (Populations are defined
using functions in populations.clj in files under src/sims .)

**reports.clj**

One-stop shopping for population information access: Aliases to
functions that can be used to get data from a time series of
populations.

**populations.clj**

Functions for defining and accessing population structures.  A
population structure contains the state of a population of persons at a
tick.

**communic.clj**

Functions for transmitting propositions/beliefs between persons,
adding (unmasking) them in proposition neural nets and analogy neural
nets, etc.  Note some of these functions may also be used in the
creation of Persons, so they aren't only relevant to interpersonal
transmission.

**person.clj**

Definitions and routines for creating and accessing fields in Persons.
See also ../nn/nets.clj, ../nn/analogy.clj, ../nn/propn.clj.

**lot.clj**

Definitions and functions for "Language Of Thought" elements: i.e.
Propositions, Predicates, and Objects.

