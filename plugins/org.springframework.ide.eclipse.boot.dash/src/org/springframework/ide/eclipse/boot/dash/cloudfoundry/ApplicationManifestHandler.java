/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

/**
 * Reads and creates manifest.yml content from a specific location relative to
 * an {@link IProject}.
 *
 */
public class ApplicationManifestHandler {

	public static final String RANDOM_VAR = "${random}"; //$NON-NLS-1$

	public static final String APPLICATIONS_PROP = "applications";

	public static final String NAME_PROP = "name";

	public static final String MEMORY_PROP = "memory";

	public static final String INSTANCES_PROP = "instances";

	public static final String SUB_DOMAIN_PROP = "host";

	public static final String SUB_DOMAINS_PROP = "hosts";

	public static final String DOMAIN_PROP = "domain";

	public static final String DOMAINS_PROP = "domains";

	public static final String NO_ROUTE_PROP = "no-route";

	public static final String NO_HOSTNAME_PROP = "no-hostname";

	public static final String RANDOM_ROUTE_PROP = "random-route";

	public static final String SERVICES_PROP = "services";

	public static final String LABEL_PROP = "label";

	public static final String PROVIDER_PROP = "provider";

	public static final String VERSION_PROP = "version";

	public static final String PLAN_PROP = "plan";

	public static final String PATH_PROP = "path";

	public static final String BUILDPACK_PROP = "buildpack";

	public static final String ENV_PROP = "env";

	public static final String DISK_QUOTA_PROP = "disk_quota";

	private final IProject project;

	private final IFile manifestFile;

	private final List<CloudDomain> domains;

	public ApplicationManifestHandler(IProject project, List<CloudDomain> domains) {
		this(project, domains, null);
	}

	public ApplicationManifestHandler(IProject project, List<CloudDomain> domains, IFile manifestFile) {
		this.project = project;
		this.manifestFile = manifestFile;
		this.domains = domains;
	}

	protected InputStream getInputStream() throws Exception {

		File file = getManifestFile();
		if (file != null && file.exists()) {
			return new FileInputStream(file);
		} else {
			throw ExceptionUtil.coreException("No manifest.yml file found in project: " + project.getName());
		}
	}

	/**
	 *
	 * @return manifest file if it exists. Null otherwise
	 */
	protected File getManifestFile() {
		if (manifestFile != null) {
			URI locationURI = manifestFile.getLocationURI();
			File file = new File(locationURI);
			return file.exists() ? file : null;
		}
		return null;
	}

	public boolean hasManifest() {
		File file = getManifestFile();
		return file != null && file.exists();
	}

