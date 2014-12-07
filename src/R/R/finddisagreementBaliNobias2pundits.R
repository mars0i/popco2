source("~/p2/src/R/R/df2ra.R")
load("bali.rdata")

print("tolerance 0.1:")
print(findRunsWithDisagreement(bali[3:42,,,], 0.1))

print("tolerance 0.01:")
print(findRunsWithDisagreement(bali[3:42,,,], 0.01))
