How to generate analogy-bias scatter plots
====

Steps that I would commonly use to do this.


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
`java -jar ~/p2/target/<latest version of a standalone jar file>`  
(so I have to keep the popco script up to date, or do something
fancier inside it).

The `-n sims.crime3.threegroups` says to `require` that namespace, in
which an initial population should be defined and assigned to variable
`popn`.  This will then be available via the `sims` namespace alias.
(src/popco/core/popco.clj sets this up).

The last line specifies the Clojure commands to run.  In this case, we
run 5000 timesteps of popco starting from the initial population, and
then pass it to a csv-writing function, along with a function that
will rewrite variable names so that columns in the csv file won't
confuse R.

### Part II: Converting the data into an R array (& sanity checks)

cd ~/data
~/p2/src/qsub/submitRead2MultirunRA.sh yo

### Part III: Generating plots, etc.
