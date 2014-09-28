\name{xyMeanActivnPlotsBiasPDF}
\alias{xyMeanActivnPlotsBiasPDF}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
xyMeanActivnPlotsBiasPDF(df1, df2, domy, domx, ...)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{df1}{
%%     ~~Describe \code{df1} here~~
}
  \item{df2}{
%%     ~~Describe \code{df2} here~~
}
  \item{domy}{
%%     ~~Describe \code{domy} here~~
}
  \item{domx}{
%%     ~~Describe \code{domx} here~~
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
function (df1, df2, domy, domx, ...) 
{
    unconditioned.string <- paste0(domy, "~", domx)
    unconditioned.form <- as.formula(unconditioned.string)
    conditioned.string <- paste0(unconditioned.string, "|bias")
    conditioned.form <- as.formula(conditioned.string)
    df1.main.plot <- xyMeanActivnPlot(conditioned.form, groups = rawsum, 
        data = df1, ...)
    df1.jitter.plot <- addJitter(trellobj = df1.main.plot)
    df1.mean.plot <- xyMeanActivnPlot(unconditioned.form, groups = bias, 
        data = df1[df1$rawsum == "mean", ], ...)
    df2.main.plot <- xyMeanActivnPlot(conditioned.form, groups = rawsum, 
        data = df2, ...)
    df2.jitter.plot <- addJitter(trellobj = df2.main.plot)
    df2.mean.plot <- xyMeanActivnPlot(unconditioned.form, groups = bias, 
        data = df2[df2$rawsum == "mean", ], ...)
    pdf()
    print(df1.main.plot)
    print(df1.jitter.plot)
    print(df1.mean.plot)
    print(df2.main.plot)
    print(df2.jitter.plot)
    print(df2.mean.plot)
    dev.off()
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
