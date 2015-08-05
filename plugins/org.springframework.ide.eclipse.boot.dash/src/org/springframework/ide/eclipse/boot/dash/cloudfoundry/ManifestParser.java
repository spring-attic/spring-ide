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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.yaml.snakeyaml.Yaml;

public class ManifestParser {

	public static final String APPLICATIONS_PROP = "applications";

	public static final String NAME_PROP = "name";

	public static final String MEMORY_PROP = "memory";

	public static final String INSTANCES_PROP = "instances";

	public static final String SUB_DOMAIN_PROP = "host";

	public static final String DOMAIN_PROP = "domain";

	public static final String SERVICES_PROP = "services";

	public static final String LABEL_PROP = "label";

	public static final String PROVIDER_PROP = "provider";

	public static final String VERSION_PROP = "version";

	public static final String PLAN_PROP = "plan";

	public static final String PATH_PROP = "path";

	public static final String BUILDPACK_PROP = "buildpack";

	public static final String ENV_PROP = "env";

	private final IProject project;

	private final String manifestPath;

	private final List<CloudDomain> domains;

	public static final String DEFAULT_PATH = "manifest.yml";

	public ManifestParser(IProject project, List<CloudDomain> domains) {
		this(project, domains, DEFAULT_PATH);
	}

	public ManifestParser(IProject project, List<CloudDomain> domains, String manifestPath) {
		Assert.isNotNull(project);

		this.project = project;
		this.manifestPath = manifestPath;
		this.domains = domains;
	}

	protected InputStream getInputStream() throws Exception {

		File file = getManifestFile();
		if (file != null && file.exists()) {
			return new FileInputStream(file);
		} else {
			throw BootDashActivator.asCoreException("No manifest.yml file found in project: " + project.getName());
		}
	}

	protected OutputStream getOutStream() throws IOException, FileNotFoundException {
		File file = getManifestFile();
		if (file != null) {
			if (file.exists()) {
				file.delete();
			}

			if (file.createNewFile()) {
				return new FileOutputStream(file);
			}
		}

		return null;
	}

