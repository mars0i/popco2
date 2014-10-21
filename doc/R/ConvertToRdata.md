Convert propn activn CSVs to R array
====

How to Convert popco proposition activation CSV files representing
several runs of the same simulation, and then convert the CSV data into
an R array:

Instructions assumes working on the UAB computing cluster--Cheaha--but
can be modified for other situations.


### Part I: Creating the data

Run several simulations using the same parameters on Cheaha using
qsub.  For example:

`qsub -t 1-100 ~/p2/src/qsub/submitanything.job \`  
`popco -n sims.crime3.threegroups \`  
`-r '(rp/write-propn-activns-csv (take 5000
(mn/many-times sim/popn)) :cooker rp/cook-name-for-R)'`

The first line says we're submiting 100 instances of the same
simulation, which will be numbered 1-100 and won't have a special name.

The second line says to run `popco`, a shell script that is supposed to
run  
`java -jar ~/p2/target/<latest version of a standalone jar file>` .  
(So I have to keep the popco script up to date, or do something
fancier inside it.)

The `-n sims.crime3.threegroups` says to `require` that namespace, in
which an initial population should be defined and assigned to variable
`popn`.  This will then be available via the `sims` namespace alias.
(src/popco/core/popco.clj sets this up).

The last line specifies the Clojure commands to run.  In this case, we
run 5000 timesteps of popco starting from the initial population, and
then pass it to a csv-writing function, along with a function that
will rewrite variable names so that columns in the csv file won't
confuse R.

By default, popco will write csv file output to directory data from
wherever popco is running.  On Cheaha, I have the "data" dir under my
popco2 git repository aliased to my data dir on the Cheaha scratch
filesystem.

<small>(You can do the same thing on a system that doesn't have qsub to
submit batch jobs by simply running everything starting with `popco` as
many times as you want.  Note that `many-times` will try to split
processing between multiple CPU cores.  Popco will use roughly `<number
of persons>/32` cores, plus the extra cores that the JVM uses for GC,
etc..  You can prevent this behavior by using `unparalleled-many-times`
instead.)</small>


### Part II: Converting the data into an R array

On Cheaha, I have `data` aliased to my data directory on the scratch
filesystem.  This is also where csv files will have been written in
Part I.  Do this to convert the csv files to a 4-D R array, and then
write it to an rdata file (which is compressed):

`cd ~/data`  

`~/p2/src/qsub/submitRead2MultirunRA.sh crime`

The second line will submit a batch job to the cluster that will
eventually create an rdata file crime.rdata, in which
each csv file is treated as different run.  Replace "crime" with
whatever you want.

submitRead2MultirunRA.sh uses functions defined in src/R/R/df2ra.R.

You'll usually want to `scp` the file back to your own machine once
it's done, possibly deleting the data files on Cheaha.  For example:

`scp mabrams@cheaha.uabgrid.uab.edu:data/crime.rdata  .`


### Part III: R sanity checks

In R, do:

`load("crime.rdata")`

You can use R's `ls()` to see that you now have a variable called
`crime`.  You can check that it has the structure you expect with these
commands:

`class(crime)` (should display "array")  
`dim(crime)`  (should display numbers of persons, propositions, ticks, and runs)  
`dimnames(crime)`  (should display labels within each of the four dimensions)  

Or if you don't want to see all of the row/column/etc. names at once,
use:

`dimnames(crime)$person`   
`dimnames(crime)$proposition`   
`dimnames(crime)$tick`   
`dimnames(crime)$run`  
or  
`dimnames(crime)[i]`  
where `i` is 1, 2, 3, or 4.

Or since you may want to see all of this information except the tick names (which
are just number strings):

`dimnames(crime)[-3]`


In some contexts, my assumption is that within each isolated
subpopulation in each run, everyone pretty much has the same degree of
belief for every belief, except for any pundits.  To test this
assumption, run the function `findRunsWithDisagreement`, defined in
df2ra.R.  For example, if everyone in the population communicates freely
with everyone else, except for a single pundit named "aa" who's the
first person in the array, you can use this:

`findRunsWithDisagreement(crime[-1,,,], 0.01)`

If this returns `character(0)`, it means that in every run, all members'
beliefs are within 0.01 of each other.  If any runs don't satisfy this
requirement, they'll be listed as output, and you can investigate
further.

If you have three 40-person subpopulations that you think will have
converged, you can do this:

`findRunsWithDisagreement(crime[2:41,,,], 0.01)`  
`findRunsWithDisagreement(crime[42:81,,,], 0.01)`  
`findRunsWithDisagreement(crime[82:121,,,], 0.01)`

If you want to check something other than the last timestep (tick),
which `findRunsWithDisagreement` selects by default, you can enter a
tick as the last index for the crime vector.

If you've got a lot of data, you might want to run this on Cheaha.  There are some
example scripts in the popco1 repo (i.e. popco) in the qsub directory.
