#!/bin/sh

WORKSPACE=`pwd`
./build.sh $@
if [ $? -ne 0 ]
then
        exit 1
fi

scp -r $WORKSPACE/../updatesite_nightly/* bamboo@springide.org:/home/springide/htdocs/updatesite_nightly
