/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudOperation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class SelectManifestOp extends CloudOperation {

	protected final CloudAppDashElement cde;
	protected final UserInteractions ui;

	public SelectManifestOp(CloudAppDashElement cde, UserInteractions ui) {
		super("Select a manifest file", cde.getCloudModel());
		this.cde = cde;
		this.ui = ui;
	}

	@Override
	protected void doCloudOp(final IProgressMonitor monitor) throws Exception, OperationCanceledException {

		IProject project = cde.getProject();

		if (cde == null || project == null) {
			return;
		}

		IFile manifest = cde.getDeploymentManifestFile();

		Map<Object, Object> yaml = Collections.emptyMap();

		/*
		 * Commented out because manual manifest contents based on current
		 * deployment props from CF don't need to be the latest since they are
		 * not editable
		 */
		//		/*
//		 * Refresh the latest cloud application
//		 */
//		new RefreshApplications(model, Collections.singletonList(model.getAppCache().getApp(project))).run(monitor);

		try {
			List<CloudDomain> domains = model.getRunTarget().getDomains(monitor);
			yaml = ApplicationManifestHandler.toYaml(CloudApplicationDeploymentProperties.getFor(project, domains, model.getAppCache().getApp(project)), domains);
		} catch (Exception e) {
			// ignore
		}

		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);

		String defaultManifest = new Yaml(options).dump(yaml);

		CloudApplicationDeploymentProperties props = ui.promptApplicationDeploymentProperties(
				cde.getCloudModel().getRunTarget().getDomains(monitor), project, manifest, defaultManifest,
				true, false);

		if (props == null) {
			throw ExceptionUtil.coreException("Error loading deployment properties from the manifest YAML");
		}

		cde.setDeploymentManifestFile(props.getManifestFile());
	}

}
