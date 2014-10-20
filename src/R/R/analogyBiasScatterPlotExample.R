load("dispersal/dispersal1/dispersal1.df.rdata")
df <- dispersal1.df
sb <- trellis.par.get("strip.background") 
sb[["col"]][1] <- "lightblue"
trellis.par.set("strip.background", sb)
pdf()
xyMeanActivnPlot(CV~CB|bias, groups=rawsum, data=df[df$bias!="none",], xfoci=cb.foci, yfoci=cv.foci, auto.key=list(columns=2, space="bottom"), layout=c(3,1), main="Mean activations of \"capture\" vs. \"isolate\" beliefs\n for 3 analogy biases at time step 5000 in 50 simulation runs", xlab="capture\n", ylab="isolate", jitter.x=T, jitter.y=T, amount=.03, par.settings=simpleTheme(col=c("red", "blue1")), pch=c(4,1), cex=c(1.25, 1))
dev.off()
