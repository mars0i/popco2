R
====

This directory contains R scripts, etc. for use with popco2.

R/R (redundantly) contains R source code.

R/popco contains an R package made from some of that source code.  The
file DESCRIPTION has to be in this directory to allow that package to
be loaded automatically when I start R.  I put these lines in my
.Rprofile file:

`library(lattice)`  
`library(devtools); load_all("~/p2/src/R/popco")`

If there are recent changes to the files under R/R, they won't be
reflected in the functions made available automatically in R until the
files are copied here and the package is updated.

R/shell contains shell scripts.

R/doc contains notes, etc.  Notes in this directory should be about
general usage and setup of R for popco.  For more detailed
documentation about use of the functions defined here see:

- doc/R off the main directory of the popco2 repository.
- Comments in the R source code for popco.
- The qsub directory in popco 1.


