# checkMRAfileForDisagreement
# Load a single multi-run RA from a file, and check its last tick
# to see whether the non-pundits are in approximate agreement.

argv <- commandArgs(TRUE)
datafile <- argv[1]
mraname <- argv[2]

cat("Checking array", mraname, "in file", datafile, "for disagreement:\n")

cat("Loading data ...\n")
load(datafile)
cat("Loaded.\n")
ls()

# Note that mraname is a string, and what we want below is
# the variable that's named by the string.  
# get(mraname) gives us that:

dim(get(mraname))
dimnames(get(mraname))[-3]

nonpundits <- grep(paste0("^",punditPrefix), dimnames(get(mraname))$person, invert=T)

cat("Checking for disagreement in last tick, tolerance = .1:\n")
findRunsWithDisagreement(get(mraname)[nonpundits,,,], .1)
cat("Checking for disagreement in last tick, tolerance = .05:\n")
findRunsWithDisagreement(get(mraname)[nonpundits,,,], .05);
cat("Checking for disagreement in last tick, tolerance = .01:\n")
findRunsWithDisagreement(get(mraname)[nonpundits,,,], .01);
