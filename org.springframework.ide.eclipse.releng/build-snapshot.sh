#!/bin/sh
################################################################################
# Copyright (c) 2005, 2008 Spring IDE Developers
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     Spring IDE Developers - initial API and implementation
################################################################################

WORKSPACE=`pwd`
REMOTE_PATH=$1
shift
ARGS=$@

./build.sh $ARGS
if [ $? -ne 0 ]
then
    exit 1
fi

scp -r $WORKSPACE/updatesite/* ${USER}@springide.org:/home/springide/htdocs/$REMOTE_PATH
