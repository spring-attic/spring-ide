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
 * Caches {@link CloudApplication} and associated information, like the project
 * mapping and running app instances and stats, by application name. Fetching an
 * updated cloud application and its instance and stat information can be a
 * long-running process, therefore for responsiveness, a cache is maintained.
 * <p/>
 * It also allows the {@link BootDashElement} for Cloud applications to be a
 * stateless handle, making the element life-cycle management simpler and more
 * likely to be consistent in terms of providing information about the actual
 * application regardless how many times the element is created or deleted.
 * <p/>
 * API is also available to update the cache
 */
public class CloudAppCache {

	private final Map<String, CacheItem> appCache = new HashMap<String, CacheItem>();

	public CloudAppCache() {
	}

	/**
	 *
	 * @param apps
	 * @return list of all applications that have changed.
	 */
	public synchronized List<String> updateAll(Map<CloudAppInstances, IProject> apps) {

		Map<String, CacheItem> updatedCache = new HashMap<String, CacheItem>();
		List<String> changedApps = new ArrayList<String>();
		for (Entry<CloudAppInstances, IProject> entry : apps.entrySet()) {

			CloudAppInstances appInstances = entry.getKey();
			IProject project = entry.getValue();
			CacheItem old = appCache.get(appInstances.getApplication().getName());

			// Preserving any old state to avoid state briefly "flashing" to
			// UNKNOWN on refresh
			RunState runState = old != null && old.runState != null ? old.runState
					: ApplicationRunningStateTracker.getRunState(appInstances);

			CacheItem newItem = new CacheItem(appInstances, project, runState);

			if (!newItem.equals(old)) {
				changedApps.add(newItem.appName);
			}
			updatedCache.put(newItem.appName, newItem);
		}

		appCache.clear();
		appCache.putAll(updatedCache);
		return changedApps;
	}

	/**
	 * Update the cache for an existing entry with the given application name.
	 * If the entry does not exist, not update is performed as there is not
	 * sufficient information passed to create a new one.
	 * <p/>
	 * This method should only be used to update existing cache items.
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
			CloudAppInstances appInstances = oldItem.appInstances;
			CacheItem newItem = new CacheItem(appInstances, project, runState);
			appCache.put(newItem.appName, newItem);
			return !newItem.equals(oldItem);
		}
		return false;
	}

	/**
	 *
	 * @param instances
	 * @param project
	 * @param runState
	 * @return true if the application cached state changed. False otherwise
	 *         (meaning the old and new state are identical even after
	 *         replacing)
	 */
	public synchronized boolean replace(CloudAppInstances instances, IProject project, RunState runState) {
		// Do a full replace as all the information is available to replace
		CacheItem oldItem = appCache.get(instances.getApplication().getName());
		CacheItem newItem = new CacheItem(instances, project, runState);

		appCache.put(newItem.appName, newItem);

		return !newItem.equals(oldItem);
	}

	/**
	 * Updates the cache with the given app instances and run state. If the item
	 * in the cache does not exist, it will create one for that application
	 *
	 * @param instances
	 * @param runState
	 * @return true if the application cached state changed. False otherwise
	 *         (meaning the old and new state are identical)
	 */
	public synchronized boolean updateCache(CloudAppInstances instances, RunState runState) {

		CacheItem old = appCache.get(instances.getApplication().getName());

		IProject project = old != null ? old.project : null;
		CacheItem newItem = new CacheItem(instances, project, runState);

		appCache.put(newItem.appName, newItem);

		return !newItem.equals(old);
	}

	public synchronized void remove(String appName) {
		appCache.remove(appName);
	}

	public synchronized CloudApplication getApp(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.appInstances.getApplication();
		}
		return null;
	}

	public synchronized CloudAppInstances getAppInstances(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.appInstances;
		}
		return null;
	}

	public synchronized List<CloudAppInstances> getAppInstances() {
		List<CloudAppInstances> instances = new ArrayList<CloudAppInstances>();

		for (CacheItem item : appCache.values()) {
			instances.add(item.appInstances);
		}
		return instances;
	}

	public synchronized RunState getRunState(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.runState;
		}
		return RunState.UNKNOWN;
	}

	public synchronized IProject getProject(String appName) {
		CacheItem item = appCache.get(appName);
		if (item != null) {
			return item.project;
		}
		return null;
	}

	/*
	 *
	 * TODO: replace with CloudApplicationDeploymentProperties to use one
	 * "model" to represent deployment properties of an app
	 */
	static class CacheItem {

		final CloudAppInstances appInstances;
		final String appName;
		final RunState runState;
		final IProject project;
		final int totalInstances;
		final int runningInstances;

		public CacheItem(CloudAppInstances appInstances, IProject project, RunState runState) {
			this.appInstances = appInstances;
			this.project = project;
			this.runState = runState;
			this.appName = appInstances.getApplication().getName();
			this.totalInstances = appInstances.getApplication().getInstances();
			this.runningInstances = appInstances.getApplication().getRunningInstances();
		}

		// Don't use the CloudApplication to indicate equality. Only those
		// properties
		// that should trigger an app change event

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appName == null) ? 0 : appName.hashCode());
			result = prime * result + ((project == null) ? 0 : project.hashCode());
			result = prime * result + ((runState == null) ? 0 : runState.hashCode());
			result = prime * result + runningInstances;
			result = prime * result + totalInstances;
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
			if (runningInstances != other.runningInstances)
				return false;
			if (totalInstances != other.totalInstances)
				return false;
			return true;
		}
	}
}