	protected File getManifestFile() {

		IResource resource = project.getFile(DEFAULT_PATH);
		if (resource != null) {
			URI locationURI = resource.getLocationURI();
			return new File(locationURI);

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
	public String getApplicationProperty(Map<?, ?> app, String propertyName) {
		try {
			if (app != null) {
				return getStringValue(app, propertyName);
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
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

	protected String getStringValue(Map<?, ?> containingMap, String propertyName) {

		if (containingMap == null) {
			return null;
		}

		Object valObj = containingMap.get(propertyName);

		if (valObj instanceof String) {
			return (String) valObj;
		}
		return null;
	}

	protected Integer getIntegerValue(Map<?, ?> containingMap, String propertyName) {

		if (containingMap == null) {
			return null;
		}

		Object valObj = containingMap.get(propertyName);

		if (valObj instanceof Integer) {
			return (Integer) valObj;
		}
		return null;
	}

	protected List<Map<?, ?>> getApplications(Map<Object, Object> results) throws Exception {

		Object applicationsObj = results.get(APPLICATIONS_PROP);
		if (!(applicationsObj instanceof List<?>)) {
			throw BootDashActivator.asCoreException("Expected a top-level list of applications in: " + manifestPath
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
			Map<Object, Object> allResults, SubMonitor subMonitor) throws Exception {

		CloudApplicationDeploymentProperties properties = new CloudApplicationDeploymentProperties();
		properties.setProject(project);

		String appName = getStringValue(appMap, NAME_PROP);

		subMonitor.worked(1);
		if (appName != null) {
			properties.setAppName(appName);
		} else {
			throw BootDashActivator
					.asCoreException("No application name found in manifest.yml. An application name is required.");

		}

		readMemory(appMap, allResults, properties);
		subMonitor.worked(1);

		readApplicationURL(appMap, allResults, properties);
		subMonitor.worked(1);

		readBuildpackURL(appMap, allResults, properties);

		readEnvVars(appMap, allResults, properties);
		subMonitor.worked(1);

		readServices(appMap, allResults, properties);
		subMonitor.worked(1);

		readInstances(appMap, allResults, properties);

		return properties;
	}

	public List<CloudApplicationDeploymentProperties> load(IProgressMonitor monitor) throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask("Loading manifest.yml", 6);

		try {

			Map<Object, Object> allResults = parseManifestFromFile();

			if (allResults == null || allResults.isEmpty()) {
				throw BootDashActivator
						.asCoreException("No content found in manifest.yml. Make sure the manifest is valid.");

			}

			List<Map<?, ?>> appMaps = getApplications(allResults);

			if (appMaps == null || appMaps.isEmpty()) {
				throw BootDashActivator.asCoreException(
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

	protected void readEnvVars(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		Map<?, ?> propertiesMap = getContainingPropertiesMap(allResults, ENV_PROP);
		if (propertiesMap == null) {
			propertiesMap = getContainingPropertiesMap(application, ENV_PROP);
		}

		if (propertiesMap == null) {
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

		Integer instances = getIntegerValue(allResults, INSTANCES_PROP);
		if (instances == null) {
			instances = getIntegerValue(application, INSTANCES_PROP);
		}
		if (instances != null) {
			properties.setInstances(instances);
		}
	}

	protected void readBuildpackURL(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		String buildpackurl = getStringValue(allResults, BUILDPACK_PROP);
		if (buildpackurl == null) {
			buildpackurl = getStringValue(application, BUILDPACK_PROP);
		}
		if (buildpackurl != null) {
			properties.setBuildpackUrl(buildpackurl);
		}
	}

	protected void readApplicationURL(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		// See if there is a common domain defined for all apps
		String domain = getStringValue(allResults, DOMAIN_PROP);
		if (domain == null) {
			domain = getStringValue(application, DOMAIN_PROP);
		}

		String subdomain = getStringValue(application, SUB_DOMAIN_PROP);

		// A URL can only be constructed from the manifest if either a domain or
		// a subdomain is specified. If neither is specified, but the app name
		// is, the app name is used as the subdomain.Otherwise the
		// deployment process will generate a URL from the app name, but it is
		// not necessary to specify that URL here.
		if (subdomain == null && domain == null && properties.getAppName() == null) {
			return;
		}
		CloudApplicationURL cloudURL = null;
		if (subdomain == null) {
			subdomain = properties.getAppName();
		}

		if (domain == null && !domains.isEmpty()) {
			// If no domain is specified get a URL with a default domain
			domain = domains.get(0).getName();
		}

		cloudURL = new CloudApplicationURL(subdomain, domain);

		List<String> urls = Arrays.asList(cloudURL.getUrl());
		properties.setUrls(urls);
	}

	protected void readMemory(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) throws Exception {

		// First see if there is a "common" memory value that applies to all
		// applications:

		Integer memoryVal = getIntegerValue(allResults, MEMORY_PROP);
		if (memoryVal == null) {
			memoryVal = getIntegerValue(application, MEMORY_PROP);
		}

		// If not in Integer form, try String as the memory may end in with a
		// 'G' or 'M'
		if (memoryVal == null) {
			String memoryStringVal = getStringValue(allResults, MEMORY_PROP);
			if (memoryStringVal == null) {
				memoryStringVal = getStringValue(application, MEMORY_PROP);
			}
			if (memoryStringVal != null && memoryStringVal.length() > 0) {

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
					throw BootDashActivator.asCoreException("Failed to read memory value in manifest file: "
							+ manifestPath + ". Invalid memory: " + memoryStringVal);
				}

				try {
					memoryVal = Integer.valueOf(memoryStringVal);
				} catch (NumberFormatException e) {
					throw BootDashActivator.asCoreException("Failed to parse memory from manifest file: " + manifestPath
							+ " due to: " + e.getMessage());
				}
			}
		}

		if (memoryVal != null) {
			int actualMemory = -1;
			switch (memoryVal.intValue()) {
			case 1:
				actualMemory = 1024;
				break;
			case 2:
				actualMemory = 2048;
				break;
			default:
				actualMemory = memoryVal.intValue();
				break;
			}
			if (actualMemory > 0) {
				properties.setMemory(actualMemory);
			}
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
					throw BootDashActivator.asCoreException("Expected a map of values for manifest file: "
							+ manifestPath + ". Unable to load manifest content.  Actual results: " + results);
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

	protected String getMemoryAsString(int memory) {
		if (memory < 1) {
			return null;
		}
		return memory + "M";
	}
}
