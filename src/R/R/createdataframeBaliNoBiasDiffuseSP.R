UNFINISHED

source("~/p2/src/R/R/df2ra.R")
gc()
load("bali.rdata")
gc()
bali.df <- multiRAs2combinedMeanDF(list(bali[3:120,,,]), c("peasants"), "SB", "SP")
gc()
save(bali.df, file="balidf.rdata")

