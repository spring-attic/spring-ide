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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.CloudAppLogManager;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.OperationsExecution;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ProjectsDeployer;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.TargetApplicationsRefreshOperation;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springframework.ide.eclipse.boot.launch.devtools.BootDevtoolsClientLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.ProcessListenerAdapter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils;

public class CloudFoundryBootDashModel extends BootDashModel implements ModifiableModel {

	private IPropertyStore modelStore;

	private ProjectAppStore projectAppStore;

	private CloudAppCache cloudAppCache;

	public static final String PROJECT_TO_APP_MAPPING = "projectToAppMapping";

	private CloudDashElementFactory elementFactory;

	private LiveSet<BootDashElement> elements;

	private BootDashModelConsoleManager consoleManager;

	private DevtoolsDebugTargetDisconnector debugTargetDisconnector;

	public CloudFoundryBootDashModel(CloudFoundryRunTarget target, BootDashModelContext context) {
		super(target);
		RunTargetType type = target.getType();
		IPropertyStore typeStore = PropertyStoreFactory.createForScope(type, context.getRunTargetProperties());
		this.modelStore = PropertyStoreFactory.createSubStore(target.getId(), typeStore);
		this.projectAppStore = new ProjectAppStore(this.modelStore);
		this.cloudAppCache = new CloudAppCache();
		this.elementFactory = new CloudDashElementFactory(context, modelStore, this);
		this.consoleManager = new CloudAppLogManager(target);
		this.debugTargetDisconnector = DevtoolsUtil.createDebugTargetDisconnector(this);
	}

	public CloudAppCache getAppCache() {
		return cloudAppCache;
	}

	@Override
	public LiveSet<BootDashElement> getElements() {

		if (elements == null) {
			elements = new LiveSet<BootDashElement>();

			asyncRefreshElements();
		}
		return elements;
	}

	protected void asyncRefreshElements() {
		if (elements == null) {
			return;
		}

		try {
			setState(State.LOADING);
			Operation<Void> op = new TargetApplicationsRefreshOperation(this);
			getOperationsExecution().runOpAsynch(op);
		} catch (Exception e) {
			setState(State.READY);
			BootDashActivator.log(e);
		}
	}

	public void internalSetState(State newState) {
		setState(newState);
	}

	@Override
	public void dispose() {
		elements = null;
		if (debugTargetDisconnector!=null) {
			debugTargetDisconnector.dispose();
			debugTargetDisconnector = null;
		}
	}

	@Override
	public void refresh() {
		asyncRefreshElements();
	}

	public CloudFoundryRunTarget getCloudTarget() {
		return (CloudFoundryRunTarget) getRunTarget();
	}

