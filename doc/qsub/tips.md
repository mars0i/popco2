qsub tips for popco on cheaha
====

#### RAM on Cheaha:

Running 121 persons, 5000 ticks, 50 runs, with 50 propositions
(crime3), I can do the following with the qsub `-l vf=1.5G` flag, i.e. 
up to 1.5G RAM:

1. Convert csvs to a 4-D multi-run array.

2. Check for within-pop, within-domain disagreement at tick 5000.

3. Extract a dataframe from the last tick of the array.

With 100 runs, however, I can only do the first task with the
`vf=1.5G` setting.  Upping `vf` to 16GB allowed the second two to
succeed.  Not sure if I need all that, but it worked.

Note that you can override flags in a job script with command line
flags.


#### Combining arrays with `abind`:

Don't try to combine large arrays with `abind`.  At least I tried to
combine two 121-person 50-proposition 5000-tick 50-run arrays on
Cheaha, and didn't figure out how to do it.  First it bombed because
it wanted 11.5GB and couldn't get it, and then I asked for 32GB with
the `vf` flag and didn't succeed in the end.  It was taking forever.
I think I killed it.  It was easier to make the whole thing from the
original csvs, although that seems redundant.
