\name{multiRA2domMeanRA}
\alias{multiRA2domMeanRA}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
multiRA2domMeanRA(multiRA, doms, lastTick = dim(multiRA)[3], firstTick = 1)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{multiRA}{
%%     ~~Describe \code{multiRA} here~~
}
  \item{doms}{
%%     ~~Describe \code{doms} here~~
}
  \item{lastTick}{
%%     ~~Describe \code{lastTick} here~~
}
  \item{firstTick}{
%%     ~~Describe \code{firstTick} here~~
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
function (multiRA, doms, lastTick = dim(multiRA)[3], firstTick = 1) 
{
    mra <- stripRunPaths(multiRA)
    dimnms <- dimnames(mra)
    numdoms <- length(doms)
    meanmra.dims <- dim(mra)
    meanmra.dims[2] <- numdoms
    meanmra.dims[3] <- lastTick - firstTick + 1
    meanmra <- array(0, meanmra.dims)
    for (i in 1:numdoms) {
        cat(doms[i], "... ")
        meanmra[, i, , ] <- apply(multiRA2punditFreeDomRA(mra[, 
            , firstTick:lastTick, , drop = F], doms[i]), c(1, 
            3, 4), mean)
        cat("done\n")
    }
    dimnames(meanmra) <- list(person = dimnms[[1]], dom = doms, 
        tick = firstTick:lastTick, run = dimnms[[4]])
    meanmra
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
