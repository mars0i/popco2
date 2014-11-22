#!/bin/sh

if [ -z "$1" ]; then
	echo "usage $0 jobid"
	echo "checks every 15 minutes to see if qstat still shows job jobid, and"
	echo "runs createdataframeBali.R when it's done."
	exit 1
fi

# wait until first job is done, checking every 15 minutes (hope for no error)
while [ -n "`qstat | grep $1`" ] ; do sleep 15m ; done

# job is done

cd ~/data

qsub -l vf=16G -N df ~/p2/src/qsub/submitanything.job Rscript ~/p2/src/R/R/createdataframeBali.R
