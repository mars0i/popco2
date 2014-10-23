#!/bin/sh

csvvec=`echo *.csv | ~/p2/src/R/shell/files2vec`
if [ -z "$csvvec" -o -z "$1" ]; then
	echo usage: $0 mra
	echo Note: Should be run from the directory containing the csvs you want to process.
	exit 1
fi

qsub -N "$1" ~/p2/src/qsub/read2multirunRA.job `pwd` $1 "$csvvec"