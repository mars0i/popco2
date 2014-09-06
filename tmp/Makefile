BASE=thatcamp
DIR=THATcamp2014
THEMES=beamerthemeOnFoot.sty beamerthemeOnHand.sty beamerthemeOnHans.sty beamerthemeOnTheFly.sty
FIGDIR=../figures
FIGSRCDIR=../figuresrc
FIGS=
ADDLTEXFILES=
# note use of ":=" rather than "=" on next line; it's a GNU make feature that prevents recursive expansion:
#TEXINPUTS:=.:$(FIGDIR):$(TEXINPUTS)
TEXINPUTS:=.:$(TEXINPUTS)

pdf: $(BASE).pdf
bib: $(BASE).bbl

$(BASE).pdf: $(BASE).tex $(FIGS) $(THEMES)
	(xelatex -interaction batchmode $(BASE) && /bin/echo -n '+') || (echo ; sleep 1; echo ; sleep 1; echo ''; xelatex $(BASE))
#	xelatex $(BASE) ; /bin/echo -n '|'

$(BASE).bbl: phil.bib
	(TEXINPUTS="$(TEXINPUTS)"; latex -interaction batchmode $(BASE) || latex $(BASE))
	(TEXINPUTS="$(TEXINPUTS)"; bibtex $(BASE))
	(TEXINPUTS="$(TEXINPUTS)"; latex -interaction batchmode $(BASE) || latex $(BASE))
	(TEXINPUTS="$(TEXINPUTS)"; (latex -interaction batchmode $(BASE) && killall -SIGUSR1 $(XDVI)) || latex $(BASE))

tar:
	(cd .. ; make tar)

clean:
	/bin/rm *.dvi *.aux *.log *.blg *.toc #*.bbl 
