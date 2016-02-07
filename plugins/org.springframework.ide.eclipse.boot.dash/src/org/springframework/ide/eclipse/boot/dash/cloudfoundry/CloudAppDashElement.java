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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement.CloudAppIdentity;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ApplicationStopOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CompositeApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.util.LogSink;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.base.Objects;

/**
 * A handle to a Cloud application. NOTE: This element should NOT hold Cloud
 * application state as it may be discarded and created multiple times for the
 * same app for any reason.
 * <p/>
 * Cloud application state should always be resolved from external sources
 */
public class CloudAppDashElement extends WrappingBootDashElement<CloudAppIdentity> implements LogSink {

	static final private String DEPLOYMENT_MANIFEST_FILE_PATH = "deploymentManifestFilePath"; //$NON-NLS-1$

	private final CloudFoundryRunTarget cloudTarget;

	private final CloudFoundryBootDashModel cloudModel;

	private PropertyStoreApi persistentProperties;

	public CloudAppDashElement(CloudFoundryBootDashModel model, String appName, IPropertyStore modelStore) {
		super(model, new CloudAppIdentity(appName, model.getRunTarget()));
		this.cloudTarget = model.getRunTarget();
		this.cloudModel = model;
		IPropertyStore backingStore = PropertyStoreFactory.createSubStore("A"+getName(), modelStore);
		this.persistentProperties = PropertyStoreFactory.createApi(backingStore);
	}

	public CloudFoundryBootDashModel getCloudModel() {
		return (CloudFoundryBootDashModel) getBootDashModel();
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
				(CloudFoundryBootDashModel) getBootDashModel(), updateElementRunStateInModel));
		cloudModel.getOperationsExecution(ui).runOpAsynch(op);
	}

	@Override
	public void restart(RunState runingOrDebugging, UserInteractions ui) throws Exception {

		Operation<?> op = null;
		// TODO: Only do full upload on restart. Not on debug
		if (getProject() != null
		// TODO: commenting out for now as restarting doesnt seem to restage.
		// Need to re-stage for JAVA_OPTS in debugging to be set. right now that
		// is done
		// through uploading via full deployment
		// && runingOrDebugging == RunState.RUNNING
		) {
			op = cloudModel.getApplicationDeploymentOperations().restartAndPush(this, getDebugSupport(),
					runingOrDebugging, ui);
		} else {
			// Set the initial run state as Starting
			op =  cloudModel.getApplicationDeploymentOperations().restartOnly(getProject(),
					getName(), RunState.STARTING);
		}

		cloudModel.getOperationsExecution(ui).runOpAsynch(op);
	}

	public DebugSupport getDebugSupport() {
		//In the future we may need to choose between multiple strategies here.
		return getViewModel().getCfDebugSupport();
	}

	public BootDashViewModel getViewModel() {
		return getBootDashModel().getViewModel();
	}

	public void restartOnly(RunState runingOrDebugging, UserInteractions ui) throws Exception {

		CloudApplicationOperation op = cloudModel.getApplicationDeploymentOperations().restartOnly(getProject(),
				getName(), RunState.STARTING);

		cloudModel.getOperationsExecution(ui).runOpAsynch(new CompositeApplicationOperation(op));
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
			DebugSupport debugSupport = getDebugSupport();
			if (debugSupport.isDebuggerAttached(this)) {
//			if (DevtoolsUtil.isDevClientAttached(this, ILaunchManager.DEBUG_MODE)) {
				state = RunState.DEBUGGING;
			}
		}
		return state;
	}

	@Override
	public CloudFoundryRunTarget getTarget() {
		return cloudTarget;
	}

	@Override
	public int getLivePort() {
		return 80;
	}

	@Override
	public String getLiveHost() {
		CFApplication app = getCloudModel().getAppCache().getApp(getName());
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
	public ILaunchConfiguration getActiveConfig() {
		return null;
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

	public String getHealthCheck() {
		return getCloudModel().getAppCache().getHealthCheck(this);
	}

	/**
	 * Changes the cached health-check value for this model element. Note that this
	 * doesn *not* change the real value of the health-check.
	 */
	public void setHealthCheck(String hc) {
		String old = getHealthCheck();
		if (!Objects.equal(old, hc)) {
			CloudFoundryBootDashModel model = getCloudModel();
			model.getAppCache().setHealthCheck(this, hc);
			model.notifyElementChanged(this);
		}
	}

	public UUID getAppGuid() {
		CFApplication app = getCloudModel().getAppCache().getApp(getName());
		if (app!=null) {
			return app.getGuid();
		}
		return null;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		return persistentProperties;
	}

	static class CloudAppIdentity {

		private final String appName;
		private final RunTarget runTarget;

		public String toString() {
			return appName + "@" + runTarget;
		};

		CloudAppIdentity(String appName, RunTarget runTarget) {
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
			CloudAppIdentity other = (CloudAppIdentity) obj;
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

	public void log(String message) {
		log(message, LogType.LOCALSTDOUT);
	}

	public void log(String message, LogType logType) {
		try {
			getCloudModel().getElementConsoleManager().writeToConsole(this, message, logType);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	@Override
	public Object getParent() {
		return getBootDashModel();
	}

	@Override
	protected LiveExpression<URI> getActuatorUrl() {
		LiveExpression<URI> urlExp = getCloudModel().getActuatorUrlFactory().createOrGet(this);
		if (urlExp!=null) {
			return urlExp;
		}
		//only happens when this element is not valid anymore, but return something harmless / usable anyhow
		return LiveExpression.constant(null);
	}

	public IFile getDeploymentManifestFile() {
		String text = getPersistentProperties().get(DEPLOYMENT_MANIFEST_FILE_PATH);
		try {
			return text == null ? null : ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(text));
		} catch (IllegalArgumentException e) {
			BootDashActivator.log(e);
			return null;
		}
	}

	public void setDeploymentManifestFile(IFile file) {
		try {
			if (file == null) {
				getPersistentProperties().put(DEPLOYMENT_MANIFEST_FILE_PATH, (String) null);
			} else {
				getPersistentProperties().put(DEPLOYMENT_MANIFEST_FILE_PATH, file.getFullPath().toString());
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

}
