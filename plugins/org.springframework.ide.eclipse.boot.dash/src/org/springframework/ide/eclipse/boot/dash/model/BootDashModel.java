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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker.ProjectRunStateListener;
import org.springframework.ide.eclipse.boot.dash.views.BootDashView;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectOpenCloseListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectOpenCloseListenerManager.ProjectOpenCloseListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

/**
 * Model of the contents for {@link BootDashView}, provides mechanism to attach listeners to model
 * and attaches itself as a workspace listener to keep the model in synch with workspace changes.
 *
 * @author Kris De Volder
 */
public class BootDashModel {

	private IWorkspace workspace;
	private ProjectOpenCloseListenerManager openCloseListenerManager;
	private ClasspathListenerManager classpathListenerManager;
	private BootDashElementFactory elementFactory;
	private LiveSet<BootDashElement> elements; //lazy created
	private WorkspaceListener listener; //lazy created
	private ProjectRunStateTracker runStateTracker;
	private ProjectRunStateListener runStateListener;

	public class WorkspaceListener implements ProjectOpenCloseListener, ClasspathListener {

		@Override
		public void projectOpened(IProject project) {
			udpateElementsFromWorkspace();
		}
		@Override
		public void projectClosed(IProject project) {
			udpateElementsFromWorkspace();
		}
		@Override
		public void classpathChanged(IJavaProject jp) {
			udpateElementsFromWorkspace();
		}

	}

	public BootDashModel(IWorkspace workspace) {
		this.workspace = workspace;
		this.openCloseListenerManager = new ProjectOpenCloseListenerManager(workspace);
		this.classpathListenerManager = new ClasspathListenerManager();
		this.runStateTracker = new ProjectRunStateTracker();
		this.elementFactory = new DefaultBootDashElementFactory(runStateTracker);
		runStateTracker.addListener(this.runStateListener = new ProjectRunStateListener() {
			public void stateChanged(IProject project) {
				BootDashElement element = elementFactory.create(project);
				if (element!=null) {
					notifyElementChanged(element);
				}
			}
		});
	}

	public synchronized LiveSet<BootDashElement> getElements() {
		if (elements==null) {
			this.elements = new LiveSet<BootDashElement>();
			createWorkspaceListener();
			udpateElementsFromWorkspace();
		}
		return elements;
	}

	private void createWorkspaceListener() {
		listener = new WorkspaceListener();
		openCloseListenerManager.add(listener);
		classpathListenerManager.add(listener);
	}

	private void udpateElementsFromWorkspace() {
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
	 * When no longer needed the model should be disposed, otherwise it will continue
	 * listening for changes to the workspace in order to keep itself in synch.
	 */
	public void dispose() {
		if (listener!=null) {
			openCloseListenerManager.remove(listener);
			classpathListenerManager.remove(listener);
			runStateTracker.removeListener(runStateListener);
			listener = null;
		}
	}

	/**
	 * Trigger manual model refresh.
	 */
	public void refresh() {
		udpateElementsFromWorkspace();
	}

	////////////// listener cruft ///////////////////////////

	public interface ElementStateListener {
		/**
		 * Called when something about the element has changed.
		 * <p>
		 * Note this doesn't get called when elements are added / removed etc. Onlu when
		 * some property of the element itself has changed.
		 */
		void stateChanged(BootDashElement e);
	}

	private ListenerList elementStateListeners = new ListenerList();

	public void addElementStateListener(ElementStateListener l) {
		elementStateListeners.add(l);
	}

	private void notifyElementChanged(BootDashElement element) {
		for (Object l : elementStateListeners.getListeners()) {
			((ElementStateListener)l).stateChanged(element);
		}
	}



}
