How to generate analogy-bias plots
====

Steps that I would commonly use to generate scatter plots representing
analogy biases.  Many of the steps below would be used for other
purposes as well.


### Part I: Creating the data, convert to R data

See <a href="ConvertToRdata.md">ConvertToRdata.md</a>.


### Part II: Create dataframe

R's Lattice Graphics package often works only with R dataframes, so we
need generate an R dataframe containing means of activations for each
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

### Part III: Create plots

#### Scatter plots

Then run a command such as this (defined in src/R/R/popcoplots.R):

Relatively simple version:

`xyMeanActivnPlot(CV~CB|bias, groups=rawsum, data=crime3.df[crime3.df$bias!="none",], auto.key=list(columns=2, space="bottom"), layout=c(3,1), par.settings=simpleTheme(col=c("red", "blue1")), pch=c(4,21), cex=c(1.25, 1))`

With jitter, lines at points around which jitter occurs, fancier labels, etc:

`xyMeanActivnPlot(CV~CB|bias, groups=rawsum, data=crime3.df[crime3.df$bias!="none",], xfoci=cb.foci, yfoci=cv.foci, auto.key=list(columns=2, space="bottom"), layout=c(3,1), main="Mean activations of \"capture\" vs. \"isolate\" beliefs\n for 3 analogy biases at time step 5000 in 50 simulation runs", xlab="capture\n", ylab="isolate", jitter.x=T, jitter.y=T, amount=.03, par.settings=simpleTheme(col=c("red", "blue1")), pch=c(4,1), cex=c(1.25, 1))`

The three jitter parameters add random noise so that you can see
overlapping circles (or whatever you're using as points).  NOTE: Noise
is also added to the means, so if you add a lot of jitter, the means
can move substantially.  `amount=0.03` is not a lot of jitter, but
it's enough if you're using unfilled shapes.

About "foci": When activations in a group of persons all converge to the
same values, they don't go all the way to 1 and -1.  They get close--0.9
or 0.89 or so.  So the average activations aren't clustering on default
grid lines.  But when you add jitter, it's nice to see the grid lines,
so you can see where the values are supposed to be--where they are
departing from.  And it's also nice to know where the values are
clustering, even if you don't add jitter.  So, partly by trial and
error, I create two lists of numbers at regularly-spaced intervals that
reflect, roughly, where the clustering is.  These are cv.foci and
cb.foci, and for example for crime3, they're stored in foci.rdata.
These are then passed as `xfoci` and `yfoci`, which can be used by
`xyMeanActivnPlot`.

You can use `pch=(4, 21)` instead of `pch=(4, 1)` to get filled circles (i.e. 21 rather than 1).

To create a PDF file, precede the line above with `pdf()`, and follow
it with `dev.off()`.  This will create a file named Rplots.pdf, which
you can rename.

Optionally, you can also set up other parameters of the plot before
running `xyMeanActivnPlot`.  For example, I like to changes the color of
the bars at the top of each plot to light blue so that the bars will be
apparent against the beige background that I often use in presentations:

`sb <- trellis.par.get("strip.background")`  
`sb[["col"]][1] <- "lightblue"`  
`trellis.par.set("strip.background", sb)`  


#### Box/Hanoi plots

See src/R/examples/crime3hanoi.R for an example of how to create box
plots with underlying Hanoi plots.  A  [Hanoi
plot](http://stackoverflow.com/questions/15846873/symmetrical-violin-plot-like-histogram/15893422#15893422)
is something I seem to have invented, which displays the actual
distribution of data symmetrically along a vertical axis.  It's a kind
of histogram that's like a [violin
plot](http://en.wikipedia.org/wiki/Violin_plot), but without smoothed
curves over discrete or binned data.  My Lattice Graphics implementation
is in src/R/popco/R/panel.hanoi.R, and is based on [suggestions by Greg
Snow](http://stackoverflow.com/a/15852613/1455243).
