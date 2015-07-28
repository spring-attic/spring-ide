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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Caches {@link CloudApplication} by application name. Fetching an updated
 * cloud application can be a long process, therefore for responsiveness, a
 * cache is maintained.
 * <p/>
 * API is also available to update the cache for Cloud operations that fetch an
 * updated cloud application as part of their execution.
 */
public class CloudAppCache {

	private final Map<String, CacheItem> appCache = new HashMap<String, CacheItem>();

	public CloudAppCache(CloudFoundryBootDashModel model) {
	}

	/**
	 *
	 * @param apps
	 * @return list of all applications that have changed.
	 */
	public synchronized List<String> updateAll(Map<CloudApplication, IProject> apps) {

		appCache.clear();
		List<String> changedApps = new ArrayList<String>();
		for (Entry<CloudApplication, IProject> entry : apps.entrySet()) {
			CacheItem old = appCache.get(entry.getKey().getName());

			// Preserve any run state as some run states are "derived" (e.g.
			// STARTING)
			RunState runState = null;
			if (old != null) {
				runState = old.runState;
			} else {
				runState = getRunStateFromCloudApp(entry.getKey());
			}

			CacheItem newItem = new CacheItem(entry.getKey(), entry.getValue(), runState);

			if (!newItem.equals(old)) {
				changedApps.add(newItem.appName);
			}
			appCache.put(newItem.appName, newItem);
		}
		return changedApps;
	}

	/**
	 * Update the cache
	 *
	 * @param appName
	 * @param runState
	 * @return true if changed, false otherwise
	 */
	public synchronized boolean updateCache(String appName, RunState runState) {
		CacheItem oldItem = appCache.get(appName);
		// can only update existing items
		if (oldItem != null) {
			IProject project = oldItem.project;
			CloudApplication app = oldItem.app;
			CacheItem newItem = new CacheItem(app, project, runState);
			return !newItem.equals(oldItem);
		}

		return false;
	}

	public synchronized boolean replace(CloudApplication app, IProject project, RunState runState) {
		// Do a full replace as all the information is available to replace
		CacheItem oldItem = appCache.get(app.getName());
		CacheItem newItem = new CacheItem(app, project, runState);

		appCache.put(newItem.appName, newItem);

		return !newItem.equals(oldItem);
	}

	public synchronized boolean updateCache(CloudApplication app, RunState runState) {

		CacheItem old = appCache.get(app.getName());

		IProject project = old != null ? old.project : null;
		CacheItem newItem = new CacheItem(app, project, runState != null ? runState : getRunStateFromCloudApp(app));

		appCache.put(app.getName(), newItem);

		return !newItem.equals(old);
	}

	public synchronized void remove(String appName) {
		appCache.remove(appName);
	}

	public synchronized CloudApplication getApp(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.app;
		}
		return null;
	}

	public synchronized RunState getRunState(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.runState;
		}
		return RunState.INACTIVE;
	}

	public synchronized IProject getProject(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.project;
		}
		return null;
	}

	public static RunState getRunStateFromCloudApp(CloudApplication app) {
		RunState appState = RunState.INACTIVE;

		if (app != null) {
			switch (app.getState()) {
			case STARTED:
				appState = RunState.RUNNING;
				break;
			case STOPPED:
				appState = RunState.INACTIVE;
				break;
			case UPDATING:
				appState = RunState.STARTING;
				break;
			}
		}

		return appState;
	}

	static class CacheItem {

		final CloudApplication app;
		final String appName;
		final RunState runState;
		final IProject project;

		public CacheItem(CloudApplication app, IProject project, RunState runState) {
			this.app = app;
			this.project = project;
			this.runState = runState;
			this.appName = app.getName();
		}

		// Don't use the CloudApplication to indicate equality. Only project,
		// runstate and appName

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appName == null) ? 0 : appName.hashCode());
			result = prime * result + ((project == null) ? 0 : project.hashCode());
			result = prime * result + ((runState == null) ? 0 : runState.hashCode());
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
			CacheItem other = (CacheItem) obj;
			if (appName == null) {
				if (other.appName != null)
					return false;
			} else if (!appName.equals(other.appName))
				return false;
			if (project == null) {
				if (other.project != null)
					return false;
			} else if (!project.equals(other.project))
				return false;
			if (runState != other.runState)
				return false;
			return true;
		}

	}

}
