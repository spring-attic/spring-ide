/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

public class CloudApplicationDeploymentProperties {

	public static final int DEFAULT_INSTANCES = 1;

	protected final LiveSet<String> boundServices = new LiveSet<String>(new HashSet<String>());
	protected final LiveVariable<Map<String, String>> environmentVariables = new LiveVariable<Map<String, String>>(
			new HashMap<String, String>());
	protected final LiveVariable<String> buildpack = new LiveVariable<String>("");

	protected final LiveVariable<Integer> instances = new LiveVariable<Integer>(DEFAULT_INSTANCES);

	private LiveVariable<Boolean> writeManifest = new LiveVariable<>(false);

	public static final int DEFAULT_MEMORY = 1024;

	/*
	 * URLs should never be null. If no URLs are needed, keep list empty
	 */
	protected LiveSet<String> urls = new LiveSet<String>();

	protected LiveVariable<String> appName = new LiveVariable<>();

	protected LiveVariable<IProject> project = new LiveVariable<>();

	protected final LiveVariable<Integer> memory = new LiveVariable<Integer>(DEFAULT_MEMORY);

	protected Validator validator;

	public CloudApplicationDeploymentProperties() {

	}

	/*
	 * Additional properties
	 */

	private boolean shouldRestart = true;

	public void setProject(IProject project) {
		this.project.setValue(project);
	}

	public IProject getProject() {
		return this.project.getValue();
	}

	public void setMemory(int memory) {
		this.memory.setValue(memory);
	}

	public int getMemory() {
		return memory.getValue();
	}

	/**
	 * Returns a copy of the list of URLs for the application
	 *
	 * @return never null
	 */
	public List<String> getUrls() {
		return this.urls.getValues();
	}

	public void setUrls(List<String> urls) {
		if (urls == null) {
			urls = new ArrayList<String>();
		}
		this.urls.addAll(urls);
	}

	public void setAppName(String appName) {
		this.appName.setValue(appName);
	}

	public String getAppName() {
		return this.appName.getValue();
	}

	/**
	 *
	 * @return non-null validator
	 */
	public Validator getValidator() {
		return this.validator == null ? this.validator = new BasicValidator() : this.validator;
	}

	public void setBuildpack(String buildpack) {
		this.buildpack.setValue(buildpack);
	}

	public void setServices(List<String> services) {
		if (services == null) {
			services = new ArrayList<String>();
		}
		this.boundServices.addAll(services);
	}

	public void setInstances(int instances) {
		this.instances.setValue(instances);
	}

	public void setEnvironmentVariables(Map<String, String> environmentVariables) {
		if (environmentVariables == null) {
			environmentVariables = new HashMap<String, String>();
		}
		this.environmentVariables.setValue(environmentVariables);
	}

	public void setShouldRestart(boolean shouldRestart) {
		this.shouldRestart = shouldRestart;
	}

	public boolean shouldRestart() {
		return this.shouldRestart;
	}

	public String getBuildpack() {
		return buildpack.getValue();
	}

	public int getInstances() {
		return instances.getValue();
	}

	public boolean writeManifest() {
		return writeManifest.getValue();
	}

	public void setWriteManifest(boolean writeManifest) {
		this.writeManifest.setValue(writeManifest);
	}

	public void addValidator(Validator validator) {

		this.validator = validator;

		if (validator != null) {
			validator.dependsOn(boundServices);
			validator.dependsOn(environmentVariables);
			validator.dependsOn(buildpack);
			validator.dependsOn(memory);
			validator.dependsOn(memory);
			validator.dependsOn(urls);
			validator.dependsOn(appName);
			validator.dependsOn(project);
		}
	}

	/**
	 *
	 * @return never null
	 */
	public Map<String, String> getEnvironmentVariables() {
		return environmentVariables.getValue();
	}

	/**
	 *
	 * @return never null
	 */
	public List<String> getServices() {
		return boundServices.getValues();
	}

	/**
	 * Merges properties of this source into a target properties. Properties
	 * like application name and project in the user defined version replace
	 * those in the target one. However, URLs and other "collections" are merged
	 * into the target collections, rather than replace them.
	 *
	 * @param toMerge
	 *            target properties that will be updated with properties from
	 *            this source.
	 * @return merged properties or null if no properties to merge
	 */
	public CloudApplicationDeploymentProperties mergeInto(CloudApplicationDeploymentProperties target) {
		if (target == null) {
			return null;
		}

		target.setAppName(this.getAppName());
		target.setProject(this.getProject());

		// Instead of replacing URLs, merge them
		List<String> targetUrls = target.getUrls();
		for (String url : this.getUrls()) {
			if (!targetUrls.contains(url)) {
				targetUrls.add(url);
			}
		}
		target.setUrls(targetUrls);
		return target;

	}

	public static CloudApplicationDeploymentProperties getFor(CloudApplication app, IProject project) throws Exception {

		CloudApplicationDeploymentProperties properties = new CloudApplicationDeploymentProperties();

		properties.setAppName(app.getName());
		properties.setProject(project);
		properties.setBuildpack(app.getStaging() != null ? app.getStaging().getBuildpackUrl() : null);
		properties.setEnvironmentVariables(app.getEnvAsMap());
		properties.setInstances(app.getInstances());
		properties.setMemory(app.getMemory());
		properties.setServices(app.getServices());
		properties.setUrls(app.getUris());
		Validator validator = properties.getValidator();

		ValidationResult result = validator.getValue();
		if (!result.isOk()) {
			throw BootDashActivator.asCoreException(result.msg);
		}

		return properties;
	}

	class BasicValidator extends DeploymentPropertiesValidator {

		@Override
		protected CloudApplicationDeploymentProperties getProperties() {
			return CloudApplicationDeploymentProperties.this;
		}

	}

}
