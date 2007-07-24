#!/bin/sh

# directory 
WORKSPACE=`pwd`
NAME=`date +%Y%m%d%H%M`
#$JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=$WORKSPACE/beans-feature.builder -DforceContextQualifier=v${NAME} -Djava.rt=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/classes.jar $@
#$JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=$WORKSPACE/aop-feature.builder -DforceContextQualifier=v${NAME} -Djava.rt=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/classes.jar $@
#$JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=$WORKSPACE/webflow-feature.builder -DforceContextQualifier=v${NAME} -Djava.rt=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/classes.jar $@
$JAVA_HOME/bin/java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -application org.eclipse.ant.core.antRunner -buildfile $WORKSPACE/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=$WORKSPACE/mylyn-feature.builder -DforceContextQualifier=v${NAME} -Djava.rt=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/classes.jar $@
