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
STAGINGLOCATION=$WORKSPACE/updatesite/
#-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y

ANTLOCATION=`ls -d $WORKSPACE/org.eclipse.releng.basebuilder/plugins/org.apache.ant_*`

cd $STAGINGLOCATION
ZIP_NAME=`ls spring-ide_*.zip`
VERSION=`expr "$ZIP_NAME" : '.*\([0-9]\.[0-9]*\.[0-9]*\.[A-Z,a-z,0-9,.]*\).zip'`
echo $VERSION

ant -f $WORKSPACE/feature.builder/upload.xml -Dbasebuilder.dir=$ANTLOCATION \
 -Dbundle.version=${VERSION//_/.} \
 -Dupdatesite.package.file.name=spring-ide_updatesite_$VERSION.zip -Dupdatesite.package.output.file=$STAGINGLOCATION/spring-ide_updatesite_$VERSION.zip \
 -Darchive.package.file.name=spring-ide_$VERSION.zip -Darchive.package.output.file=$STAGINGLOCATION/spring-ide_$VERSION.zip
