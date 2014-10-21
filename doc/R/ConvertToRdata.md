How to generate analogy-bias scatter plots
====

Steps that I would commonly use to generate scatter plots representing
analogy biases.  Many of the steps below would be used for other
purposes as well.

Part I and Part II assume that I'm working on the UAB computing
cluster, Cheaha, but they don't require that.


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
command.

`class(crime)`  
`dim(crime)`  
`dimnames(crime)`  

Or if you don't want to see all of the row/column/etc. names at once,
use:

`dimnames(crime)$person`   
`dimnames(crime)$proposition`   
`dimnames(crime)$tick`   
`dimnames(crime)$run`  
or  
`dimnames(crime)[i]`  
where `i` is 1, 2, 3, or 4.

For some scatter plots, my assumption is that within each isolated
subpopulation in each run, everyone pretty much has the same degree of
belief for every belief, except for any pundits.  To test this
assumption, run the function `findRunsWithDisagreement`, defined in
df2ra.R.  For example, if everyone in the population communicates
freely with everyone else, except for a single pundit named "aa"
who's the first person in the array, you can use this:

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

If you've got a lot of data, you might want to run this on Cheaha.  There are some
example scripts in the popco1 repo (i.e. popco) in the qsub directory.


### Part IV: Generating plots, etc.

First generate an R dataframe containing means of activations for each
proposition across all members of each proposition using
`multiRAs2combinedMeanDF`, which does the following:

Given a list [i.e. with `list()`, not `c()`] of multi-run arrays, and a list or vector of strings
to use as names of the bias of each array, and two prefix strings for propositions, calls
`multiRA2meanDF` repeatedly on the arrays using the two prefix strings, and then combines the
 resulting dataframes into one dataframe by passing them along with the vector/list of bias strings 
 to combineMeanDFsWithBiases.

Examples:  

`mra.df <- multiRAs2combinedMeanDF(list(mra1, mra2, mra3), c("virus", "beast", "both"), "CV", "CB")`  

`crime.df <- multiRAs2combinedMeanDF(list(crime[2:41,,,], crime[42:81,,,], crime[82:121,,,]), c("beast", "virus", "both"), "CV", "CB")`

(Or see comments at the top of df2ra.R for more fine-grained processing.)

Note that the number of elements in the first list and the second
c-vector should be the same.

Now you can examine the resulting dataframe using `names(crime.df)` or
by displaying its contents.

You should save the data file. ADD: **FIXME**

**FIXME:**

`sb <- trellis.par.get("strip.background")`  
`sb[["col"]][1] <- "lightblue"`  
`trellis.par.set("strip.background", sb)`  
`pdf()`  
`xyMeanActivnPlot(CV~CB|bias, groups=rawsum, data=crime.df[crime.df$bias!="none",], xfoci=cb.foci, yfoci=cv.foci, auto.key=list(columns=2, space="bottom"), layout=c(3,1),
main="Mean activations of \"capture\" vs. \"isolate\" beliefs\n for 3 analogy biases at time step 5000 in 50 simulation runs", xlab="capture\n",
ylab="isolate", jitter.x=T, jitter.y=T, amount=.03, par.settings=simpleTheme(col=c("red", "blue1")), pch=c(4,1), cex=c(1.25, 1))`  
`dev.off()`  