	@Override
	public boolean canBeAdded(List<Object> sources) {
		if (sources != null && !sources.isEmpty()) {
			for (Object obj : sources) {
				// IMPORTANT: to avoid drag/drop into the SAME target, be
				// sure
				// all sources are from a different target
				if (getProject(obj) == null || !isFromDifferentTarget(obj)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public void add(List<Object> sources, UserInteractions ui) throws Exception {

		Map<IProject, BootDashElement> projects = new LinkedHashMap<IProject, BootDashElement>();
		if (sources != null) {
			for (Object obj : sources) {
				IProject project = getProject(obj);

				if (project != null) {
					projects.put(project, null);
				}
			}

			performDeployment(projects, ui);
		}
	}

	protected IProject getProject(Object obj) {
		IProject project = null;
		if (obj instanceof IProject) {
			project = (IProject) obj;
		} else if (obj instanceof IJavaProject) {
			project = ((IJavaProject) obj).getProject();
		} else if (obj instanceof IAdaptable) {
			project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
		} else if (obj instanceof BootDashElement) {
			project = ((BootDashElement) obj).getProject();
		}
		return project;
	}

	protected boolean isFromDifferentTarget(Object dropSource) {
		if (dropSource instanceof BootDashElement) {
			return ((BootDashElement) dropSource).getParent() != this;
		}

		// If not a boot element that is being dropped, it is an element
		// external to the boot dash view (e.g. project from project explorer)
		return true;
	}

	public void performDeployment(final Map<IProject, BootDashElement> projectsToDeploy, final UserInteractions ui)
			throws Exception {

		// When deploying or mapping app to project, always prompt user if they
		// want to replace the existing app
		boolean shouldAutoReplaceApp = false;

		getOperationsExecution(ui).runOpAsynch(
				new ProjectsDeployer(CloudFoundryBootDashModel.this, ui, projectsToDeploy, shouldAutoReplaceApp));

	}

	public BootDashElement addElement(CloudAppInstances appInstances, IProject project) throws Exception {
		BootDashElement addedElement = null;
		Set<BootDashElement> updated = new HashSet<BootDashElement>();
		boolean changed = false;
		synchronized (this) {

			// Safe iterate via getValues(); a copy, instead of getValue()
			List<BootDashElement> existing = elements.getValues();

			addedElement = elementFactory.create(appInstances.getApplication().getName());

			updated.add(addedElement);

			// Add any existing ones that weren't replaced by the new ones
			// Replace the existing one with a new one for the given Cloud
			// Application
			for (BootDashElement element : existing) {
				if (!addedElement.getName().equals(element.getName())) {
					updated.add(element);
				}
			}

			// Update the cache BEFORE updating the model, since the model
			// elements are handles to the cache
			changed = getAppCache().replace(appInstances, project,
					ApplicationRunningStateTracker.getRunState(appInstances));

			projectAppStore.storeProjectToAppMapping(updated);
		}

		// These trigger events, therefore be sure to call them outside of the
		// synch block to avoid deadlock
		elements.replaceAll(updated);

		if (changed) {
			notifyElementChanged(addedElement);
		}

		return addedElement;
	}

	public CloudDashElement getElement(String appName) {

		synchronized (this) {
			Set<BootDashElement> existing = elements.getValue();

			// Add any existing ones that weren't replaced by the new ones
			// Replace the existing one with a new one for the given Cloud
			// Application
			for (BootDashElement element : existing) {
				if (appName.equals(element.getName()) && element instanceof CloudDashElement) {
					return (CloudDashElement) element;
				}
			}
			return null;
		}

	}

	public void updateElements(Map<CloudAppInstances, IProject> apps) throws Exception {

		Map<String, BootDashElement> updated = new HashMap<String, BootDashElement>();
		List<String> toNotify = null;
		synchronized (this) {

			// Update external cache that keeps track of additional element
			// state (e.g the running state,
			// app instances, and project mapping)
			toNotify = getAppCache().updateAll(apps);

			// Create new handles to the applications. Note that the cache
			// should be updated first before creating elements
			// as elements are handles to state in the cache
			for (Entry<CloudAppInstances, IProject> entry : apps.entrySet()) {
				BootDashElement addedElement = elementFactory.create(entry.getKey().getApplication().getName());
				updated.put(addedElement.getName(), addedElement);
			}

			projectAppStore.storeProjectToAppMapping(updated.values());
		}

		// Fire events outside of synch block to avoid deadlock

		// This only fires a model CHANGE event (adding/removing elements). It
		// does not fire an event for app state changes that are tracked
		// externally
		// (runstate, instances, project) in the cache. The latter is handled
		// separately
		// below.
		elements.replaceAll(updated.values());

		// Fire app state change based on changes to the app cache
		if (toNotify != null) {
			for (String appName : toNotify) {
				BootDashElement updatedEl = updated.get(appName);
				if (updatedEl != null) {
					notifyElementChanged(updatedEl);
				}
			}
		}
	}

	public ProjectAppStore getProjectToAppMappingStore() {
		return projectAppStore;
	}

	public OperationsExecution getOperationsExecution(UserInteractions ui) {
		return new OperationsExecution(ui);
	}

	public OperationsExecution getOperationsExecution() {
		return new OperationsExecution(null);
	}

	public void updateApplication(String appName, RunState runState) {
		CloudDashElement element = getElement(appName);
		if (element != null && element.getRunState() != runState) {
			getAppCache().updateCache(appName, runState);

			notifyElementChanged(element);
		}
	}

	public void updateApplication(CloudAppInstances appInstance, RunState runState) {
		RunState updatedRunState = runState != null ? runState
				: ApplicationRunningStateTracker.getRunState(appInstance);
		CloudDashElement element = getElement(appInstance.getApplication().getName());

		boolean notifyChanged = getAppCache().updateCache(appInstance, updatedRunState);
		if (notifyChanged && element != null) {
			notifyElementChanged(element);
		}
	}

	public void updateApplication(CloudAppInstances app) {
		updateApplication(app, null);
	}

	@Override
	public void delete(Collection<BootDashElement> toRemove, UserInteractions ui) {

		if (toRemove == null || toRemove.isEmpty()) {
			return;
		}

		for (BootDashElement element : toRemove) {
			if (element instanceof CloudDashElement) {
				try {
					delete((CloudDashElement) element, ui);
				} catch (Exception e) {
					BootDashActivator.log(e);
				}
			}
		}
	}

	/**
	 * Remove one element at a time, which updates the model
	 *
	 * @param element
	 * @return
	 * @throws Exception
	 */
	protected void delete(final CloudDashElement cloudElement, final UserInteractions ui) throws Exception {

		CloudApplicationOperation operation = new CloudApplicationOperation("Deleting: " + cloudElement.getName(), this,
				cloudElement.getName()) {

			@Override
			protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				// Delete from CF first. Do it outside of synch block to avoid
				// deadlock
				requests.deleteApplication(appName);
				Set<BootDashElement> updatedElements = new HashSet<BootDashElement>();

				synchronized (CloudFoundryBootDashModel.this) {
					// Safe iterate via getValues(); a copy, instead of
					// getValue()
					List<BootDashElement> existing = elements.getValues();

					// Be sure it is removed from the cache as well as
					// elements
					// are handles to the cache
					getAppCache().remove(cloudElement.getName());

					getElementConsoleManager().terminateConsole(cloudElement.getName());

					// Add any existing ones that weren't replaced by the new
					// ones
					// Replace the existing one with a new one for the given
					// Cloud
					// Application
					for (BootDashElement element : existing) {
						if (!cloudElement.getName().equals(element.getName())) {
							updatedElements.add(element);
						}
					}

					try {
						projectAppStore.storeProjectToAppMapping(updatedElements);
					} catch (Exception e) {
						ui.errorPopup("Error saving project to application mappings", e.getMessage());
					}
				}

				// do this outside the synch block

				elements.replaceAll(updatedElements);
			}
		};
		getOperationsExecution(ui).runOpAsynch(operation);

	}

	@Override
	public String toString() {
		return this.getClass().getName() + "(" + getRunTarget().getName() + ")";
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		return this.consoleManager;
	}
}
