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
#NAME=`date +%Y%m%d%H%M`
NAME=200808011800
STAGINGLOCATION=$WORKSPACE/updatesite/
TEST_STAGINGLOCATION=$WORKSPACE/testupdatesite/
ECLIPSELOCATION=$WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_1.0.0.v20070606.jar
#ECLIPSE_DISTRO_URL=http://mirror.cc.columbia.edu/pub/software/eclipse/technology/epp/downloads/release/europa/winter/eclipse-jee-europa-winter-macosx-carbon.tar.gz
ECLIPSE_DISTRO_URL=http://build.eclipse.org/technology/epp/epp_build/34/download/20080623-1700/20080623-1700_eclipse-jee-ganymede-macosx.carbon.ppc.tar.gz
ECLIPSE_TEMP_NAME=eclipse-base.tar.gz
ECLIPSE_TEST_DISTRO_URL=http://gulus.usherbrooke.ca/pub/appl/eclipse/eclipse/downloads/drops/R-3.3.1.1-200710231652/eclipse-Automated-Tests-3.3.1.1.zip

MYLYN_UPDATE_SITE_URL=http://download.eclipse.org/tools/mylyn/update/e3.4/
AJDT_UPDATE_SITE_URL=http://download.eclipse.org/tools/ajdt/34/dev/update

#-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y

# Run the Eclipse builder on a single builder
build() {

	rm -rf org.eclipse.releng.basebuilder/eclipse
	if [ ! -d "org.eclipse.releng.basebuilder/eclipse" ]
		then
		unzip -d org.eclipse.releng.basebuilder org.eclipse.releng.basebuilder/basebuilder.zip
	fi

    p=$@
    $JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/plugins/org.eclipse.pde.build_3.4.0.v20080522/scripts/build.xml -Dbuilder=$WORKSPACE/feature.builder -DforceContextQualifier=v${NAME} $p

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

# Run p2 metadata generation
p2() {
	$JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator -updateSite $STAGINGLOCATION -site file://$STAGINGLOCATION/site.xml -metadataRepository file://$STAGINGLOCATION -metadataRepositoryName "Spring IDE Update Site" -artifactRepository file://$STAGINGLOCATION -artifactRepositoryName "Spring IDE Artifacts" -compress -reusePack200Files -noDefaultIUs -vmargs -Xmx256m
}

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
	$JAVA_HOME/bin/java -cp $ECLIPSELOCATION org.eclipse.equinox.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command install -featureId $1 -version $version -from $2
}

# Download and unzip Eclipse from $ECLIPSE_DISTRO_URL
install_eclipse() {
	if [ ! -e "./$ECLIPSE_TEMP_NAME" ]
	then
		echo Downloading Eclipse Distribution
		wget $ECLIPSE_DISTRO_URL -O $ECLIPSE_TEMP_NAME
		if [ $? -ne 0 ]
		then
	    	exit 1
		fi
	fi
	tar zxvf ./$ECLIPSE_TEMP_NAME
	
	unzip ./org.eclipse.releng.basebuilder.zip
	rm -rf ./__MACOSX/
}

#echo Command line: $@
ARGS=$@
INSTALL_FEATURES=0
EXECUTE_TESTS=0

while [ "$1" != "" ]; do
    case $1 in
        -delete-eclipse-base )  echo Deleting Eclipse
                                rm -rf $WORKSPACE/eclipse
                                rm -rf $WORKSPACE/$ECLIPSE_TEMP_NAME
                                INSTALL_FEATURES=1
                                ;;
        -update-features )  	INSTALL_FEATURES=1
                                ;;
		-execute-tests )    	EXECUTE_TESTS=1
                                ;;
        * )
    esac
    shift
done

# Only trigger download and feature install if Eclipse directory is missing
if [ ! -d "./eclipse" ]
then
	install_eclipse
    INSTALL_FEATURES=1
fi

if [ "$INSTALL_FEATURES" = "1" ] 
then
#	install_feature org.eclipse.mylyn_feature $MYLYN_UPDATE_SITE_URL
#	install_feature org.eclipse.mylyn.context_feature $MYLYN_UPDATE_SITE_URL
#	install_feature org.eclipse.mylyn.ide_feature $MYLYN_UPDATE_SITE_URL
#	install_feature org.eclipse.mylyn.java_feature $MYLYN_UPDATE_SITE_URL

	install_feature org.eclipse.ajdt $AJDT_UPDATE_SITE_URL
fi

# Clean previous builds
rm -rf $STAGINGLOCATION
rm -rf $TESTSTAGINGLOCATION
rm -rf $WORKSPACE/build
rm -rf $WORKSPACE/eclipse-stage

# Trigger build of features
build $ARGS



# Trigger pack
#pack now done during the ant build phase

# Trigger p2 metadata creation
#p2

if [ "$EXECUTE_TESTS" = "1" ]
then

	rm -rf $WORKSPACE/results

	# Start test
	TEST_SUPPORT_ARCHIVE=`ls $WORKSPACE/org.eclipse.test_*`
	ECLIPSELOCATION=`ls $WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_*`
	JUNIT4=$WORKSPACE/eclipse/plugins/org.junit4_4.3.1

	# Unzip test support zip into target eclipse
	tar xvfz $TEST_SUPPORT_ARCHIVE
	rm -rf $JUNIT4
	
	# Install Spring IDE features into target eclipse
	install_feature org.springframework.ide.eclipse.feature file://$STAGINGLOCATION
	install_feature org.springframework.ide.eclipse.aop.feature file://$STAGINGLOCATION
	install_feature org.springframework.ide.eclipse.ajdt.feature file://$STAGINGLOCATION
	install_feature org.springframework.ide.eclipse.javaconfig.feature file://$STAGINGLOCATION
	install_feature org.springframework.ide.eclipse.webflow.feature file://$STAGINGLOCATION
	install_feature org.springframework.ide.eclipse.mylyn.feature file://$STAGINGLOCATION
	install_feature org.springframework.ide.eclipse.osgi.feature file://$STAGINGLOCATION

	# Build the tests feature
	$JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=$WORKSPACE/test-feature.builder -DforceContextQualifier=v${NAME} $ARGS
	if [ $? -ne 0 ]
	then
     	exit 1
	fi

	# Install the test feature into target eclipse
	install_feature org.springframework.ide.eclipse.tests.feature file://$TEST_STAGINGLOCATION
	
	# clean out old osgi cache data
	rm -rf $WORKSPACE/eclipse/configuration/org.eclipse.osgi
	
	# Run test
	java -jar $ECLIPSELOCATION -application org.eclipse.ant.core.antRunner -application org.eclipse.ant.core.antRunner -buildfile eclipse/plugins/org.springframework.ide.eclipse.beans.core.tests*/test.xml -Declipse-home=$WORKSPACE/eclipse -Dos=macosx -Dws=carbon -Darch=x86

	# Move results
	mv $WORKSPACE/eclipse/results .

	# Clean up
	rm -rf $WORKSPACE/eclipse
fi