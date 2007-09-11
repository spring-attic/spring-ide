#!/bin/sh

WORKSPACE=`pwd`
./build.sh
scp -r $WORKSPACE/../updatesite_nightly/* bamboo@springide.org:/home/springide/htdocs/updatesite_nightly
