ksBootTwoBiases <-
function(df, dom, bias1, bias2, ...) {
  ksBootTwoMeanDFs(df[df$bias==bias1,], df[df$bias==bias2,], dom, ...)
}
