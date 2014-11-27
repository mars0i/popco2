source("~/p2/src/R/R/df2ra.R")
gc()
load("bali.rdata") # from sims/bali/threegroups.clj, i.e. test differeing influence of worldly alternatives on spiritual beliefs
gc()
bali.df <- multiRAs2combinedMeanDF(list(bali[2:41,,,], bali[42:81,,,], bali[82:121,,,]), c("worldly brahman", "worldly peasant", "both"), "SB", "SP")
gc()
save(bali.df, file="balidf.rdata")
