stripLeftZeros <-
function(brks, cnts) {
  if (cnts[1] == 0) {
    stripLeftZeros(brks[-1], cnts[-1])
  } else {
    list(brks, cnts)
  }
}
