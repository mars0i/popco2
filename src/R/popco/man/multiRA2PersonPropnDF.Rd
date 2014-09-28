\name{multiRA2PersonPropnDF}
\alias{multiRA2PersonPropnDF}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
multiRA2PersonPropnDF(mra, interval = 1)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{mra}{
%%     ~~Describe \code{mra} here~~
}
  \item{interval}{
%%     ~~Describe \code{interval} here~~
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
%%  ~Describe the value returned
%%  If it is a LIST, use
%%  \item{comp1 }{Description of 'comp1'}
%%  \item{comp2 }{Description of 'comp2'}
%% ...
}
\references{
%% ~put references to the literature/web site here ~
}
\author{
%%  ~~who you are~~
}
\note{
%%  ~~further notes~~
}

%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
%% ~~objects to See Also as \code{\link{help}}, ~~~
}
\examples{
##---- Should be DIRECTLY executable !! ----
##-- ==>  Define data, use random,
##--	or do  help(data=index)  for the standard data sets.

## The function is currently defined as
function (mra, interval = 1) 
{
    dnames <- dimnames(mra)
    persons <- dnames[[1]]
    propns <- dnames[[2]]
    ticks <- dnames[[3]]
    runs <- dnames[[4]]
    domsInPropnOrder <- sub("_.*", "", propns)
    dims <- dim(mra)
    npersons <- dims[1]
    npropns <- dims[2]
    nticks <- dims[3]
    nruns <- dims[4]
    dummynums <- rep(NA_real_, nticks)
    dummystrings <- rep(NA_character_, nticks)
    newdf <- data.frame(activn = dummynums, person = dummystrings, 
        dom = dummystrings, propn = dummystrings, run = dummystrings, 
        tick = dummynums, stringsAsFactors = FALSE)
    cat("Making dataframe with", (npersons * npropns * nticks * 
        nruns)/interval, "rows ...\n")
    i <- 0
    for (rn in 1:nruns) {
        for (pr in 1:npersons) {
            for (ppn in 1:npropns) {
                for (tck in seq(1, nticks, interval)) {
                  i <- i + 1
                  newdf[i, ] <- c(mra[pr, ppn, tck, rn], persons[pr], 
                    domsInPropnOrder[ppn], propns[ppn], runs[rn], 
                    tck)
                  if (i\%\%1000 == 0) {
                    cat(i, "")
                  }
                }
            }
        }
    }
    newdf
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
