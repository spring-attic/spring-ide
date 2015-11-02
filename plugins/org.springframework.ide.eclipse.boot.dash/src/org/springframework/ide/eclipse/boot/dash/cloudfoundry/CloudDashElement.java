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
import java.util.Arrays;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement.CloudElementIdentity;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationStartOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationStopOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CompositeApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.FullApplicationRestartOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.RemoteDevClientStartOperation;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
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
		// Note some stop operations are part of a composite operation that has
		// a preferred runState (e.g. STARTING)
		// For example, as part of a large restart operation, an app may be
		// first stopped. However, in these cases
		// the app run state in the model should not be updated.
		// But when directly stopped through element API, ensure that the app
		// run state IS indeed updated to show that
		// it is stopped
		boolean updateElementRunStateInModel = true;
		CloudApplicationOperation op = new CompositeApplicationOperation(new ApplicationStopOperation(this.getName(),
				(CloudFoundryBootDashModel) getParent(), updateElementRunStateInModel));
		cloudModel.getOperationsExecution(ui).runOpAsynch(op);
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {

		Operation<?> op = null;
		// Only do full upload on restart. Not on debug
		if (getProject() != null
		// TODO: commenting out for now as restarting doesnt seem to restage.
		// Need to re-stage for JAVA_OPTS in debugging to be set. right now that
		// is done
		// through uploading via full deployment
		// && runingOrDebugging == RunState.RUNNING
		) {
			String opName = "Starting application '" + getName() + "' in "
					+ (runingOrDebugging == RunState.DEBUGGING ? "DEBUG" : "RUN") + " mode";
			if (runingOrDebugging == RunState.DEBUGGING) {
				op = new CompositeApplicationOperation(opName, cloudModel, getName(),
						Arrays.asList(new CloudApplicationOperation[] {
								new FullApplicationRestartOperation(opName, cloudModel, getName(), runingOrDebugging,
										ui),
								new RemoteDevClientStartOperation(cloudModel, getName(), runingOrDebugging) }), RunState.STARTING);
			} else {
				op = new FullApplicationRestartOperation(opName, cloudModel, getName(), runingOrDebugging, ui);
			}
		} else {
			// Set the initial run state as Starting
			CloudApplicationOperation restartOp = new ApplicationStartOperation(getName(),
					(CloudFoundryBootDashModel) getParent(), RunState.STARTING);

			op = new CompositeApplicationOperation(restartOp);
		}

		cloudModel.getOperationsExecution(ui).runOpAsynch(op);
	}

	public void restartOnly(RunState runingOrDebugging, UserInteractions ui) throws Exception {

		CloudApplicationOperation restartOp = new ApplicationStartOperation(getName(),
				(CloudFoundryBootDashModel) getParent(), RunState.STARTING);

		cloudModel.getOperationsExecution(ui).runOpAsynch(new CompositeApplicationOperation(restartOp));
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
		RunState state = getCloudModel().getAppCache().getRunState(getName());
		if (state == RunState.RUNNING) {
			if (DevtoolsUtil.isDevClientAttached(this, ILaunchManager.DEBUG_MODE)) {
				state = RunState.DEBUGGING;
			}
		}
		return state;
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

		public String toString() {
			return appName + "@" + runTarget;
		};

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
