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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.Staging;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertiesMapper;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class CloudFoundryBootDashModel extends BootDashModel implements ModifiableModel {

	private IPropertyStore modelStore;

	private CloudBootDashOperations operations;

	public static final String PROJECT_TO_APP_MAPPING = "projectToAppMapping";

	public CloudFoundryBootDashModel(CloudFoundryRunTarget target, BootDashModelContext context) {
		super(target);
		RunTargetType type = target.getType();
		IPropertyStore typeStore = PropertyStoreFactory.createForScope(type, context.getRunTargetProperties());
		this.modelStore = PropertyStoreFactory.createSubStore(target.getId(), typeStore);
		this.operations = new CloudBootDashOperations(context);
	}

	private LiveSet<BootDashElement> elements;

	@Override
	public LiveSet<BootDashElement> getElements() {

		if (elements == null) {
			elements = new LiveSet<BootDashElement>();
			elements.addListener(new ProjectAppMappingListener(this.modelStore));

			asyncRefreshElements();
		}
		return elements;
	}

	protected void asyncRefreshElements() {
		if (elements == null) {
			return;
		}

		Operation<Void> op = new Operation<Void>(
				"Refreshing list of Cloud applications for: " + getRunTarget().getName()) {

			@Override
			protected Void runOp(IProgressMonitor monitor) throws Exception {
				try {

					List<CloudApplication> apps = getCloudTarget().getClient().getApplications();
					List<BootDashElement> updatedElements = new ArrayList<BootDashElement>();

					Map<String, String> existingProjectToAppMappings = new HashMap<String, String>();
					PropertiesMapper<Map<String, String>> propertiesMapper = new PropertiesMapper<Map<String, String>>();
					String storedVal = modelStore.get(PROJECT_TO_APP_MAPPING);
					if (storedVal != null) {
						Map<String, String> mappings = propertiesMapper.convert(storedVal);
						if (mappings != null) {
							existingProjectToAppMappings.putAll(mappings);
						}
					}

					if (apps != null) {
						for (CloudApplication app : apps) {

							String projectName = existingProjectToAppMappings.get(app.getName());
							IProject project = null;
							if (projectName != null) {
								project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
								if (project != null && !project.isAccessible()) {
									project = null;
								}
							}

							updatedElements.add(new CloudDashElement(CloudFoundryBootDashModel.this, app, project,
									operations, modelStore));
						}
					}

					elements.replaceAll(updatedElements);

				} catch (Exception e) {
					BootDashActivator.log(e);
				}
				return null;
			}

		};
		operations.runRefreshOperation(op);
	}

	@Override
	public void dispose() {
		elements = null;
	}

	@Override
	public void refresh() {
		asyncRefreshElements();
	}

	public CloudFoundryRunTarget getCloudTarget() {
		return (CloudFoundryRunTarget) getRunTarget();
	}

	@Override
	public boolean canAccept(List<Object> source, Object target) {
		if (!source.isEmpty()) {
			for (Object obj : source) {
				if (getJavaProject(obj) == null) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void add(List<Object> sources, Object target, UserInteractions ui) throws Exception {
		// For now, only support new deployments (no mapping to existing apps in
		// CF)
		// Therefore ignore the target
		List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		if (sources != null) {
			for (Object obj : sources) {
				IJavaProject javaProject = getJavaProject(obj);
				if (javaProject != null) {
					javaProjects.add(javaProject);
				}
			}

			performDeployment(javaProjects, ui);
		}
	}

	protected IJavaProject getJavaProject(Object source) {
		if (source instanceof IJavaProject) {
			return (IJavaProject) source;
		} else if (source instanceof BootDashElement) {
			return ((BootDashElement) source).getJavaProject();
		}
		return null;
	}

	public void performDeployment(final List<IJavaProject> projectsToDeploy, UserInteractions ui) throws Exception {
		CloudOperation<Void> op = new CloudOperation<Void>("Deploying projects to " + getRunTarget().getName(),
				getCloudTarget().getClient(), ui) {

			@Override
			protected Void doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor) throws Exception {

				SubMonitor subMonitor = SubMonitor.convert(monitor, projectsToDeploy.size() * 100);
				for (IJavaProject javaProject : projectsToDeploy) {

					// IMPORTANT: the manifest.yml may specify ANOTHER app name,
					// therefore do NOT assume the
					// project name will be the app name
					ManifestParser parser = new ManifestParser(javaProject.getProject());
					CloudDeploymentProperties deploymentProperties = parser.load(monitor);

					if (deploymentProperties == null) {
						throw BootDashActivator.asCoreException(
								"No Cloud deployment properties parsed from manifest.yml file. Please ensure the project contains a valid manifest.yml");
					}

					IStatus status = deploymentProperties.validate();
					if (!status.isOK()) {
						throw new CoreException(status);
					}

					// See if the application exists
					String appName = deploymentProperties.getAppName();

					CloudApplication app = null;
					try {
						app = client.getApplication(appName);
						subMonitor.worked(20);

					} catch (Throwable t) {
						// Ignore. Apps that dont exist throw exception. If any
						// other error occurs (network I/O)
						// it will be thrown in further operations below
					}

					if (app == null) {

						if (deploymentProperties != null) {
							client.createApplication(deploymentProperties.getAppName(),
									new Staging(null, deploymentProperties.getBuildpackUrl()),
									deploymentProperties.getMemory(), deploymentProperties.getUrls(),
									deploymentProperties.getServices());
						}
					}

					// get the created app to verify it exists as well as fetch
					// the updated version
					app = client.getApplication(appName);

					// Upload the application
					CloudZipApplicationArchive archive = null;

					try {
						CloudApplicationArchiver archiver = new CloudApplicationArchiver(javaProject, app.getName(),
								parser);
						archive = archiver.getApplicationArchive(subMonitor.newChild(60));
						if (archive != null) {
							client.uploadApplication(appName, archive);

							// start the app
							client.startApplication(appName);

							// if it successfully created add it to elements
							// AFTER it uploads to ensure correct state
							addElement(app, javaProject.getProject());
						} else {
							throw BootDashActivator
									.asCoreException("Failed to generate application archive for " + appName);
						}
					} finally {
						// IMPORTANT: MUST close the archive to avoid resource
						// leakage and
						// potential bug were the same archive file keeps being
						// pushes even if the
						// archive file changes
						if (archive != null) {
							archive.close();
						}
					}

					subMonitor.worked(20);

				}
				return null;
			}
		};
		operations.runDeploymentOperation(op);
	}

	protected void addElement(CloudApplication app, IProject project) {
		if (elements != null) {
			Set<BootDashElement> existing = elements.getValue();
			Set<BootDashElement> copy = new HashSet<BootDashElement>();
			// Replace the existing one with a new one for the given Cloud
			// Application
			for (BootDashElement element : existing) {
				if (!app.getName().equals(element.getName())) {
					copy.add(element);
				}
			}
			copy.add(new CloudDashElement(this, app, project, operations, modelStore));
			elements.addAll(copy);
		}

	}

	/*
	 * Project Mapping Storage.
	 *
	 * TODO: This can be refactored and be re-used by Lattice and other models
	 * as well since it is general and not Cloud Foundry specific.
	 *
	 *
	 */

	class ProjectAppMappingListener implements ValueListener<Set<BootDashElement>> {

		private final IPropertyStore modelStore;

		public ProjectAppMappingListener(IPropertyStore modelStore) {
			this.modelStore = modelStore;
		}

		@Override
		public void gotValue(LiveExpression<Set<BootDashElement>> exp, Set<BootDashElement> val) {

			try {
				storeProjectToAppMappings(val);
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
		}

		protected synchronized void storeProjectToAppMappings(Collection<BootDashElement> elements) throws Exception {
			PropertiesMapper<Map<String, String>> propertiesMapper = new PropertiesMapper<Map<String, String>>();
			Map<String, String> projectsToApps = new HashMap<String, String>();

			if (elements != null) {
				for (BootDashElement element : elements) {
					IProject project = element.getProject();
					if (project != null) {
						projectsToApps.put(element.getName(), project.getName());
					}
				}
			}

			String asValue = propertiesMapper.convertToString(projectsToApps);
			modelStore.put(PROJECT_TO_APP_MAPPING, asValue);
		}
	}
}
