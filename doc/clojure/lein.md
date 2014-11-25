Notes on Leiningen relevant to popco2
====

I normally experiment using 'lein repl', which does a kind of load of
src/popco/core/popco.clj, and then I load additional files using
`require`, `use`, `load-file`, or my function `unlocknload`, which
runs `load-file` and `use`.

At some points I've used 'lein uberjar' to create a jar file that can be
run using 'java -jar ...'.  Running this performs the command line
processing in the second half of src/popco/core/popco.clj.

'lein run' also performs the same command line processing.  This is
better method, since, if I forget to recompiled the jar file, it will
do it automatically.

Each of the three methods above will perform an up front compilation of
parts of popco if needed because I currently (11/2014) have `:aot
[popco.core.popco]` in project.clj, and/or because I have `:gen-class`
in popco.clj, I think.  The rest of the source code gets compiled as
loaded, normally, I think.  I'm not entirely clear on why certain things
get precompiled.  You can perform the compilation in advance with 'lein
compile'.  The resulting files are put in the 'target' directory, which
you can remove to have a fresh start using 'lein clean'.

When the stuff is recompiled before popco starts, constants.clj gets
recompiled, which prints a new random seed to stdout, and a
restoreRNG*.clj file is generated.  These are files that allow one to
run again using the same seed.

Then when popco actually runs, this file is loaded again, and a
different random seed is printed to stdout.  This is the one actually
used in the popco run.  I also use the seed in some filenames when I
write out data.  Of course, the number used in the filename will be the
one from the second seed displayed, when there was a recompilation
beforehand.
