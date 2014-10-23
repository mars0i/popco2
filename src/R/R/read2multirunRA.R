# read2multirunRA.R

# usage, e.g.:
# qsub submitanything.job Rscript --no-init-file --verbose \
#  read2multirunRA.R arrayname <csvfile> <csvfile> ...

args <- commandArgs(TRUE)

arrayname <- args[1]
filename <- paste0(arrayname, ".rdata")

csvvec <- args[-1]

source("~/p2/src/R/R/df2ra.R")
assign(arrayname, read2multirunRA(csvvec))
save(list=arrayname, file=filename)
