congruentRuns1df <-
function(df, bias1, bias2) {
  congruentRuns2dfs(df[df$bias==bias1 && df$rawsum=="raw",], df[df$bias==bias2 && df$rawsum=="raw",])
}
