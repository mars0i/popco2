panel.hanoi <-
function(x, y, horizontal, breaks="Sturges", ...) {  # "Sturges" is hist()'s default

  if (horizontal) {
    condvar <- y # conditioning ("independent") variable
    datavar <- x # data ("dependent") variable
  } else {
    condvar <- x
    datavar <- y
  }

  conds <- sort(unique(condvar))

  # loop through the possible values of the conditioning variable
  for (i in seq_along(conds)) {

      h <- hist(datavar[condvar == conds[i]], plot=F, breaks) # use base hist(ogram) function to extract some information

    # strip outer counts == 0, and corresponding bins
    brks.cnts <- stripOuterZeros(h$breaks, h$counts)
    brks <- brks.cnts[[1]]
    cnts <- brks.cnts[[2]]

    halfrelfs <- (cnts/sum(cnts))/2  # i.e. half of the relative frequency
    center <- i

    # All of the variables passed to panel.rec will usually be vectors, and panel.rect will therefore make multiple rectangles.
    if (horizontal) {
      panel.rect(butlast(brks), center - halfrelfs, butfirst(brks), center + halfrelfs, ...)
    } else {
      panel.rect(center - halfrelfs, butlast(brks), center + halfrelfs, butfirst(brks), ...)
    }
  }
}
