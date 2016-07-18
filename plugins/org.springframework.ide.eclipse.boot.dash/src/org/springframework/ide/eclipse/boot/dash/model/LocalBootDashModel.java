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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsPortRefresher;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.LaunchConfigurationTracker;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springframework.ide.eclipse.boot.dash.views.BootDashTreeView;
import org.springframework.ide.eclipse.boot.dash.views.LocalElementConsoleManager;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager.ProjectChangeListener;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
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

	LiveSetVariable<BootDashElement> elements; //lazy created
	private BootDashModelConsoleManager consoleManager;

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
		this.launchConfElementFactory = new LaunchConfDashElementFactory(this, context.getLaunchManager());
		this.projectElementFactory = new BootProjectDashElementFactory(this, context.getProjectProperties(), launchConfElementFactory);
		this.consoleManager = new LocalElementConsoleManager();
		this.projectExclusion = context.getBootProjectExclusion();
	}

	void init() {
		if (elements==null) {
			this.elements = new LiveSetVariable<>(AsyncMode.SYNC);
			WorkspaceListener workspaceListener = new WorkspaceListener();
			this.openCloseListenerManager = new ProjectChangeListenerManager(workspace, workspaceListener);
			this.classpathListenerManager = new ClasspathListenerManager(workspaceListener);
			projectExclusion.addListener(projectExclusionListener = new ValueListener<Pattern>() {
				public void gotValue(LiveExpression<Pattern> exp, Pattern value) {
					updateElementsFromWorkspace();
				}
			});
			updateElementsFromWorkspace();
			this.devtoolsPortRefresher = new DevtoolsPortRefresher(this, projectElementFactory);
		}
	}

	/**
	 * When no longer needed the model should be disposed, otherwise it will continue
	 * listening for changes to the workspace in order to keep itself in synch.
	 */
	public void dispose() {
		if (elements!=null) {
			elements = null;
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
		launchConfTracker.dispose();
		launchConfRunStateTracker.dispose();
	}

	void updateElementsFromWorkspace() {
		Set<BootDashElement> newElements = new HashSet<>();
		for (IProject p : this.workspace.getRoot().getProjects()) {
			BootDashElement element = projectElementFactory.createOrGet(p);
			if (element!=null) {
				newElements.add(element);
			}
		}
		elements.replaceAll(newElements);
		projectElementFactory.disposeAllExcept(newElements);
	}

	public synchronized ObservableSet<BootDashElement> getElements() {
		init();
		return elements;
	}

	/**
	 * Trigger manual model refresh.
	 */
	public void refresh(UserInteractions ui) {
		updateElementsFromWorkspace();
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
}
