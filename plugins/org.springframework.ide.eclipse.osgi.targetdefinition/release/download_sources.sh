#!/bin/sh

SPRING_VERSION=2.5.6
BUNDLE_SPRING_VERSION=2.5.6

OSGI_VERSION=1.1.2.B
BUNDLE_OSGI_VERSION=1.1.2.B
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
	mkdir -p $FOLDER_NAME
	curl -o $FOLDER_NAME/src.zip ${REPO}/$GROUP_ID/$ARTIFACT_ID/$VERSION/$ARTIFACT_ID-$VERSION-sources.jar 
}

download_spring_source() {
	download_source org/springframework $1 $SPRING_VERSION $2 $BUNDLE_SPRING_VERSION spring.source http://repository.springsource.com/maven/bundles/release
}

download_osgi_source() {
	download_source org/springframework/osgi $1 $OSGI_VERSION $2 $BUNDLE_OSGI_VERSION springdm.source http://repository.springsource.com/maven/bundles/release
}

download_spring_source org.springframework.aop org.springframework.aop 
download_spring_source org.springframework.beans org.springframework.beans
download_spring_source org.springframework.core org.springframework.core
download_spring_source org.springframework.context org.springframework.context 
download_spring_source org.springframework.context.support org.springframework.context.support 
download_spring_source org.springframework.test org.springframework.test

download_osgi_source org.springframework.osgi.extensions.annotation org.springframework.osgi.extensions.annotations
download_osgi_source org.springframework.osgi.core org.springframework.osgi.core
download_osgi_source org.springframework.osgi.extender org.springframework.osgi.extender
download_osgi_source org.springframework.osgi.io org.springframework.osgi.io
download_osgi_source org.springframework.osgi.test org.springframework.osgi.test