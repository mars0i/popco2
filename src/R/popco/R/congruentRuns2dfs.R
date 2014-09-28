congruentRuns2dfs <-
function(df1, df2) {
  all(substr(dimnames(df1)[[1]],1,12) == substr(dimnames(df2)[[1]],1,12))
}
