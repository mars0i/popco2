ksBootAllBiasesAllDoms <-
function(df1, df2, biases, doms) {
  for (bs in biases){
    for (dom in doms){
      cat(bs,dom,"pvalue =",ks.boot(df1[df1$bias==bs,dom],df2[df2$bias==bs,dom],nboots=1000)$ks.boot.pvalue,"\n")
    }
  }
}
