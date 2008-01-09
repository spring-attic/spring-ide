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
STAGINGLOCATION=$WORKSPACE/updatesite/
ECLIPSELOCATION=$WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_1.0.0.v20070606.jar
ECLIPSE_DISTRO_URL=http://mirror.csclub.uwaterloo.ca/eclipse/technology/epp/downloads/release/20071103/eclipse-jee-europa-fall2-linux-gtk.tar.gz
ECLIPSE_TEMP_NAME=eclipse-base.tar.gz

MYLYN_UPDATE_SITE_URL=http://download.eclipse.org/tools/mylyn/update/e3.3/
AJDT_UPDATE_SITE_URL=http://download.eclipse.org/tools/ajdt/33/dev/update

FEATURES_TO_BUILD="beans-feature dependency-feature aop-feature ajdt-feature javaconfig-feature webflow-feature mylyn-feature osgi-feature"


# Run the Eclipse builder on a single builder
build() {
    b=$1
    shift
    p=$@
    echo Building $b with $p
    $JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=$WORKSPACE/$b.builder -DforceContextQualifier=v${NAME} $p

    if [ $? -ne 0 ]
    then
        exit 1
    fi
}

# Run the pack20 on the update site jars
pack() {
	ECLIPSELOCATION=`ls $WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_*`
    $JAVA_HOME/bin/java -Xmx256m -jar $ECLIPSELOCATION -application org.eclipse.update.core.siteOptimizer -jarProcessor -verbose -processAll -repack -pack -outputDir $STAGINGLOCATION $STAGINGLOCATION  
    if [ $? -ne 0 ]
    then
        exit 1
    fi
}

# Install given feature into downloaded Eclipse
install_feature () {
	ECLIPSELOCATION=`ls $WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_*`
	echo Installing $1 from $2
	if [ -z "$3" ]
	then
		output=`java -cp $ECLIPSELOCATION org.eclipse.equinox.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command search -from $2 | grep $1` 
		version=`expr "$output" : '.*\([0-9]\.[0-9]*\.[0-9]*\.[A-Z,a-z,0-9,-]*\).*'`
	else
		version=$3
	fi	
	echo Version $1_$version
	java -cp $ECLIPSELOCATION org.eclipse.equinox.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command install -featureId $1 -version $version -from $2
}

# Download and unzip Eclipse from $ECLIPSE_DISTRO_URL
install_eclipse() {
	echo Downloading Eclipse Distribution
	wget $ECLIPSE_DISTRO_URL -O $ECLIPSE_TEAMP_NAME
	tar zxvf ./$ECLIPSE_TEAMP_NAME
}

#echo Command line: $@

# Only trigger download and feature install if Eclipse directory is missing
if [ ! -d "./eclipse" ]
then
	install_eclipse
	
	install_feature org.eclipse.mylyn_feature $MYLYN_UPDATE_SITE_URL
	install_feature org.eclipse.mylyn.context_feature $MYLYN_UPDATE_SITE_URL
	install_feature org.eclipse.mylyn.ide_feature $MYLYN_UPDATE_SITE_URL
	install_feature org.eclipse.mylyn.java_feature $MYLYN_UPDATE_SITE_URL
	
	install_feature org.eclipse.ajdt $AJDT_UPDATE_SITE_URL
fi

# Clean previous builds
rm -rf $STAGINGLOCATION
rm -rf $WORKSPACE/build
rm -rf $WORKSPACE/eclipse-stage

# Trigger build of features stored in FEATURES_TO_BUILD variable
for featureName in $FEATURES_TO_BUILD
do
	build $featureName $@
done


# trigger pack
pack
