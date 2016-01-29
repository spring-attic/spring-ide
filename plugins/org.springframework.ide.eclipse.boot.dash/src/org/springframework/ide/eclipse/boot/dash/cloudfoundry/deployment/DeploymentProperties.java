package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DeploymentProperties {

	int DEFAULT_MEMORY = 1024;
	int DEFAULT_INSTANCES = 1;

	String getAppName();

	int getMemory();

	int getDiskQuota();

	String getBuildpack();

	Map<String, String> getEnvironmentVariables();

	int getInstances();

	List<String> getServices();

	Set<String> getUris();

}
