multiRA2PersonPropnDF <-
function(mra, interval=1) {
  dnames <- dimnames(mra)
  persons <- dnames[[1]]
  propns <- dnames[[2]]
  ticks <- dnames[[3]]
  runs <- dnames[[4]]

  domsInPropnOrder<- sub("_.*", "", propns) # list of domain names in the same order as propns they came from

  dims <- dim(mra)
  npersons <- dims[1]
  npropns <- dims[2]
  nticks <- dims[3]
  nruns <- dims[4]

  #cat(npersons, npropns, nticks, runs)
  dummynums <- rep(NA_real_, nticks)
  dummystrings <- rep(NA_character_, nticks)

  # preallocate. sorta.
  newdf <- data.frame(activn=dummynums,
                      person=dummystrings,
                      dom=dummystrings,
                      propn=dummystrings,
                      run=dummystrings,
                      tick=dummynums,
		      stringsAsFactors=FALSE) # otherwise R complains we add new strings

  cat("Making dataframe with", (npersons * npropns * nticks * nruns) / interval, "rows ...\n")

  i <- 0
  for (rn in 1:nruns) {
    for (pr in 1:npersons) {
      for (ppn in 1:npropns) {
        for (tck in seq(1,nticks,interval)) {
	  i <- i + 1 # row index
          newdf[i,] <- c(mra[pr,ppn,tck,rn], persons[pr], domsInPropnOrder[ppn], propns[ppn], runs[rn], tck)
	  if (i %% 1000 == 0) {cat(i, "")} # don't let user get lonely, but don't be obnoxious
        }
      }
    }
  }

  newdf
}
