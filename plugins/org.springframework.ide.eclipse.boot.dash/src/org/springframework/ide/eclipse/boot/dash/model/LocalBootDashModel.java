/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.springframework.ide.eclipse.boot.core.cli.BootCliUtils;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager.BootInstallListener;
import org.springframework.ide.eclipse.boot.core.cli.CloudCliUtils;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsPortRefresher;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSets;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfigurationTracker;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springframework.ide.eclipse.boot.dash.views.BootDashTreeView;
import org.springframework.ide.eclipse.boot.dash.views.LocalElementConsoleManager;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager.ProjectChangeListener;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Model of the contents for {@link BootDashTreeView}, provides mechanism to attach listeners to model
 * and attaches itself as a workspace listener to keep the model in synch with workspace changes.
 *
 * @author Kris De Volder
 */
public class LocalBootDashModel extends AbstractBootDashModel implements DeletionCapabableModel {

	private IWorkspace workspace;
	private BootProjectDashElementFactory projectElementFactory;
	private LaunchConfDashElementFactory launchConfElementFactory;

	ProjectChangeListenerManager openCloseListenerManager;
	ClasspathListenerManager classpathListenerManager;

	private final LaunchConfRunStateTracker launchConfRunStateTracker = new LaunchConfRunStateTracker();
	final LaunchConfigurationTracker launchConfTracker = new LaunchConfigurationTracker(BootLaunchConfigurationDelegate.TYPE_ID);

	private LiveSetVariable<BootProjectDashElement> applications; //lazy created
	private LiveSetVariable<LocalCloudServiceDashElement> cloudCliservices;
	private ObservableSet<BootDashElement> allElements;

	private BootDashModelConsoleManager consoleManager;

	private DevtoolsPortRefresher devtoolsPortRefresher;
	private LiveExpression<Pattern> projectExclusion;
	private ValueListener<Pattern> projectExclusionListener;

	private BootInstallListener bootInstallListener;
	private IPropertyStore modelStore;

	private LiveVariable<RefreshState> bootAppsRefreshState = new LiveVariable<>(RefreshState.READY);
	private LiveVariable<RefreshState> cloudCliServicesRefreshState = new LiveVariable<>(RefreshState.READY);

	private LiveExpression<Boolean> hideCloudCliServices = new LiveExpression<Boolean>() {
		{
			dependsOn(getViewModel().getToggleFilters().getSelectedFilters());
		}

		@Override
		protected Boolean compute() {
			return getViewModel().getToggleFilters().getSelectedFilters()
					.contains(ToggleFiltersModel.FILTER_CHOICE_HIDE_LOCAL_SERVICES);
		}
	};

	private LiveExpression<RefreshState> refreshState = new LiveExpression<RefreshState>() {
		{
			dependsOn(bootAppsRefreshState);
			dependsOn(cloudCliServicesRefreshState);
			addListener((e,v) -> notifyModelStateChanged());
		}

		@Override
		protected RefreshState compute() {
			return RefreshState.merge(bootAppsRefreshState.getValue(), cloudCliServicesRefreshState.getValue());
		}
	};

	public class WorkspaceListener implements ProjectChangeListener, ClasspathListener {

		@Override
		public void projectChanged(IProject project) {
			updateElementsFromWorkspace();
		}

		@Override
		public void classpathChanged(IJavaProject jp) {
			updateElementsFromWorkspace();
		}
	}

	public LocalBootDashModel(BootDashModelContext context, BootDashViewModel parent) {
		super(RunTargets.LOCAL, parent);
		this.workspace = context.getWorkspace();
		this.launchConfElementFactory = new LaunchConfDashElementFactory(this, context.getLaunchManager());
		this.projectElementFactory = new BootProjectDashElementFactory(this, context.getProjectProperties(), launchConfElementFactory);
		this.consoleManager = new LocalElementConsoleManager();
		this.projectExclusion = context.getBootProjectExclusion();

		RunTargetType type = getRunTarget().getType();
		IPropertyStore typeStore = PropertyStoreFactory.createForScope(type, context.getRunTargetProperties());
		this.modelStore = PropertyStoreFactory.createSubStore(getRunTarget().getId(), typeStore);

		// Listen to M2E JDT plugin active event to refresh local boot project dash elements.
		addMavenInitializationIssueEventHandling();
	}

	/**
	 * Refresh boot project dash elements once m2e JDT plugin is fully
	 * initialized. Boot project checks may not succeed in some cases if m2e JDT
	 * hasn't completed it's start procedure
	 */
	private void addMavenInitializationIssueEventHandling() {
		Bundle bundle = Platform.getBundle("org.eclipse.m2e.jdt");
		if (bundle != null) {
			BundleListener listener = new BundleListener() {
				@Override
				public void bundleChanged(BundleEvent event) {
					if (event.getBundle() == bundle && event.getType() == BundleEvent.STARTED) {
						try {
							updateElementsFromWorkspace();
						} catch (Throwable t) {
							Log.log(t);
						} finally {
							bundle.getBundleContext().removeBundleListener(this);
						}
					}
				}
			};
			bundle.getBundleContext().addBundleListener(listener);
		}
	}

