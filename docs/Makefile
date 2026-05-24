REPORT=report

.PHONY: all clean

all: $(REPORT).pdf

$(REPORT).pdf: $(REPORT).tex bib/references.bib
	pdflatex -interaction=nonstopmode $(REPORT).tex
	bibtex $(REPORT)
	pdflatex -interaction=nonstopmode $(REPORT).tex
	pdflatex -interaction=nonstopmode $(REPORT).tex

clean:
	rm -f $(REPORT).aux $(REPORT).bbl $(REPORT).blg $(REPORT).log $(REPORT).out $(REPORT).pdf $(REPORT).toc

