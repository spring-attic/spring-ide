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
STAGINGLOCATION=$HOME/Desktop/update
ECLIPSELOCATION=$WORKSPACE/eclipse/plugins/org.eclipse.equinox.launcher_1.0.0.v20070606.jar

$WORKSPACE/eclipse/eclipse -nosplash -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator \
	-updateSite $STAGINGLOCATION -site file://$STAGINGLOCATION/site.xml -metadataRepository file://$STAGINGLOCATION -metadataRepositoryName 'SpringSource Update Site for Eclipse 3.4' -artifactRepository file://$STAGINGLOCATION -artifactRepositoryName 'SpringSource Artifacts' -compress -reusePack200Files -noDefaultIUs

