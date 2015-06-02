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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker.ProjectRunStateListener;
import org.springframework.ide.eclipse.boot.dash.views.BootDashView;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager.ProjectChangeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

/**
 * Model of the contents for {@link BootDashView}, provides mechanism to attach listeners to model
 * and attaches itself as a workspace listener to keep the model in synch with workspace changes.
 *
 * @author Kris De Volder
 */
public class BootDashModel {

	private IWorkspace workspace;
	private ProjectChangeListenerManager openCloseListenerManager;
	private ClasspathListenerManager classpathListenerManager;
	private BootDashElementFactory elementFactory;
	private ProjectRunStateTracker runStateTracker;
	private LiveSet<BootDashElement> elements; //lazy created

	private BootDashModelStateSaver modelState;

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

	public BootDashModel(BootDashModelContext context) {
		this.workspace = context.getWorkspace();
		this.elementFactory = new BootDashElementFactory(this);
		try {
			ISavedState lastState = workspace.addSaveParticipant(BootDashActivator.PLUGIN_ID, modelState = new BootDashModelStateSaver(context, elementFactory));
			modelState.restore(lastState);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	public synchronized LiveSet<BootDashElement> getElements() {
		init();
		return elements;
	}

	private void init() {
		if (elements==null) {
			this.elements = new LiveSet<BootDashElement>();
			WorkspaceListener workspaceListener = new WorkspaceListener();
			this.openCloseListenerManager = new ProjectChangeListenerManager(workspace, workspaceListener);
			this.classpathListenerManager = new ClasspathListenerManager(workspaceListener);
			this.runStateTracker = new ProjectRunStateTracker();
			runStateTracker.setListener(new ProjectRunStateListener() {
				public void stateChanged(IProject p) {
					BootDashElement e = elementFactory.create(p);
					if (e!=null) {
						notifyElementChanged(e);
					}
				}
			});
			updateElementsFromWorkspace();
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
			elementFactory.dispose();
			classpathListenerManager.dispose();
			runStateTracker.dispose();
		}
	}

	private void updateElementsFromWorkspace() {
		Set<BootDashElement> newElements = new HashSet<BootDashElement>();
		for (IProject p : this.workspace.getRoot().getProjects()) {
			BootDashElement element = elementFactory.create(p);
			if (element!=null) {
				newElements.add(element);
			}
		}
		elements.replaceAll(newElements);
	}

	/**
	 * Trigger manual model refresh.
	 */
	public void refresh() {
		updateElementsFromWorkspace();
	}

	////////////// listener cruft ///////////////////////////

	public interface ElementStateListener {
		/**
		 * Called when something about the element has changed.
		 * <p>
		 * Note this doesn't get called when elements are added / removed etc. Only when
		 * some property of the element itself has changed.
		 */
		void stateChanged(BootDashElement e);
	}

	private ListenerList elementStateListeners = new ListenerList();

	public void addElementStateListener(ElementStateListener l) {
		elementStateListeners.add(l);
	}

	public void removeElementStateListener(ElementStateListener l) {
		elementStateListeners.remove(l);
	}

	private void notifyElementChanged(BootDashElement element) {
		for (Object l : elementStateListeners.getListeners()) {
			((ElementStateListener)l).stateChanged(element);
		}
	}

	public ProjectRunStateTracker getRunStateTracker() {
		return runStateTracker;
	}

	public ILaunchConfiguration getPreferredConfigs(BootProjectDashElement e) {
		return modelState.getPreferredConfig(e);
	}

	public void setPreferredConfig(
			BootProjectDashElement e,
			ILaunchConfiguration c) {
		modelState.setPreferredConfig(e, c);
	}

}