	/**
	 *
	 * @param applicationName
	 *            name of application to lookup in the manifest file.
	 * @param propertyName
	 *            String value property to retrieve from manifest for given
	 *            application entry.
	 * @return Value of property, or null if not found, or entry for application
	 *         in manifest does not exist.
	 */
	public String getApplicationProperty(String appName, String propertyName, IProgressMonitor monitor) {
		try {
			Map<?, ?> appMap = getApplicationMap(appName, monitor);
			if (appMap != null) {
				return getValue(appMap, propertyName, String.class);
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	public Map<?, ?> getApplicationMap(String appName, IProgressMonitor monitor) throws Exception {
		Map<Object, Object> allResults = parseManifestFromFile();

		List<Map<?, ?>> appMaps = getApplications(allResults);

		for (Map<?, ?> appMap : appMaps) {

			String existingAppName = getValue(appMap, NAME_PROP, String.class);
			if (existingAppName != null && existingAppName.equals(appName)) {
				return appMap;
			}
		}
		return null;
	}

	/**
	 *
	 * @param containerMap
	 * @param propertyName
	 * @return map of values for the given property name, or null if it cannot
	 *         be resolved
	 */
	protected Map<?, ?> getContainingPropertiesMap(Map<?, ?> containerMap, String propertyName) {
		if (containerMap == null || propertyName == null) {
			return null;
		}
		Object yamlElementObj = containerMap.get(propertyName);

		if (yamlElementObj instanceof Map<?, ?>) {
			return (Map<Object, Object>) yamlElementObj;
		} else {
			return null;
		}
	}

//	protected String getStringValue(Map<?, ?> containingMap, String propertyName) {
//
//		if (containingMap == null) {
//			return null;
//		}
//
//		Object valObj = containingMap.get(propertyName);
//
//		if (valObj instanceof String) {
//			return (String) valObj;
//		}
//		return null;
//	}
//
	@SuppressWarnings("unchecked")
	protected <T> T getValue(Map<?, ?> containingMap, String propertyName, Class<T> type) {
		if (containingMap == null) {
			return null;
		}
		Object valObj = containingMap.get(propertyName);

		if (valObj != null && type.isAssignableFrom(valObj.getClass())) {
			return (T) valObj;
		}
		return null;
	}

	protected List<Map<?, ?>> getApplications(Map<Object, Object> results) throws Exception {

		Object applicationsObj = results.get(APPLICATIONS_PROP);
		if (!(applicationsObj instanceof List<?>)) {
			String source = manifestFile == null ? "entered manifest" : "file " + manifestFile.getFullPath();
			throw ExceptionUtil.coreException("Expected a top-level list of applications in " + source
					+ ". Unable to continue parsing manifest values. No manifest values will be loaded into the application deployment info.");
		}

		List<?> applicationsList = (List<?>) applicationsObj;

		List<Map<?, ?>> applications = new ArrayList<Map<?, ?>>();

		// Use only the first application entry
		if (!applicationsList.isEmpty()) {
			for (Object val : applicationsList) {
				if (val instanceof Map<?, ?>) {
					applications.add((Map<?, ?>) val);
				}

			}
		}

		return applications;
	}

	protected CloudApplicationDeploymentProperties getDeploymentProperties(Map<?, ?> appMap,
			Map<Object, Object> allResults, IProgressMonitor monitor) throws Exception {

		CloudApplicationDeploymentProperties properties = new CloudApplicationDeploymentProperties();

		String appName = getValue(appMap, NAME_PROP, String.class);

		properties.setAppName(appName);
		properties.setProject(project);
		properties.setManifestFile(manifestFile);

		readMemory(appMap, allResults, properties);

		readDiskQuota(appMap, allResults, properties);

		readApplicationURL(appMap, allResults, properties);

		readBuildpack(appMap, allResults, properties);

		readEnvVars(appMap, allResults, properties);

		readServices(appMap, allResults, properties);

		readInstances(appMap, allResults, properties);

		ValidationResult validation = properties.getValidator().getValue();
		if (validation != null && !validation.isOk()) {
			throw ExceptionUtil.coreException(validation.msg);
		}

		return properties;
	}

	public List<CloudApplicationDeploymentProperties> load(IProgressMonitor monitor) throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask("Loading manifest.yml", 6);

		try {

			Map<Object, Object> allResults = parseManifestFromFile();

			if (allResults == null || allResults.isEmpty()) {
				throw ExceptionUtil
						.coreException("No content found in manifest.yml. Make sure the manifest is valid.");

			}

			List<Map<?, ?>> appMaps = getApplications(allResults);

			if (appMaps == null || appMaps.isEmpty()) {
				throw ExceptionUtil.coreException(
						"No application definition found in manifest.yml. Make sure at least one application is defined");
			}
			List<CloudApplicationDeploymentProperties> properties = new ArrayList<CloudApplicationDeploymentProperties>();

			for (Map<?, ?> app : appMaps) {
				CloudApplicationDeploymentProperties props = getDeploymentProperties(app, allResults, subMonitor);
				if (props != null) {
					properties.add(props);
				}
			}

			return properties;

		} finally {
			subMonitor.done();
		}

	}

	/**
	 * Creates a new manifest.yml file. If one already exists, the existing one
	 * will not be replaced.
	 *
	 * @return true if new file created with content. False otherwise
	 * @throws Exception
	 *             if error occurred during file creation or serialising
	 *             manifest content
	 */
	public boolean create(IProgressMonitor monitor, CloudApplicationDeploymentProperties properties) throws Exception {

		if (properties == null) {
			return false;
		}
		File file = getManifestFile();
		if (file != null) {
			BootDashActivator.logWarning(
					"Manifest.yml file already found at: " + manifestFile.getFullPath() + ". New content will not be written.");
			return false;
		}

		Map<Object, Object> deploymentInfoYaml = toYaml(properties, domains);
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		String manifestValue = yaml.dump(deploymentInfoYaml);

		if (manifestValue == null) {
			throw ExceptionUtil.coreException("Failed to generate manifesty.yml for: " + properties.getAppName()
					+ " Unknown problem trying to serialise content of manifest into: " + deploymentInfoYaml);
		}

		createFile(project, manifestFile, manifestValue, monitor);
		return true;
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> toYaml(DeploymentProperties properties, List<CloudDomain> cloudDomains) {
		Map<Object, Object> deploymentInfoYaml = new LinkedHashMap<Object, Object>();

		Object applicationsObj = deploymentInfoYaml.get(APPLICATIONS_PROP);
		List<Map<Object, Object>> applicationsList = null;
		if (applicationsObj == null) {
			applicationsList = new ArrayList<Map<Object, Object>>();
			deploymentInfoYaml.put(APPLICATIONS_PROP, applicationsList);
		} else if (applicationsObj instanceof List<?>) {
			applicationsList = (List<Map<Object, Object>>) applicationsObj;
		}

		Map<Object, Object> application = new LinkedHashMap<Object, Object>();
		applicationsList.add(application);

		application.put(NAME_PROP, properties.getAppName());

		String memory = getMemoryAsString(properties.getMemory());
		if (memory != null) {
			application.put(MEMORY_PROP, memory);
		}

		String diskQuota = getMemoryAsString(properties.getDiskQuota());
		if (diskQuota != null && properties.getDiskQuota() != DeploymentProperties.DEFAULT_MEMORY) {
			application.put(DISK_QUOTA_PROP, diskQuota);
		}

		if (properties.getInstances() != DeploymentProperties.DEFAULT_INSTANCES) {
			application.put(ApplicationManifestHandler.INSTANCES_PROP, properties.getInstances());
		}
		if (properties.getServices() != null && !properties.getServices().isEmpty()) {
			application.put(SERVICES_PROP, properties.getServices());
		}
		if (properties.getEnvironmentVariables() != null && !properties.getEnvironmentVariables().isEmpty()) {
			application.put(ENV_PROP, properties.getEnvironmentVariables());
		}

		Set<String> hosts = new LinkedHashSet<>();
		Set<String> domains = new LinkedHashSet<>();

		extractHostsAndDomains(properties.getUris(), cloudDomains, hosts, domains);
		for (String uri : properties.getUris()) {
			try {
				// Find the first valid URL
				CloudApplicationURL cloudAppUrl = CloudApplicationURL.getCloudApplicationURL(uri, cloudDomains);
				if (cloudAppUrl.getSubdomain() != null) {
					hosts.add(cloudAppUrl.getSubdomain());
				}
				if (cloudAppUrl.getDomain() != null) {
					domains.add(cloudAppUrl.getDomain());
				}
			} catch (Exception e) {
				// ignore
			}
		}

		if (hosts.isEmpty() && domains.isEmpty()) {
			application.put(NO_ROUTE_PROP, true);
		} else {
			if (hosts.isEmpty()) {
				application.put(NO_HOSTNAME_PROP, true);
			} else if (hosts.size() == 1) {
				String host = hosts.iterator().next();
				if (!properties.getAppName().equals(host)) {
					application.put(SUB_DOMAIN_PROP, host);
				}
			} else {
				application.put(SUB_DOMAINS_PROP, new ArrayList<>(hosts));
			}
			if (domains.size() == 1) {
				application.put(DOMAIN_PROP, domains.iterator().next());
			} else if (domains.size() > 1) {
				application.put(DOMAINS_PROP, new ArrayList<>(domains));
			}
		}

		return deploymentInfoYaml;
	}

	public static void extractHostsAndDomains(Collection<String> uris, List<CloudDomain> cloudDomains, Set<String> hostsSet, Set<String> domainsSet) {
		for (String uri : uris) {
			try {
				// Find the first valid URL
				CloudApplicationURL cloudAppUrl = CloudApplicationURL.getCloudApplicationURL(uri, cloudDomains);
				if (cloudAppUrl.getSubdomain() != null) {
					hostsSet.add(cloudAppUrl.getSubdomain());
				}
				if (cloudAppUrl.getDomain() != null) {
					domainsSet.add(cloudAppUrl.getDomain());
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void createFile(IProject project, IFile file, String data, IProgressMonitor monitor) throws CoreException {
		file.create(new ByteArrayInputStream(data.getBytes()), true, monitor);
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	protected void readEnvVars(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		Map<Object, Object> propertiesMap = new LinkedHashMap<>();
		Map<?, ?> map = getContainingPropertiesMap(allResults, ENV_PROP);
		if (map != null) {
			propertiesMap.putAll(map);
		}
		map = getContainingPropertiesMap(application, ENV_PROP);
		if (map != null) {
			propertiesMap.putAll(map);
		}

		if (propertiesMap.isEmpty()) {
			return;
		}

		Map<String, String> loadedVars = new HashMap<String, String>();

		for (Entry<?, ?> entry : propertiesMap.entrySet()) {
			if ((entry.getKey() instanceof String)) {
				String varName = (String) entry.getKey();
				String varValue = null;
				if (entry.getValue() instanceof String) {
					varValue = (String) entry.getValue();
				} else if (entry.getValue() instanceof Integer) {
					varValue = Integer.toString((Integer) entry.getValue());
				}
				if (varName != null && varValue != null) {
					loadedVars.put(varName, varValue);
				}
			}
		}
		properties.setEnvironmentVariables(loadedVars);
	}

	protected void readServices(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		Object yamlElementObj = allResults.get(SERVICES_PROP);
		List<String> cloudServices = new ArrayList<String>();

		if (yamlElementObj instanceof List<?>) {
			addServices((List<?>) yamlElementObj, cloudServices);
		}

		yamlElementObj = application.get(SERVICES_PROP);
		if (yamlElementObj instanceof List<?>) {
			addServices((List<?>) yamlElementObj, cloudServices);
		}

		properties.setServices(cloudServices);
	}

	protected void addServices(List<?> servicesToAdd, List<String> cloudServices) {

		for (Object servNameObj : servicesToAdd) {
			if (servNameObj instanceof String && !cloudServices.contains(servNameObj)) {
				String serviceName = (String) servNameObj;
				cloudServices.add(serviceName);
			}
		}
	}

	protected void readInstances(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		Integer instances = getValue(application, INSTANCES_PROP, Integer.class);
		if (instances == null) {
			instances = getValue(allResults, INSTANCES_PROP, Integer.class);
		}
		if (instances != null) {
			properties.setInstances(instances);
		}
	}

	protected void readBuildpack(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		String buildpack = getValue(application, BUILDPACK_PROP, String.class);
		if (buildpack == null) {
			buildpack = getValue(allResults, BUILDPACK_PROP, String.class);
		}
		if (buildpack != null) {
			properties.setBuildpack(buildpack);
		}
	}

	@SuppressWarnings("unchecked")
	protected void readApplicationURL(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		/*
		 * Check for "no-route: true". If set then uris list should be empty
		 */
		Boolean noRoute = getValue(application, NO_ROUTE_PROP, Boolean.class);
		if (noRoute == null) {
			noRoute = getValue(allResults, NO_ROUTE_PROP, Boolean.class);
		}
		if (Boolean.TRUE.equals(noRoute)) {
			return;
		}

		HashSet<String> hostsSet = new LinkedHashSet<>();
		HashSet<String> domainsSet = new LinkedHashSet<>();

		/*
		 * Gather domains from app node and root node from 'domain' and 'domains' attributes
		 */
		String domain = getValue(application, DOMAIN_PROP, String.class);
		if (domain == null) {
			domain = getValue(allResults, DOMAIN_PROP, String.class);
		}
		if (domain != null && isDomainValid(domain, domains)) {
			domainsSet.add(domain);
		}
		List<String> domainList = (List<String>) getValue(allResults, DOMAINS_PROP, List.class);
		if (domainList != null) {
			for (String d : domainList) {
				if (isDomainValid(d, domains)) {
					domainsSet.add(d);
				}
			}
		}
		domainList = (List<String>) getValue(application, DOMAINS_PROP, List.class);
		if (domainList != null) {
			for (String d : domainList) {
				if (isDomainValid(d, domains)) {
					domainsSet.add(d);
				}
			}
		}

		/*
		 * Gather domains from app node and root node from 'host' and 'hosts'
		 * attributes. Account for ${random} in host's name
		 */
		String host = getValue(application, SUB_DOMAIN_PROP, String.class);
		if (host == null) {
			host = getValue(allResults, SUB_DOMAIN_PROP, String.class);
		}
		if (host != null) {
			hostsSet.add(host);
		}
		List<String> hostList = (List<String>) getValue(allResults, SUB_DOMAINS_PROP, List.class);
		if (hostList != null) {
			hostsSet.addAll(hostList);
		}
		hostList = (List<String>) getValue(application, SUB_DOMAINS_PROP, List.class);
		if (hostList != null) {
			hostsSet.addAll(hostList);
		}

		/*
		 * If no host names found check for "random-route: true" and
		 * "no-hostname: true" otherwise take app name as the host name
		 */
		if (hostsSet.isEmpty()) {
			Boolean randomRoute = getValue(application, RANDOM_ROUTE_PROP, Boolean.class);
			if (randomRoute == null) {
				randomRoute = getValue(allResults, RANDOM_ROUTE_PROP, Boolean.class);
			}
			if (Boolean.TRUE.equals(randomRoute)) {
				hostsSet.add(extractHost("${random}", 10));
				domainsSet.clear();
				domainsSet.add(domains.get(0).getName());
			} else {
				Boolean noHostName = getValue(application, NO_HOSTNAME_PROP, Boolean.class);
				if (noHostName == null) {
					noHostName = getValue(allResults, NO_HOSTNAME_PROP, Boolean.class);
				}
				if (!Boolean.TRUE.equals(noHostName)) {
					/*
					 * Assumes name is set before URIs are processed
					 */
					hostsSet.add(properties.getAppName());
				}
			}
		}

		/*
		 * Set a domain if they are still empty
		 */
		if (domainsSet.isEmpty()) {
			domainsSet.add(domains.get(0).getName());
		}

		/*
		 * Compose URIs for application based on hosts and domains
		 */
		List<String> uris = new ArrayList<>(hostsSet.isEmpty() ? 1 : hostsSet.size() * domainsSet.size());
		for (String d : domainsSet) {
			if (hostsSet.isEmpty()) {
				uris.add(new CloudApplicationURL(null, d).getUrl());
			} else {
				for (String h : hostsSet) {
					uris.add(new CloudApplicationURL(h, d).getUrl());
				}
			}
		}

		properties.setUris(uris);

	}

	public static boolean isDomainValid(String domain, List<CloudDomain> domains) {
		for (CloudDomain cloudDomain : domains) {
			if (cloudDomain.getName().equals(domain)) {
				return true;
			}
		}
		return false;
	}

	private String extractHost(String subdomain, int length) {
		// Check for random word
		int varIndex = subdomain.indexOf(RANDOM_VAR);
		while (varIndex >= 0)  {
			String randomWord = RandomStringUtils.randomAlphabetic(length);
			subdomain = subdomain.replace(subdomain.substring(varIndex, RANDOM_VAR.length()), randomWord);
			varIndex = subdomain.indexOf(RANDOM_VAR);
		}
		return subdomain;
	}

	protected void readMemory(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) throws Exception {
		int memoryValue = readMemoryValue(application, allResults, MEMORY_PROP);
		if (memoryValue >= 0) {
			properties.setMemory(memoryValue);
		}
	}

	protected void readDiskQuota(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) throws Exception {
		int memoryValue = readMemoryValue(application, allResults, DISK_QUOTA_PROP);
		if (memoryValue >= 0) {
			properties.setDiskQuota(memoryValue);
		}
	}

	protected Integer readMemoryValue(Map<?, ?> application, Map<Object, Object> allResults,
			String propertyKey) throws Exception {

		int result = -1;

		// First see if there is a "common" memory value that applies to all
		// applications:

		Integer memoryVal = getValue(application, MEMORY_PROP, Integer.class);
		if (memoryVal == null) {
			memoryVal = getValue(allResults, MEMORY_PROP, Integer.class);
		}

		// If not in Integer form, try String as the memory may end in with a
		// 'G' or 'M'
		if (memoryVal == null) {
			String memoryStringVal = getValue(application, MEMORY_PROP, String.class);
			if (memoryStringVal == null) {
				memoryStringVal = getValue(allResults, MEMORY_PROP, String.class);
			}
			if (memoryStringVal != null && memoryStringVal.length() > 0) {
				memoryVal = convertMemory(memoryStringVal);
			}
		}

		if (memoryVal != null) {
			switch (memoryVal.intValue()) {
			case 1:
				result = 1024;
				break;
			case 2:
				result = 2048;
				break;
			default:
				result = memoryVal.intValue();
				break;
			}
		}

		return result;
	}

	public static int convertMemory(String memoryStringVal) throws CoreException {
		char memoryIndicator[] = { 'M', 'G', 'm', 'g' };
		int gIndex = -1;

		for (char indicator : memoryIndicator) {
			gIndex = memoryStringVal.indexOf(indicator);
			if (gIndex >= 0) {
				break;
			}
		}

		// There has to be a number before the 'G' or 'M', if 'G' or 'M'
		// is used, or its not a valid
		// memory
		if (gIndex > 0) {
			memoryStringVal = memoryStringVal.substring(0, gIndex);
		} else if (gIndex == 0) {
			throw ExceptionUtil.coreException("Failed to read memory value. Invalid memory: " + memoryStringVal);
		}

		try {
			return Integer.valueOf(memoryStringVal);
		} catch (NumberFormatException e) {
			throw ExceptionUtil.coreException("Failed to parse memory due to: " + e.getMessage());
		}
	}

	/**
	 *
	 * @return map of parsed manifest file, if the file exists. If the file does
	 *         not exist, return null.
	 * @throws CoreException
	 *             if manifest file exists, but error occurred that prevents a
	 *             map to be generated.
	 */
	protected Map<Object, Object> parseManifestFromFile() throws Exception {

		InputStream inputStream = getInputStream();

		if (inputStream != null) {
			Yaml yaml = new Yaml();

			try {
				Object results = yaml.load(inputStream);

				if (results instanceof Map<?, ?>) {
					return (Map<Object, Object>) results;
				} else {
					String source = manifestFile == null ? "entered manifest" : "file " + manifestFile.getFullPath();
					throw ExceptionUtil.coreException("Expected a map of values for "
							+ source + ". Unable to load manifest content.  Actual results: " + results);
				}

			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}

		}
		return null;
	}

	static protected String getMemoryAsString(int memory) {
		if (memory < 1) {
			return null;
		}
		return memory + "M";
	}
}
