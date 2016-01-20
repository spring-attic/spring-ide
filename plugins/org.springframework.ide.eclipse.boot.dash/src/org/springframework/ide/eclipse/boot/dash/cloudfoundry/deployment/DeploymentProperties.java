package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.List;
import java.util.Map;

public interface DeploymentProperties {

	String getAppName();

	int getMemory();

	String getBuildpack();

	Map<String, String> getEnvironmentVariables();

	int getInstances();

	List<String> getServices();

	String getHost();

	String getDomain();

}
