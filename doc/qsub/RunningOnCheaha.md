Running popco on the Cheaha cluster
====

See also docs/R.


Here's an example of how to submit all of the steps of a job at once on the Cheaha cluster.

##### Step 1:

First run the popco simulations.  This submits 100 simulation runs with
5000 ticks each using the `bali.nobias2pundits` model.  `-N sim` is a
name that will be displayed by `qstat`.

	qsub -N sim -t 1-100 ~/p2/src/qsub/submitanything.job popco -n sims.bali.nobias2pundits -r '(rp/write-propn-activns-csv-for-R (take 5000 (mn/many-times sim/popn)))'

##### Step 2:

Now we'll submit a job that will wait until the previous jobs are done,
then creating a 4-D array named `bali` in R from the 100 output data
files, saving the array into a file named *bali.rdata*.  

Run `qstat` and see what the job id is for the jobs you just submitted.
Replace the number at the end of the next command line with that number:

	qsub -N csv2r ~/p2/src/qsub/read2multirunRA.job bali 9869485

##### Step 3:

Next we'll submit a script that will start two jobs when the R datafile
has been created.  One of those jobs will check to see whether, within a
given run, the activation of proposition $P$ is almost the same as the
activation of that proposition in every other non-pundit person in the
run.  (This is what's supposed to happen after many ticks.)  The other
job creates a dataframe containing average proposition activations from
the last tick of each run.  Note that these scripts have to be
coordinated to the layout of persons in the model: They must be
customized to the number of pundits and to bias groups within the
population.

Again, run `qstat` and see what the job id is for the jobs you just submitted.
Replace the number at the end of the next command line with that number:

	qsub -N ready ~/p2/src/qsub/submitanything.job ~/p2/src/qsub/createdataframeBaliWhenReady2pundits.sh 9869487

