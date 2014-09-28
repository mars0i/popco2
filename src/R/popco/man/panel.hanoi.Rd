\name{panel.hanoi}
\alias{panel.hanoi}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
panel.hanoi(x, y, horizontal, breaks = "Sturges", ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{x}{
%%     ~~Describe \code{x} here~~
}
  \item{y}{
%%     ~~Describe \code{y} here~~
}
  \item{horizontal}{
%%     ~~Describe \code{horizontal} here~~
}
  \item{breaks}{
%%     ~~Describe \code{breaks} here~~
}
  \item{\dots}{
%%     ~~Describe \code{\dots} here~~
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
function (x, y, horizontal, breaks = "Sturges", ...) 
{
    if (horizontal) {
        condvar <- y
        datavar <- x
    }
    else {
        condvar <- x
        datavar <- y
    }
    conds <- sort(unique(condvar))
    for (i in seq_along(conds)) {
        h <- hist(datavar[condvar == conds[i]], plot = F, breaks)
        brks.cnts <- stripOuterZeros(h$breaks, h$counts)
        brks <- brks.cnts[[1]]
        cnts <- brks.cnts[[2]]
        halfrelfs <- (cnts/sum(cnts))/2
        center <- i
        if (horizontal) {
            panel.rect(butlast(brks), center - halfrelfs, butfirst(brks), 
                center + halfrelfs, ...)
        }
        else {
            panel.rect(center - halfrelfs, butlast(brks), center + 
                halfrelfs, butfirst(brks), ...)
        }
    }
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
