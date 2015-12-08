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

import java.util.List;

import org.eclipse.core.resources.IProject;

/**
 * Deployment properties for an app that can be prompted to a user. This
 * distinguishes from {@link CloudApplicationDeploymentProperties} in the sense
 * that the latter can represent a "complete" properties and can be resolved
 * from existing apps already in CF.
 * <p/>
 * The user-defined properties are a subset of the
 * {@link CloudApplicationDeploymentProperties} and allows for easier merging of
 * user properties into the existing CF properties without accidentally deleting
 * information in the existing version that is not user-specified.
 * <p/>
 * For example, if the user defined properties does not contain environment
 * variables but the existing version does, merging user defined into the
 * existing version, the existing environment variables will not be deleted,
 * since the user-defined properties contains no API to get env vars.
 *
 */
public class UserDefinedDeploymentProperties extends BaseDeploymentProperties {
	private boolean writeManifest = false;

	public UserDefinedDeploymentProperties(IProject project) {
		super(project);
	}

	public boolean writeManifest() {
		return writeManifest;
	}

	public void setWriteManifest(boolean writeManifest) {
		this.writeManifest = writeManifest;
	}

	public CloudApplicationDeploymentProperties asCloudAppDeploymentProperties() {
		CloudApplicationDeploymentProperties properties = new CloudApplicationDeploymentProperties(this.getProject());
		properties.setAppName(this.getAppName());
		properties.setUrls(this.getUrls());
		properties.setMemory(this.getMemory());
		return properties;
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
}
