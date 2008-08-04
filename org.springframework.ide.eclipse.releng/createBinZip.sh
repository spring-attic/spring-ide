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
NAME=`date +%Y%m%d%H%M`
#NAME=200802061800
STAGINGLOCATION=$WORKSPACE/updatesite
DROP_STAGINGLOCATION=$WORKSPACE/extension/
ECLIPSELOCATION=$WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_1.0.0.v20070606.jar
ECLIPSE_DISTRO_URL=http://mirror.cc.columbia.edu/pub/software/eclipse/technology/epp/downloads/release/20071103/eclipse-jee-europa-fall2-macosx-carbon.tar.gz
ECLIPSE_TEMP_NAME=eclipse-base.tar.gz
ECLIPSE_TEST_DISTRO_URL=http://gulus.usherbrooke.ca/pub/appl/eclipse/eclipse/downloads/drops/R-3.3.1.1-200710231652/eclipse-Automated-Tests-3.3.1.1.zip

MYLYN_UPDATE_SITE_URL=http://download.eclipse.org/tools/mylyn/update/e3.3/
AJDT_UPDATE_SITE_URL=http://download.eclipse.org/tools/ajdt/33/update
UPDATESITE=file://$STAGINGLOCATION

# Install given feature into downloaded Eclipse
install_feature () {
	ECLIPSELOCATION=`ls $WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_*`
	echo Installing $1 from $2
		#$JAVA_HOME/bin/java -cp $ECLIPSELOCATION org.eclipse.equinox.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command search -from $2
	if [ -z "$3" ]
	then
		output=`$JAVA_HOME/bin/java -cp $ECLIPSELOCATION org.eclipse.equinox.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command search -from $2 | grep $1` 
		version=`expr "$output" : '.*\([0-9]\.[0-9]*\.[0-9]*\.[A-Z,a-z,0-9,-]*\).*'`
	else
		version=$3
	fi	
	echo Version $1_$version
	$JAVA_HOME/bin/java -cp $ECLIPSELOCATION org.eclipse.equinox.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command install -featureId $1 -version $version -from $2 -to $DROP_STAGINGLOCATION
}

# Install Spring IDE features into target eclipse
install_feature org.springframework.ide.eclipse.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.aop.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.ajdt.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.javaconfig.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.webflow.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.mylyn.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.osgi.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.security.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.autowire.feature $UPDATESITE
install_feature org.springframework.ide.eclipse.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.aop.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.ajdt.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.javaconfig.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.webflow.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.mylyn.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.osgi.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.security.feature.source $UPDATESITE
install_feature org.springframework.ide.eclipse.autowire.feature.source $UPDATESITE


cd $STAGINGLOCATION
ZIP_NAME=`ls *.zip`
VERSION=`expr "$ZIP_NAME" : 'spring-ide_updatesite_\(.*\).zip'`
ZIP_NAME=spring-ide_$VERSION.zip
cd $DROP_STAGINGLOCATION
zip -r $ZIP_NAME eclipse
mv $DROP_STAGINGLOCATION/$ZIP_NAME $STAGINGLOCATION
cd $STAGINGLOCATION
openssl dgst -md5 $ZIP_NAME >>$ZIP_NAME.md5
openssl dgst -sha1 $ZIP_NAME >>$ZIP_NAME.sha1;

rm -rf $DROP_STAGINGLOCATION
