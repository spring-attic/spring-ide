/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
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

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	public synchronized RunState getState(final IProject project) {
		return getState(activeStates, project);
	}

	///////////////////////// stuff below is implementation cruft ////////////////////

	private Map<IProject, RunState> activeStates = null;
	private ProcessTracker processTracker = new ProcessTracker(this);
	private ProjectRunStateListener listener;

	public ProjectRunStateTracker() {
		activeStates = new HashMap<IProject, RunState>();
		processTracker = new ProcessTracker(this);
		updateProjectStatesAndFireEvents();
	}

	private static RunState getState(Map<IProject, RunState> states, IProject p) {
		if (states!=null) {
			RunState state = states.get(p);
			if (state!=null) {
				return state;
			}
		}
		return RunState.INACTIVE;
	}

	private Map<IProject, RunState> getCurrentActiveStates() {
		Map<IProject, RunState> states = new HashMap<IProject, RunState>();
		for (ILaunch l : launchManager().getLaunches()) {
			if (!l.isTerminated()) {
				IProject p = LaunchUtil.getProject(l);
				RunState s1 = getState(states, p);
				RunState s2 = LaunchUtil.isDebugging(l)
						? RunState.DEBUGGING
						: RunState.RUNNING;
				states.put(p, s1.merge(s2));
			}
		}
		return states;
	}

	protected ILaunchManager launchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public void dispose() {
		if (processTracker!=null) {
			processTracker.dispose();
			processTracker = null;
		}
	}

	private void updateProjectStatesAndFireEvents() {
		//Note that updateProjectStates is synchronized, but this method is not.
		// Important not to keep locks while firing events.
		Set<IProject> affected = updateProjectStates();
		ProjectRunStateListener listener = this.listener;
		if (listener!=null) {
			for (IProject p : affected) {
				listener.stateChanged(p);
			}
		}
	}

	private synchronized Set<IProject> updateProjectStates() {
		Map<IProject, RunState> oldStates = activeStates;
		activeStates = getCurrentActiveStates();

		// Compute set of projects who's state has changed
		Set<IProject> affectedProjects = new HashSet<IProject>(keySet(oldStates));
		affectedProjects.addAll(keySet(activeStates));
		Iterator<IProject> iter = affectedProjects.iterator();
		while (iter.hasNext()) {
			IProject p = iter.next();
			RunState oldState = getState(oldStates, p);
			RunState newState = getState(activeStates, p);
			if (oldState.equals(newState)) {
				iter.remove();
			} else {
				debug(p+": "+ oldState +" => " + newState);
			}
		}
		return affectedProjects;
	}

	/**
	 * Null-safe 'keySet' fetcher for map.
	 */
	private <K,V> Set<K> keySet(Map<K, V> map) {
		if (map==null) {
			return Collections.emptySet();
		}
		return map.keySet();
	}

	@Override
	public void processTerminated(IProcess process) {
		updateProjectStatesAndFireEvents();
	}

	@Override
	public void processCreated(IProcess process) {
		updateProjectStatesAndFireEvents();
	}

	public void setListener(ProjectRunStateListener listener) {
		if (this.listener!=null) {
			throw new IllegalStateException("Listener can only be set once");
		}
		this.listener = listener;
	}
}
