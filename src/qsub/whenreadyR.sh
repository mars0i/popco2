#!/bin/sh

if [ -z "$2" ]; then
	echo usage: `basename $0` jobid R-script
	echo "checks every 10 minutes to see if qstat still shows job jobid, and"
	echo "runs R-script when it's done."
	exit 1
fi

# wait until first job is done, checking every 10 minutes (hope for no error)
while [ -n "`qstat | grep $1`" ] ; do sleep 10m ; done

# job is done

if [ -f *.rdata ]; then
	cd ~/data
	qsub -l h_rt=18:00:00,vf=40G -N `basename "$2"` ~/p2/src/qsub/submitanything.job Rscript --no-init-file --verbose "$2"
else
	echo `basename $0`: datafile missing
	exit 2
fi
