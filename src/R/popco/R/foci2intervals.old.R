foci2intervals.old <-
function(foci, divs=1){
  dif <- foci[3]-foci[2]  # start from index 2, in case the first entry doesn't follow the pattern, e.g. it's a -1 that was added in.
  c(-1, foci[1]-dif/2, foci+dif/2, 1)
}
