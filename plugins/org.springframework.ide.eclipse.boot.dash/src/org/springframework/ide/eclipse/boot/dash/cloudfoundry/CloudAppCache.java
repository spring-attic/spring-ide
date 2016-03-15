/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
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

	//TODO: when we use v2 client this info is probably integerated with the rest of the appCache.
	// Then this should be removed.
	private Map<String, String> healthChecks = new HashMap<>();


	public CloudAppCache() {
	}


	public synchronized String getHealthCheck(CloudAppDashElement e) {
		return healthChecks.get(e.getName());
	}

	public synchronized void setHealthCheck(CloudAppDashElement e, String healthCheck) {
		healthChecks.put(e.getName(), healthCheck);
	}

	/**
	 *
	 * @param apps
	 * @return list of all applications that have changed.
	 */
	public synchronized List<String> updateAll(Collection<CloudAppInstances> apps) {

		Map<String, CacheItem> updatedCache = new HashMap<String, CacheItem>();
		List<String> changedApps = new ArrayList<String>();
		for (CloudAppInstances appInstances : apps) {

			CacheItem old = appCache.get(appInstances.getApplication().getName());

			CacheItem newItem = new CacheItem(appInstances);

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
	 * @return true if changed, false otherwise
	 */
	public synchronized boolean updateCache(String appName) {
		CacheItem oldItem = appCache.get(appName);
		// can only update existing items
		if (oldItem != null) {
			CloudAppInstances appInstances = oldItem.appInstances;
			CacheItem newItem = new CacheItem(appInstances);
			appCache.put(newItem.appName, newItem);
			return !newItem.equals(oldItem);
		}
		return false;
	}

	/**
	 * Updates the cache with the given app instances. If the item
	 * in the cache does not exist, it will create one for that application
	 *
	 * @param instances
	 * @return true if the application cached state changed. False otherwise
	 *         (meaning the old and new state are identical)
	 */
	public synchronized boolean updateCache(CloudAppInstances instances) {
		CacheItem old = appCache.get(instances.getApplication().getName());
		CacheItem newItem = new CacheItem(instances);
		appCache.put(newItem.appName, newItem);
		return !newItem.equals(old);
	}

	public synchronized void remove(String appName) {
		appCache.remove(appName);
	}

	public synchronized CFApplication getApp(String appName) {
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

	/*
	 *
	 * TODO: replace with CloudApplicationDeploymentProperties to use one
	 * "model" to represent deployment properties of an app
	 */
	static class CacheItem {

		final CloudAppInstances appInstances;
		final String appName;
		final int totalInstances;
		final int runningInstances;

		public CacheItem(CloudAppInstances appInstances) {
			this.appInstances = appInstances;
			this.appName = appInstances.getApplication().getName();
			this.totalInstances = appInstances.getApplication().getInstances();
			this.runningInstances = appInstances.getApplication().getRunningInstances();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((appName == null) ? 0 : appName.hashCode());
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
			if (runningInstances != other.runningInstances)
				return false;
			if (totalInstances != other.totalInstances)
				return false;
			return true;
		}

		// Don't use the CloudApplication to indicate equality. Only those
		// properties
		// that should trigger an app change event


	}

}
