set JAVA_HOME=D:\tools\jre1.6.0
set WORKSPACE=%~dp0..

%JAVA_HOME%\bin\java -jar org.eclipse.releng.basebuilder/eclipse/startup.jar -verbose -application org.eclipse.ant.core.antRunner -buildfile %WORKSPACE%/org.springframework.ide.eclipse.build-pde/org.eclipse.releng.basebuilder/eclipse/plugins/org.eclipse.pde.build_3.2.0.v20060505a/scripts/build.xml -Dbuilder=%WORKSPACE%/org.springframework.ide.eclipse.build-pde/springide.builder
