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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement.CloudElementIdentity;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationOperationWithModelUpdate;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationStartOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationStopOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ProjectsDeployer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.StartOnlyUpdateListener;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

/**
 * A handle to a Cloud application. NOTE: This element should NOT hold Cloud
 * application state as it may be discarded and created multiple times for the
 * same app for any reason.
 * <p/>
 * Cloud application state should always be resolved from external sources
 *
 */
public class CloudDashElement extends WrappingBootDashElement<CloudElementIdentity> {

	private final CloudFoundryRunTarget cloudTarget;

	private final CloudFoundryBootDashModel cloudModel;

	private PropertyStoreApi persistentProperties;

	public CloudDashElement(CloudFoundryBootDashModel model, String appName, IPropertyStore modelStore) {
		super(model, new CloudElementIdentity(appName, model.getRunTarget()));
		this.cloudTarget = model.getCloudTarget();
		this.cloudModel = model;
		IPropertyStore backingStore = PropertyStoreFactory.createSubStore(getName(), modelStore);
		this.persistentProperties = PropertyStoreFactory.createApi(backingStore);
	}

	protected CloudFoundryBootDashModel getCloudModel() {
		return (CloudFoundryBootDashModel) getParent();
	}

	@Override
	public void stopAsync(UserInteractions ui) throws Exception {
		CloudApplicationOperation op = new ApplicationOperationWithModelUpdate(
				new ApplicationStopOperation(this, (CloudFoundryBootDashModel) getParent()), false);
		cloudModel.getOperationsExecution(ui).runOpAsynch(op);
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {

		Operation<?> op = null;
		// Only do full upload on restart. Not on debug
		if (getProject() != null && runingOrDebugging == RunState.RUNNING) {
			boolean shouldAutoReplaceApp = true;
			List<BootDashElement> elements = new ArrayList<BootDashElement>();
			elements.add(this);
			op = new ProjectsDeployer((CloudFoundryBootDashModel) getParent(), ui, elements, shouldAutoReplaceApp, runingOrDebugging);
		} else {
			CloudApplicationOperation restartOp = new ApplicationStartOperation(getName(),
					(CloudFoundryBootDashModel) getParent(), runingOrDebugging);

			restartOp.addApplicationUpdateListener(new StartOnlyUpdateListener(getName(), getCloudModel()));

			op = new ApplicationOperationWithModelUpdate(restartOp, true);
		}

		cloudModel.getOperationsExecution(ui).runOpAsynch(op);
	}

	public void restartOnly(RunState runingOrDebugging, UserInteractions ui) throws Exception {

		CloudApplicationOperation restartOp = new ApplicationStartOperation(getName(),
				(CloudFoundryBootDashModel) getParent(), runingOrDebugging);

		restartOp.addApplicationUpdateListener(new StartOnlyUpdateListener(getName(), getCloudModel()));

		cloudModel.getOperationsExecution(ui).runOpAsynch(new ApplicationOperationWithModelUpdate(restartOp, true));
	}

	@Override
	public void openConfig(UserInteractions ui) {

	}

	@Override
	public String getName() {
		return delegate.getAppName();
	}

	@Override
	public IProject getProject() {
		return getCloudModel().getAppCache().getProject(getName());
	}

	@Override
	public RunState getRunState() {
		return getCloudModel().getAppCache().getRunState(getName());
	}

	public CloudApplicationDeploymentProperties getDeploymentProperties() {
		CloudApplication app = getCloudModel().getAppCache().getApp(getName());
		if (app != null) {
			return CloudApplicationDeploymentProperties.getFor(app);
		}

		return null;
	}

	@Override
	public RunTarget getTarget() {
		return cloudTarget;
	}

	@Override
	public int getLivePort() {
		return 80;
	}

	@Override
	public String getLiveHost() {
		CloudApplication app = getCloudModel().getAppCache().getApp(getName());
		if (app != null) {
			List<String> uris = app.getUris();
			if (uris != null) {
				for (String uri : uris) {
					return uri;
				}
			}
		}
		return null;
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		return new ArrayList<RequestMapping>(0);
	}

	@Override
	public ILaunchConfiguration getActiveConfig() {
		return null;
	}

	@Override
	public ILaunchConfiguration getPreferredConfig() {
		return null;
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {

	}

	/**
	 * Add a set of environment variables to the existing environment variables
	 * of an application.
	 *
	 * @param vars
	 *            to add to existing variables.
	 * @param monitor
	 *            should be non-null. This requires update to CF so it may take
	 *            time.
	 * @throws Exception
	 *             if app does not exist (e.g. model is out of synch with Cloud
	 *             Foundry), or failure to add env vars.
	 */
	public void addEnvironmentVariables(final Map<String, String> toAdd, IProgressMonitor monitor) throws Exception {
		// Nothing to add , avoid CF calls
		if (toAdd == null || toAdd.isEmpty()) {
			return;
		}
		final String opName = "Updating " + getName() + " environment variables.";

		new CloudApplicationOperation(opName, (CloudFoundryBootDashModel) getParent(), getName()) {

			@Override
			protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				CloudAppInstances instances = getCachedApplication();
				if (instances == null) {
					instances = getCloudApplicationInstances();
				}

				if (instances == null) {
					throw BootDashActivator.asCoreException(
							"Unable to update environment variables. No application found in Cloud Foundry for: "
									+ CloudDashElement.this.getName()
									+ ". View appears to be out of synch with Cloud Foundry. Please refresh and try again.");
				}

				Map<String, Object> existingVars = getClient()
						.getApplicationEnvironment(instances.getApplication().getMeta().getGuid());

				Map<String, String> varsToUpdate = new HashMap<String, String>();
				if (existingVars != null) {
					for (Entry<String, Object> var : existingVars.entrySet()) {
						varsToUpdate.put(var.getKey(), var.getValue().toString());
					}
				}

				varsToUpdate.putAll(toAdd);

				getClient().updateApplicationEnv(appName, varsToUpdate);
				((CloudFoundryBootDashModel) getParent()).updateApplication(getCloudApplicationInstances());
			}
		}.run(monitor);
	}

	@Override
	public int getActualInstances() {
		return getCloudModel().getAppCache().getApp(getName()) != null
				? getCloudModel().getAppCache().getApp(getName()).getRunningInstances() : 0;
	}

	@Override
	public int getDesiredInstances() {
		return getCloudModel().getAppCache().getApp(getName()) != null
				? getCloudModel().getAppCache().getApp(getName()).getInstances() : 0;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}

	static class CloudElementIdentity {

		private final String appName;
		private final RunTarget runTarget;

		CloudElementIdentity(String appName, RunTarget runTarget) {
			this.appName = appName;
			this.runTarget = runTarget;
		}

		public String getAppName() {
			return this.appName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appName == null) ? 0 : appName.hashCode());
			result = prime * result + ((runTarget == null) ? 0 : runTarget.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CloudElementIdentity other = (CloudElementIdentity) obj;
			if (appName == null) {
				if (other.appName != null)
					return false;
			} else if (!appName.equals(other.appName))
				return false;
			if (runTarget == null) {
				if (other.runTarget != null)
					return false;
			} else if (!runTarget.equals(other.runTarget))
				return false;
			return true;
		}

	}
}
