xyMeanActivnPlotsBiasPDF <-
function(df1, df2, domy, domx, ...) {
  unconditioned.string <- paste0(domy, "~", domx)
  unconditioned.form <- as.formula(unconditioned.string)
  conditioned.string <- paste0(unconditioned.string, "|bias")
  conditioned.form <- as.formula(conditioned.string)

  df1.main.plot <- xyMeanActivnPlot(conditioned.form, groups=rawsum, data=df1, ...)
  df1.jitter.plot <- addJitter(trellobj=df1.main.plot)
  df1.mean.plot <- xyMeanActivnPlot(unconditioned.form, groups=bias, data=df1[df1$rawsum=="mean",], ...)

  df2.main.plot <- xyMeanActivnPlot(conditioned.form, groups=rawsum, data=df2, ...)
  df2.jitter.plot <- addJitter(trellobj=df2.main.plot)
  df2.mean.plot <- xyMeanActivnPlot(unconditioned.form, groups=bias, data=df2[df2$rawsum=="mean",], ...)

  pdf()
  print(df1.main.plot)
  print(df1.jitter.plot)
  print(df1.mean.plot)
  print(df2.main.plot)
  print(df2.jitter.plot)
  print(df2.mean.plot)
  dev.off()
}
