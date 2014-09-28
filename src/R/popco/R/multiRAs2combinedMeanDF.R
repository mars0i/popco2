multiRAs2combinedMeanDF <-
function(mras, biases, dom1, dom2, lastTick=dim(mras[[1]])[3], firstTick=lastTick) {
  combineMeanDFsWithBiases(lapply(mras, multiRA2meanDF, dom1, dom2, lastTick, firstTick), biases)
}
