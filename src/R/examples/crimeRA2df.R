# crimeRA2df.R

# run e.g. as:
# qsub submitanything.job Rscript --no-init-file --verbose crimeRA2DF.R

source("~/p2/src/R/R/df2ra.R")
load("crime3.rdata")
crime3.df <- multiRAs2combinedMeanDF(list(crime3[2:41,,,], crime3[42:81,,,], crime3[82:121,,,]), c("virus", "beast", "both"), "CV", "CB")
save(crime3.df, file="crime3df.rdata")
