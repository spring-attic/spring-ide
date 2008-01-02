#!/bin/sh

WORKSPACE=`pwd`
REMOTE_PATH=$1
shift
ARGS=$@

./build.sh $ARGS
if [ $? -ne 0 ]
then
    exit 1
fi

scp -r $WORKSPACE/../updatesite_nightly/* bamboo@springide.org:/home/springide/htdocs/$REMOTE_PATH
