src/popco/core/
=======

Directory for starting points, central functions, and things that
don't fit elsewhere.

**popco.clj**

Main entry point for the entire simulations framework.

**communic.clj**

Functions for transmitting propositions/beliefs between persons,
adding (unmasking) them in proposition neural nets and analogy neural
nets, etc.  Note some of these functions may also be used in the
creation of Persons, so they aren't only relevant to interpersonal
transmission.

**person.clj**

Definitions and routines for creating and accessing fields in Persons.

**lot.clj**

Definitions and functions for "Language Of Thought" elements: i.e.
Propositions, Predicates, and Objects.

