# crimeFindDisagreement.R

source("~/p2/src/R/R/df2ra.R")
load("crime3.rdata")
print(findRunsWithDisagreement(mra[2:41,,,], 0.01))
print(findRunsWithDisagreement(mra[42:81,,,], 0.01))
print(findRunsWithDisagreement(mra[82:121,,,], 0.01))
