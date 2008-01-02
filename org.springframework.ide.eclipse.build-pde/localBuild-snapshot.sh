#!/bin/sh

WORKSPACE=`pwd`
REMOTE_PATH=updatesite_nightly
shift
ARGS=$@

./localBuild.sh $ARGS
if [ $? -ne 0 ]
then
    exit 1
fi

scp -r $WORKSPACE/../updatesite_nightly/* cdupuis@springide.org:/home/springide/htdocs/$REMOTE_PATH
