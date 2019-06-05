#!/bin/bash
set -e
VERSION=0.0.2.RELEASE
if [ ! -f nohttp-cli-${VERSION}.jar ]; then
    curl -O https://repo.maven.apache.org/maven2/io/spring/nohttp/nohttp-cli/${VERSION}/nohttp-cli-${VERSION}.jar
fi
java -jar nohttp-cli-${VERSION}.jar
