biasDiffs <-
function(df, bias1, bias2, doms) {
  newdf <- df[df$rawsum=="raw" && df$bias==bias1, doms] - df[df$rawsum=="raw" && df$bias==bias2, doms]
  newdf$rawsum <- "raw"
  newdf <- rbind(newdf, c(sapply(newdf[,doms], mean), rawsum="mean"))
  newdf[,doms[1]] <- as.numeric(newdf[,doms[1]])  # rbind coerced everything to string by intermediate conversion to matrix
  newdf[,doms[2]] <- as.numeric(newdf[,doms[2]])  # so undo that
  newdf
}
