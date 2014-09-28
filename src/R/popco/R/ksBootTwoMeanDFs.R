ksBootTwoMeanDFs <-
function(df1, df2, dom, ...) {
  require(Matching)
  vec1 <- df1[df1$rawsum=="raw", dom]
  vec2 <- df2[df2$rawsum=="raw", dom]
  ks.boot(vec1, vec2, ...)
}
