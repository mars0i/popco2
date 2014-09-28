stripOuterZeros <-
function(brks, cnts) { do.call("stripLeftZeros", stripRightZeros(brks, cnts)) }
