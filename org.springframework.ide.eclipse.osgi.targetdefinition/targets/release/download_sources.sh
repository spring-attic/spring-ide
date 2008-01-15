#!/bin/sh

SPRING_VERSION=2.5
BUNDLE_SPRING_VERSION=2.5.0

OSGI_VERSION=1.0-rc2
BUNDLE_OSGI_VERSION=1.0.0.rc2
ROOT=`pwd`

download_source() {	
	GROUP_ID=$1
	ARTIFACT_ID=$2
	VERSION=$3
	SYMBOLIC_NAME=$4
	BUNDLE_VERSION=$5
	SRC_BUNDLE=$6
	REPO=$7
	FOLDER_NAME=$ROOT/target/plugins/${SRC_BUNDLE}_${BUNDLE_VERSION}/src/${SYMBOLIC_NAME}_${BUNDLE_VERSION}
	echo $FOLDER_NAME
	mkdir $FOLDER_NAME
	curl -o $FOLDER_NAME/src.zip ${REPO}/$GROUP_ID/$ARTIFACT_ID/$VERSION/$ARTIFACT_ID-$VERSION-sources.jar 
}

download_spring_source() {
	download_source org/springframework $1 $SPRING_VERSION $2 $BUNDLE_SPRING_VERSION org.springframework.spring.source http://repo1.maven.org/maven2
}

download_osgi_source() {
	download_source org/springframework/osgi $1 $OSGI_VERSION $2 $BUNDLE_OSGI_VERSION org.springframework.osgi.source http://s3.amazonaws.com/maven.springframework.org/milestone
}

download_spring_source spring-aop org.springframework.bundle.spring.aop 
download_spring_source spring-beans org.springframework.bundle.spring.beans
download_spring_source spring-core org.springframework.bundle.spring.core
download_spring_source spring-context org.springframework.bundle.spring.context 
download_spring_source spring-context-support org.springframework.bundle.spring.context.support 

download_osgi_source spring-osgi-annotation org.springframework.osgi.bundle.annotation
download_osgi_source spring-osgi-core org.springframework.osgi.bundle.core
download_osgi_source spring-osgi-extender org.springframework.osgi.bundle.extender
download_osgi_source spring-osgi-io org.springframework.osgi.bundle.io
download_osgi_source spring-osgi-test org.springframework.osgi.bundle.test