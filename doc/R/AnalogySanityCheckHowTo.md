How to perform a basic sanity check of analogies
====

1. Once you've defined propositions, semantic rules, etc., make a
   population specification file in which there are (for example) four
   persons.  Specify that none communicate at all, that all have all
   of the propositions.  Suppose that in each of the two analogs, there
   are two parts, representing two ways that the analogy could settle.
   This is how I set up the Sanday analogies and the crime analogies.

	Example file: *src/sims/crime/hermits.clj*.  ("hermits" is a good
	name for this kind of file, since no one communicates.  Each
	person is lost in its own perceptions and thought processes.)

2. Next, create a csv file of proposition activations in a format
   suitble for reading into R.  Here is code that does that with
   the population defined in crime/hermits.clj:

````clojure
(unlocknload 'sims.crime3.hermits)
(rpt/write-propn-activns-csv (take 100 (many-times popn)) :cooker csv/cook-name-for-R)
````

That last step creates a csv file under directory *data* with a name
based on the current session id.  You can pull this into Excel to make
sure it looks OK.  I chose 100 ticks because in past experiments,
without communication, the proposition networks settle long before tick
100.


3. Use the R function `read2multirunRA` defined in *src/R/R/df2ra.R* to
load the csv file into an R dataframe.  For example, after I renamed the
csv file created in the above code to "hermits.csv", I ran this in R
after making sure that the current directory in R was *data*:

````R
hermits <- read2multirunRA("hermits.csv")
````

You might want to check that this came out as expected: `dim(hermits)`,
`dimnames(hermits)`, etc.

Then, to create a bar chart showing the effects of the analogies, run the function 
`activnsAtTickBarchart` defined in *src/R/R/popcoplots.R*, for example like this:

````R
activnsAtTickBarchart(hermits, 100)
````

which will display the activation values for each proposition for the
four members of the population at tick 100.  Experiment with different
tick numbers instead of 100 to see the process of settling.

Note that some of what `activnsAtTickBarchart` does depens on having
chosen proposition names appropriately.  In particular, different groups
of propositions corresponding to different parts of analogies should
have different prefixes.  The function looks for such as scheme.

If you want to create a PDF file of the plot, with a more informative upper title,
you can do something like this now:

````R
pdf("hermitsTick100activns.pdf")
activnsAtTickBarchart(hermits, 100, main="crime/hermits.clj tick 100 activations")
dev.off()
````
