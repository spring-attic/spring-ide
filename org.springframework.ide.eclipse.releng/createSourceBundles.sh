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

TARGET_DIR=../required-bundles
#TARGET_DIR=/Users/cdupuis/Development/Java/work/dm-server-tools/main-branches/jersey/required-bundles

echo "...source bundles"
rm bundles.map
pwd

for II in $TARGET_DIR/*-sources-*; do 
	echo $II
	NAME=`expr "$II" : '.*/\(.*\)-sources-.*'`
	VERSION=`expr "$II" : '.*-sources-\(.*\).jar'`

	ant -f create-source-bundle.xml -Dbundle.symbolic.name=$NAME -Dbundle.version=$VERSION -Dbundle.home.path=$TARGET_DIR
	
	rm $II
	echo bundle@$NAME=GET,https://src.springframework.org/svn/spring-ide/trunk/required-bundles/$NAME-$VERSION.jar >> bundles.map
	echo bundle@$NAME.source=GET,https://src.springframework.org/svn/spring-ide/trunk/required-bundles/$NAME.source-$VERSION.jar >> bundles.map
	
	#echo bundle@$NAME.source=GET,https://src.springsource.org/svn/dm-server-tools/main-branches/jersey/required-bundles/$NAME.source-$VERSION.jar >> bundles.map
	
done

for II in $TARGET_DIR/*; do 
	echo $II
	NAME=`expr "$II" : '.*/\(.*\)-.*'`
	VERSION=`expr "$II" : '.*-\(.*\).jar'`

	echo bundle@$NAME=GET,https://src.springframework.org/svn/spring-ide/trunk/required-bundles/$NAME-$VERSION.jar >> bundles.map
	
done