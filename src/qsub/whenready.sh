#!/bin/sh

if [ -z "$2" ]; then
	echo usage: `basename $0` jobid R-script
	echo "checks every 10 minutes to see if qstat still shows job jobid, and"
	echo "runs R-script when it's done."
	exit 1
fi

# wait until first job is done, checking every 10 minutes (hope for no error)
while [ -n "`qstat | grep $1`" ] ; do sleep 10m ; done

# previous job is done

cd ~/data
qsub -l h_rt=18:00:00,vf=16G -N `basename "$2"` ~/p2/src/qsub/submitanything.job "$2"
