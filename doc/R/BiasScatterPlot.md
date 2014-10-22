How to generate analogy-bias scatter plots
====

Steps that I would commonly use to generate scatter plots representing
analogy biases.  Many of the steps below would be used for other
purposes as well.


### Part I: Creating the data, convert to R data

See <a href="ConvertToRdata.md">ConvertToRdata.md</a>.


### Part IV: Generating plots, etc.

First generate an R dataframe containing means of activations for each
proposition across all members of each proposition using
`multiRAs2combinedMeanDF`, which does the following:

Given a list [i.e. with `list()`, not `c()`] of multi-run arrays, and a
list or vector of strings to use as names of the bias of each array, and
two prefix strings for propositions, calls `multiRA2meanDF` repeatedly
on the arrays using the two prefix strings, and then combines the
 resulting dataframes into one dataframe by passing them along with the
 vector/list of bias strings to combineMeanDFsWithBiases.

Examples:  

`mra.df <- multiRAs2combinedMeanDF(list(mra1, mra2, mra3), c("virus", "beast", "both"), "CV", "CB")`  

`crime3.df <- multiRAs2combinedMeanDF(list(crime3[2:41,,,], crime3[42:81,,,], crime3[82:121,,,]), c("virus", "beast", "both"), "CV", "CB")`

(Or see comments at the top of df2ra.R for more fine-grained processing.)

Note that the number of elements in the first list and the second
c-vector should be the same.

Now you can examine the resulting dataframe using `names(crime3.df)` or
by displaying its contents.

You probably should save the dataframe to a file, e.g. like this:

`save(crime3.df, file="crime3df.rdata")`

Then run a command such as this (defined in src/R/R/popcoplots.R):

`xyMeanActivnPlot(CV~CB|bias, groups=rawsum, data=crime3.df[crime3.df$bias!="none",], xfoci=cb.foci, yfoci=cv.foci, auto.key=list(columns=2, space="bottom"), layout=c(3,1), main="Mean activations of \"capture\" vs. \"isolate\" beliefs\n for 3 analogy biases at time step 5000 in 50 simulation runs", xlab="capture\n", ylab="isolate", jitter.x=T, jitter.y=T, amount=.03, par.settings=simpleTheme(col=c("red", "blue1")), pch=c(4,1), cex=c(1.25, 1))`


To create a PDF file, precede the line above with `pdf()`, and follow
it with `dev.off()`.  This will create a file named Rplots.pdf, which
you can rename.

Optionally, you can also set up other parameters of the plot before
running `xyMeanActivnPlot`.  For example,
this changes the color of the bars at the top of each plot to light
blue:

`sb <- trellis.par.get("strip.background")`  
`sb[["col"]][1] <- "lightblue"`  
`trellis.par.set("strip.background", sb)`  
