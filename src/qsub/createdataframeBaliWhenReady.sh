#!/bin/sh

if [ -z "$1" ]; then
	echo usage: `basename $0` jobid
	echo "checks every 15 minutes to see if qstat still shows job jobid, and"
	echo "runs createdataframeBali.R when it's done."
	exit 1
fi

# wait until first job is done, checking every 15 minutes (hope for no error)
while [ -n "`qstat | grep $1`" ] ; do sleep 15m ; done

# job is done

if [ -f bali.rdata ]; then
	cd ~/data
	qsub -l h_rt=18:00:00,vf=20G -N finddisg ~/p2/src/qsub/submitanything.job Rscript --no-init-file --verbose ~/p2/src/R/R/finddisagreementBali.R
	qsub -l h_rt=18:00:00,vf=20G -N createdf ~/p2/src/qsub/submitanything.job Rscript --no-init-file --verbose ~/p2/src/R/R/createdataframeBali.R
else
	echo `basename $0`: datafile missing
	exit 2
fi
