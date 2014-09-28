\name{biasDiffs}
\alias{biasDiffs}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
biasDiffs(df, bias1, bias2, doms)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{df}{
%%     ~~Describe \code{df} here~~
}
  \item{bias1}{
%%     ~~Describe \code{bias1} here~~
}
  \item{bias2}{
%%     ~~Describe \code{bias2} here~~
}
  \item{doms}{
%%     ~~Describe \code{doms} here~~
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
function (df, bias1, bias2, doms) 
{
    newdf <- df[df$rawsum == "raw" && df$bias == bias1, doms] - 
        df[df$rawsum == "raw" && df$bias == bias2, doms]
    newdf$rawsum <- "raw"
    newdf <- rbind(newdf, c(sapply(newdf[, doms], mean), rawsum = "mean"))
    newdf[, doms[1]] <- as.numeric(newdf[, doms[1]])
    newdf[, doms[2]] <- as.numeric(newdf[, doms[2]])
    newdf
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
