/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.util.ProcessTracker.ProcessListener;

public class ProjectRunStateTracker implements ProcessListener {

	//// public API ///////////////////////////////////////////////////////////////////

	public interface ProjectRunStateListener {
		void stateChanged(IProject project);
	}

	public synchronized RunState getState(final IProject project) {
		init();
		return getState(activeStates, project);
	}

	public void addListener(ProjectRunStateListener l) {
		init();
		this.listeners.add(l);
	}

	public void removeListener(ProjectRunStateListener l) {
		this.listeners.remove(l);
	}

	///////////////////////// stuff below is implementation cruft ////////////////////

	private ListenerList listeners;
	private Map<IProject, RunState> activeStates = null;
	private ProcessTracker processTracker = new ProcessTracker();

	private void init() {
		if (activeStates==null) {
			listeners = new ListenerList();
			activeStates = new HashMap<IProject, RunState>();
			processTracker.addListener(this);
			updateProjectStates();
		}
	}

	private RunState getState(Map<IProject, RunState> activeStates2, IProject p) {
		RunState state = activeStates.get(p);
		if (state!=null) {
			return state;
		}
		return RunState.INACTIVE;
	}

	private Map<IProject, RunState> getCurrentActiveStates() {
		Map<IProject, RunState> states = new HashMap<IProject, RunState>();
		for (ILaunch l : launchManager().getLaunches()) {
			if (!l.isTerminated()) {
				IProject project = LaunchUtil.getProject(l);
				states.put(project, LaunchUtil.isDebugging(l)
						? RunState.DEBUGGING
						: RunState.RUNNING
				);
			}
		}
		return states;
	}

	protected ILaunchManager launchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public void dispose() {
		processTracker.removeListener(this);
	}

	private void  updateProjectStatesAndFireEvents() {
		Set<IProject> affected = updateProjectStates();
		for (IProject p : affected) {
			notifyListeners(p);
		}
	}
	private void notifyListeners(IProject p) {
		for (Object l : listeners.getListeners()) {
			((ProjectRunStateListener)l).stateChanged(p);
		}
	}

	private synchronized Set<IProject> updateProjectStates() {
		Map<IProject, RunState> oldStates = activeStates;
		activeStates = getCurrentActiveStates();

		// Compute set of projects who's state has changed
		Set<IProject> affectedProjects = new HashSet<IProject>(oldStates.keySet());
		affectedProjects.addAll(activeStates.keySet());
		Iterator<IProject> iter = affectedProjects.iterator();
		while (iter.hasNext()) {
			IProject p = iter.next();
			if (!getState(oldStates, p).equals(getState(activeStates, p))) {
				iter.remove();
			}
		}

		return affectedProjects;
	}

	@Override
	public void processTerminated(IProcess process) {
		updateProjectStatesAndFireEvents();
	}

	@Override
	public void processCreated(IProcess process) {
		updateProjectStatesAndFireEvents();
	}

}
