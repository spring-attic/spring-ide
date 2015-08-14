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
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.ProcessListenerAdapter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * @author Kris De Volder
 */
public class ProjectRunStateTracker extends ProcessListenerAdapter {

	//// public API ///////////////////////////////////////////////////////////////////

	public interface ProjectRunStateListener {
		void stateChanged(IProject project);
	}

	public ProjectRunStateTracker() {
		activeStates = new HashMap<IProject, RunState>();
		processTracker = new ProcessTracker(this);
		updateProjectStatesAndFireEvents();
	}

	public synchronized RunState getState(final IProject project) {
		return getState(activeStates, project);
	}

	public void setListener(ProjectRunStateListener listener) {
		if (this.listener!=null) {
			throw new IllegalStateException("Listener can only be set once");
		}
		this.listener = listener;
	}

	///////////////////////// stuff below is implementation cruft ////////////////////

	private static final boolean DEBUG = false; //(""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private Map<IProject, RunState> activeStates = null;
	private Map<ILaunch, ReadyStateMonitor> readyStateTrackers = null;
	private ProcessTracker processTracker = null;
	private ProjectRunStateListener listener; // listeners that are interested in us (i.e. clients)

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
			if (!l.isTerminated() && isInteresting(l)) {
				IProject p = LaunchUtil.getProject(l);
				RunState s1 = getState(states, p);
				RunState s2 = getActiveState(l);
				states.put(p, s1.merge(s2));
			}
		}
		return states;
	}

	private boolean isInteresting(ILaunch l) {
		try {
			ILaunchConfiguration conf = l.getLaunchConfiguration();
			if (conf!=null) {
					String type = conf.getType().getIdentifier();
				return BootLaunchConfigurationDelegate.LAUNCH_CONFIG_TYPE_ID.equals(type);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return false;
	}

	/**
	 * Assuming that l is an active launch, determine its RunState.
	 */
	private RunState getActiveState(ILaunch l) {
		boolean isReady = getReadyState(l).getValue();
		if (isReady) {
			return LaunchUtil.isDebugging(l)
					? RunState.DEBUGGING
					: RunState.RUNNING;
		}
		return RunState.STARTING;
	}

	private synchronized LiveExpression<Boolean> getReadyState(ILaunch l) {
		if (readyStateTrackers==null) {
			readyStateTrackers = new HashMap<ILaunch, ReadyStateMonitor>();
		}
		ReadyStateMonitor tracker = readyStateTrackers.get(l);
		if (tracker==null) {
			readyStateTrackers.put(l, tracker = createReadyStateTracker(l));
			tracker.getReady().addListener(readyStateListener);
		}
		return tracker.getReady();
	}

	private ValueListener<Boolean> readyStateListener = new ValueListener<Boolean>() {
		public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
			if (value) {
				//ready state tracker detected a launch just entered the 'ready' state
				updateProjectStatesAndFireEvents();
			}
		}
	};
	private boolean updateInProgress;

	protected ReadyStateMonitor createReadyStateTracker(ILaunch l) {
		ILaunchConfiguration conf = l.getLaunchConfiguration();
		if (conf!=null) {
			int jmxPort = getJMXPort(conf);
			if (jmxPort>0) {
				return new SpringApplicationReadyStateMonitor(jmxPort);
			}
		}
		return DummyReadyStateMonitor.create();
	}

	private synchronized void cleanupReadyStateTrackers() {
		if (readyStateTrackers!=null) {
			Iterator<Entry<ILaunch, ReadyStateMonitor>> iter = readyStateTrackers.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<ILaunch, ReadyStateMonitor> entry = iter.next();
				ILaunch l = entry.getKey();
				if (l.isTerminated()) {
					ReadyStateMonitor tracker = entry.getValue();
					iter.remove();
					tracker.dispose();
				}
			}
		}
	}

	/**
	 * Gets the JMX port for life-cycle tracking if this feature is enabled in
	 * the configuration.
	 */
	protected int getJMXPort(ILaunchConfiguration conf) {
		try {
			if (BootLaunchConfigurationDelegate.canUseLifeCycle(conf)) {
				String portStr = BootLaunchConfigurationDelegate.getJMXPort(conf);
				return Integer.parseInt(portStr);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return 0; // 0 means feature is disabled
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
		if (updateInProgress) {
			//Avoid bug caused by reentrance from same thread.
			//This bug causes double update events for INAVTIVE -> RUNNING for projects that
			// don't have ready state tracking and so immediately enter the ready state upon
			// creation.
			return Collections.emptySet();
		} else {
			updateInProgress = true;
			try {
				Map<IProject, RunState> oldStates = activeStates;
				debug("old: "+oldStates);
				activeStates = getCurrentActiveStates();
				debug("new: "+activeStates);

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
			} finally {
				updateInProgress = false;
			}
		}
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
	public void processTerminated(ProcessTracker tracker, IProcess process) {
		updateProjectStatesAndFireEvents();
		cleanupReadyStateTrackers();
	}

	@Override
	public void processCreated(ProcessTracker tracker, IProcess process) {
		updateProjectStatesAndFireEvents();
	}
}
