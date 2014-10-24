qsub tips
====

#### RAM on Cheaha

Running 121 persons, 5000 ticks, 50 runs, with 50 propositions
(crime3), I can do the following with the qsub `-l vf=1.5G` flag, i.e. 
up to 1.5G RAM:

1. Convert csvs to a 4-D multi-run array.

2. Check for within-pop, within-domain disagreement at tick 5000.

3. Extract a dataframe from the last tick of the array.

With 100 runs, however, I can only do the first task with the
`vf=1.5G` setting.  Upping `vf` to 16GB allowed the second two to
succeed.  Not sure if I need all that, but it worked.
