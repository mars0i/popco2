ksBootTwoMeanDFsForBias <-
function(df1, df2, dom, bias, ...) {
  ksBootTwoMeanDFs(df1[df1$bias==bias,], df2[df2$bias==bias,], dom, ...)
}
