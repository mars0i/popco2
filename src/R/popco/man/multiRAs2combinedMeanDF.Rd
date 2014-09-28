\name{multiRAs2combinedMeanDF}
\alias{multiRAs2combinedMeanDF}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
multiRAs2combinedMeanDF(mras, biases, dom1, dom2, lastTick = dim(mras[[1]])[3], firstTick = lastTick)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{mras}{
%%     ~~Describe \code{mras} here~~
}
  \item{biases}{
%%     ~~Describe \code{biases} here~~
}
  \item{dom1}{
%%     ~~Describe \code{dom1} here~~
}
  \item{dom2}{
%%     ~~Describe \code{dom2} here~~
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
function (mras, biases, dom1, dom2, lastTick = dim(mras[[1]])[3], 
    firstTick = lastTick) 
{
    combineMeanDFsWithBiases(lapply(mras, multiRA2meanDF, dom1, 
        dom2, lastTick, firstTick), biases)
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
