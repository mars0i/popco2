multiRA2domMeanRA <-
function(multiRA, doms, lastTick=dim(multiRA)[3], firstTick=1) {
  mra <- stripRunPaths(multiRA)
  dimnms <- dimnames(mra)
  numdoms <- length(doms)

  # construct dimensions of new array:
  meanmra.dims <- dim(mra)
  meanmra.dims[2] <- numdoms # replace number of propns with number of doms
  meanmra.dims[3] <- lastTick - firstTick + 1 # adjust ticks dimension

  meanmra <- array(0, meanmra.dims) # make empty array

  for (i in 1:numdoms) {
    cat(doms[i], "... ")
    # c(1,3,4) as an argument to apply means average *only* over propositions 
    # (within the domain), not over persons (1), ticks (3), or runs (4):
    # Then assign the result to domain i.
    meanmra[,i,,] <- apply(multiRA2punditFreeDomRA(mra[,,firstTick:lastTick,,drop=F], doms[i]), c(1,3,4), mean)
    cat("done\n")
  }

  # add in dimension names
  dimnames(meanmra) <- list(person=dimnms[[1]],
                            dom=doms,
			    tick=firstTick:lastTick,     # makes ticks into numbers rather than strings, I guess
			    run=dimnms[[4]])

  meanmra
}
