#!/bin/sh

if [ -z "$1" ]; then
	echo usage: `basename $0` jobid
	echo "checks every 10 minutes to see if qstat still shows job jobid, and"
	echo "runs createdataframeBali.R when it's done."
	exit 1
fi

# wait until first job is done, checking every 10 minutes (hope for no error)
while [ -n "`qstat | grep $1`" ] ; do sleep 10m ; done

# job is done

if [ -f bali.rdata ]; then
	cd ~/data
	qsub -l h_rt=18:00:00,vf=30G -N finddisg ~/p2/src/qsub/submitanything.job Rscript --no-init-file --verbose ~/p2/src/R/R/finddisagreementBali2pundits.R
	qsub -l h_rt=18:00:00,vf=40G -N createdf ~/p2/src/qsub/submitanything.job Rscript --no-init-file --verbose ~/p2/src/R/R/createdataframeBali2pundits.R
else
	echo `basename $0`: datafile missing
	exit 2
fi