	void init() {
		if (allElements==null) {
			this.applications = new LiveSetVariable<>(AsyncMode.SYNC);
			this.cloudCliservices = new LiveSetVariable<>(AsyncMode.SYNC);
			this.allElements = LiveSets.union(this.applications, this.cloudCliservices);
			WorkspaceListener workspaceListener = new WorkspaceListener();
			this.openCloseListenerManager = new ProjectChangeListenerManager(workspace, workspaceListener);
			this.classpathListenerManager = new ClasspathListenerManager(workspaceListener);
			projectExclusion.addListener(projectExclusionListener = new ValueListener<Pattern>() {
				public void gotValue(LiveExpression<Pattern> exp, Pattern value) {
					updateElementsFromWorkspace();
				}
			});

			refresh(null);

			bootInstallListener = new BootInstallListener() {
				@Override
				public void defaultInstallChanged() {
					refreshLocalCloudServices();
				}
			};
			try {
				BootInstallManager.getInstance().addBootInstallListener(bootInstallListener);
			} catch (Exception e) {
				Log.log(e);
			}

			// Listen to changes in "Hide Local Cloud Services" filter toggle
			hideCloudCliServices.addListener((e, v) -> refreshLocalCloudServices());

			this.devtoolsPortRefresher = new DevtoolsPortRefresher(this, projectElementFactory);
		}
	}

	/**
	 * When no longer needed the model should be disposed, otherwise it will continue
	 * listening for changes to the workspace in order to keep itself in synch.
	 */
	public void dispose() {
		if (applications!=null) {
			applications.getValue().forEach(bde -> bde.dispose());
			applications.dispose();
			applications = null;
			openCloseListenerManager.dispose();
			openCloseListenerManager = null;
			classpathListenerManager.dispose();
			classpathListenerManager = null;
			devtoolsPortRefresher.dispose();
			devtoolsPortRefresher = null;
		}
		if (launchConfElementFactory!=null) {
			launchConfElementFactory.dispose();
			launchConfElementFactory = null;
		}
		if (projectElementFactory!=null) {
			projectElementFactory.dispose();
			projectElementFactory = null;
		}
		if (projectExclusionListener!=null) {
			projectExclusion.removeListener(projectExclusionListener);
			projectExclusionListener=null;
		}
		if (bootInstallListener != null) {
			try {
				BootInstallManager.getInstance().removeBootInstallListener(bootInstallListener);
			} catch (Exception e) {
				Log.log(e);
			}
		}
		if (cloudCliservices != null) {
			cloudCliservices.getValue().forEach(bde -> bde.dispose());
			cloudCliservices.dispose();
			cloudCliservices = null;
		}
		if (allElements != null) {
			allElements.dispose();
			allElements = null;
		}
		hideCloudCliServices.dispose();
		launchConfTracker.dispose();
		launchConfRunStateTracker.dispose();
	}

	void updateElementsFromWorkspace() {
		LiveSetVariable<BootProjectDashElement> apps = this.applications;
		if (apps!=null) {
			Set<BootProjectDashElement> newElements = Arrays.stream(this.workspace.getRoot().getProjects())
					.map(projectElementFactory::createOrGet)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			apps.replaceAll(newElements);
			projectElementFactory.disposeAllExcept(newElements);
		}
	}

	public synchronized ObservableSet<BootDashElement> getElements() {
		init();
		return allElements;
	}

	/**
	 * Trigger manual model refresh.
	 */
	public void refresh(UserInteractions ui) {
		updateElementsFromWorkspace();
		refreshLocalCloudServices();
	}

	private List<LocalCloudServiceDashElement> fetchLocalServices() {
		try {
			IBootInstall bootInstall = BootCliUtils.getSpringBootInstall();
			try {
				return Arrays.stream(CloudCliUtils.getCloudServices(bootInstall)).map(serviceId -> new LocalCloudServiceDashElement(this, serviceId)).collect(Collectors.toList());
			} catch (CoreException e) {
				// Core Exception would be thrown if Spring Cloud CLI command fails to execute
				Log.log(e);
			}
		} catch (Exception e) {
			// ignore
		}
		return Collections.emptyList();
	}

	private void refreshLocalCloudServices() {
		if (hideCloudCliServices.getValue()) {
			cloudCliservices.getValue().forEach(bde -> bde.dispose());
			cloudCliservices.replaceAll(Collections.emptySet());
		} else {
			new Job("Loading local cloud services") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						cloudCliServicesRefreshState.setValue(RefreshState.loading("Fetching Local Cloud Sevices..."));
						List<LocalCloudServiceDashElement> newCloudCliservices = fetchLocalServices();
						cloudCliservices.getValue().forEach(bde -> bde.dispose());
						cloudCliservices.replaceAll(newCloudCliservices);
						return Status.OK_STATUS;
					} finally {
						cloudCliServicesRefreshState.setValue(RefreshState.READY);
					}
				}
			}.schedule();
		}
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		return consoleManager;
	}

	public LaunchConfRunStateTracker getLaunchConfRunStateTracker() {
		return launchConfRunStateTracker;
	}

	public BootProjectDashElementFactory getProjectElementFactory() {
		return projectElementFactory;
	}

	public LaunchConfDashElementFactory getLaunchConfElementFactory() {
		return launchConfElementFactory;
	}

	@Override
	public void delete(Collection<BootDashElement> elements, UserInteractions ui) {
		for (BootDashElement e : elements) {
			if (e instanceof Deletable) {
				((Deletable)e).delete(ui);
			}
		}
	}

	@Override
	public boolean canDelete(BootDashElement element) {
		return element instanceof Deletable;
	}

	@Override
	public String getDeletionConfirmationMessage(Collection<BootDashElement> value) {
		return "Are you sure you want to delete the selected local launch configuration(s)? The configuration(s) will be permanently removed from the workspace.";
	}

	public IPropertyStore getModelStore() {
		return modelStore;
	}

	@Override
	public RefreshState getRefreshState() {
		return refreshState.getValue();
	}

}
