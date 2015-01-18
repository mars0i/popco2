How popco2 differs from popco1
====

0. The biggest differences between popco2 and popco1 are that popco2 is
written in Clojure rather than Common Lisp, and that popco2 uses a
matrix representation of connectionist/neural networks, whereas popco1
represented nodes and links as individual data structures.  Also,
Clojure and Common Lisp floating point numbers are different, and
this, along with difference in random number generation algorithms,
can cause differences in outcome in individual runs because there can
be sensitive dependence to exact values of propositions at certain
points in the evolution of a population when communication is
occurring.  However, these are not significant differences in
functionality.

1. The order of tasks in the main loop differs.  See the "Moderate
   Role" paper or popco.lisp in popco1 for the order in popco1.  In
   popco2, see `popco.core.main/once` and functions called there, such
   as `popco.nn.update/update-person-nets`.

2. Popco1 uses .99 as the max activation value, and -.99 for the min
   value.  This derives from Thagard's code.  In popco2, the max and
   min are 1 and -1, respectively.
