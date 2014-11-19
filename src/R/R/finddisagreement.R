source("~/pop/R/df2ra.R")

args <- commandArgs(TRUE)
datafile <- args[1]
mra <- args[2]
personRanges <- args[3] # should parse to a list of vectors. e.g. list(2:21,22:41,42:61)

load(datafile)

print(dimnames(mra)[-3])

for (i in 1:length(personRanges)) {
  personidxs <- personRanges[[i]]  # will be a vector of indexes to persons
  print("tolerance 0.1:")
  print(findRunsWithDisagreement(mra[personidxs, , , ], 0.1))
  print("=======================")
}
