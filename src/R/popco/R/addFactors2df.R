addFactors2df <-
function(df, col, foci) {
  newcol <- paste0(col, ".fac")
  df[,newcol] <- cut(df[,col], foci2intervals(cv.foci))
  df
}
