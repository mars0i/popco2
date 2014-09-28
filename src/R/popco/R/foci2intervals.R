foci2intervals <-
function(foci, divs=1){
  dif <- (foci[3]-foci[2])/divs  # start from index 2, in case the first entry doesn't follow the pattern, e.g. it's a -1 that was added in.
  ints <- seq(foci[1]-dif/2, foci[length(foci)]+dif/2, dif)
  if (ints[1] > -1) {
    ints <- c(-1, ints)
  }
  if (ints[length(ints)] < 1) {
    ints <- c(ints, 1)
  }
  ints
}
