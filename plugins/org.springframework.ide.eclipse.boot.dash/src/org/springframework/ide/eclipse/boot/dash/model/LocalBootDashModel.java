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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsPortRefresher;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSetVariable;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfigurationTracker;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker.ProjectRunStateListener;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springframework.ide.eclipse.boot.dash.views.BootDashTreeView;
import org.springframework.ide.eclipse.boot.dash.views.LocalElementConsoleManager;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager.ProjectChangeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Model of the contents for {@link BootDashTreeView}, provides mechanism to attach listeners to model
 * and attaches itself as a workspace listener to keep the model in synch with workspace changes.
 *
 * @author Kris De Volder
 */
public class LocalBootDashModel extends BootDashModel {

	private IWorkspace workspace;
	ProjectChangeListenerManager openCloseListenerManager;
	ClasspathListenerManager classpathListenerManager;
	BootDashElementFactory elementFactory;
	ProjectRunStateTracker runStateTracker;
	LiveSetVariable<BootDashElement> elements; //lazy created
	private BootDashModelConsoleManager consoleManager;

	LaunchConfigurationTracker launchConfTracker = new LaunchConfigurationTracker(BootLaunchConfigurationDelegate.TYPE_ID);

	private BootDashModelStateSaver modelState;
	private DevtoolsPortRefresher devtoolsPortRefresher;
	private LiveExpression<Pattern> projectExclusion;
	private ValueListener<Pattern> projectExclusionListener;

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
		this.elementFactory = new BootDashElementFactory(this, context.getProjectProperties());
		this.consoleManager = new LocalElementConsoleManager();
		try {
			ISavedState lastState = workspace.addSaveParticipant(BootDashActivator.PLUGIN_ID, modelState = new BootDashModelStateSaver(context, elementFactory));
			modelState.restore(lastState);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		this.devtoolsPortRefresher = new DevtoolsPortRefresher(this, elementFactory);
		this.projectExclusion = context.getBootProjectExclusion();
	}

	void init() {
		if (elements==null) {
			this.elements = new LiveSetVariable<BootDashElement>();
			WorkspaceListener workspaceListener = new WorkspaceListener();
			this.openCloseListenerManager = new ProjectChangeListenerManager(workspace, workspaceListener);
			this.classpathListenerManager = new ClasspathListenerManager(workspaceListener);
			this.runStateTracker = new ProjectRunStateTracker();
			runStateTracker.setListener(new ProjectRunStateListener() {
				public void stateChanged(IProject p) {
					BootDashElement e = elementFactory.createOrGet(p);
					if (e!=null) {
						notifyElementChanged(e);
					}
				}
			});
			projectExclusion.addListener(projectExclusionListener = new ValueListener<Pattern>() {
				public void gotValue(LiveExpression<Pattern> exp, Pattern value) {
					updateElementsFromWorkspace();
				}
			});
			updateElementsFromWorkspace();
		}
	}

	void updateElementsFromWorkspace() {
		Set<BootDashElement> newElements = new HashSet<BootDashElement>();
		for (IProject p : this.workspace.getRoot().getProjects()) {
			BootDashElement element = elementFactory.createOrGet(p);
			if (element!=null) {
				newElements.add(element);
			}
		}
		for (BootDashElement oldElement : elements.getValues()) {
			if (!newElements.contains(oldElement)) {
				if (oldElement instanceof Disposable) {
					((Disposable) oldElement).dispose();
				}
			}
		}
		elements.replaceAll(newElements);
	}

	public synchronized ObservableSet<BootDashElement> getElements() {
		init();
		return elements;
	}

	/**
	 * When no longer needed the model should be disposed, otherwise it will continue
	 * listening for changes to the workspace in order to keep itself in synch.
	 */
	public void dispose() {
		if (elements!=null) {
			elements = null;
			openCloseListenerManager.dispose();
			elementFactory.dispose();
			classpathListenerManager.dispose();
			runStateTracker.dispose();
			devtoolsPortRefresher.dispose();
			if (projectExclusionListener!=null) {
				projectExclusion.removeListener(projectExclusionListener);
			}
			launchConfTracker.dispose();
		}
	}

	/**
	 * Trigger manual model refresh.
	 */
	public void refresh() {
		updateElementsFromWorkspace();
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		return consoleManager;
	}


	////////////// listener cruft ///////////////////////////



	public ProjectRunStateTracker getRunStateTracker() {
		return runStateTracker;
	}

	public ILaunchConfiguration getPreferredConfigs(WrappingBootDashElement<IProject> e) {
		return modelState.getPreferredConfig(e);
	}

	public void setPreferredConfig(
			WrappingBootDashElement<IProject> e,
			ILaunchConfiguration c) {
		modelState.setPreferredConfig(e, c);
	}
}
