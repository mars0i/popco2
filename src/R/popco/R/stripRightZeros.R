stripRightZeros <-
function(brks, cnts) {
  len <- length(cnts)
  if (cnts[len] ==0) {
    stripRightZeros(brks[-(len+1)], cnts[-len])
  } else {
    list(brks, cnts)
  }
}
