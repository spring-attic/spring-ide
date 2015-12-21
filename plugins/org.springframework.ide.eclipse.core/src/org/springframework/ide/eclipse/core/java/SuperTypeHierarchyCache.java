/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Cache for {@link ITypeHierarchy} instances.
 * <p>
 * The implementation has been taken from {@link org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache}.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class SuperTypeHierarchyCache {

	/**
	 * Internal cache entry
	 */
	private static class HierarchyCacheEntry implements ITypeHierarchyChangedListener {

		private long lastAccess;

		private ITypeHierarchy typeHierarchy;

		public HierarchyCacheEntry(ITypeHierarchy hierarchy) {
			typeHierarchy = hierarchy;
			typeHierarchy.addTypeHierarchyChangedListener(this);
			markAsAccessed();
		}

		public void dispose() {
			typeHierarchy.removeTypeHierarchyChangedListener(this);
			typeHierarchy = null;
		}

		public long getLastAccess() {
			return lastAccess;
		}

		public ITypeHierarchy getTypeHierarchy() {
			return typeHierarchy;
		}

		public void markAsAccessed() {
			lastAccess = System.currentTimeMillis();
		}

		public void typeHierarchyChanged(ITypeHierarchy typeHierarchy) {
			removeHierarchyEntryFromCache(this);
		}
	}

	private static final int CACHE_SIZE = 50;

	private static List<HierarchyCacheEntry> HIERACHY_CACHE = new ArrayList<HierarchyCacheEntry>(CACHE_SIZE);

	private static void addTypeHierarchyToCache(ITypeHierarchy hierarchy) {
		synchronized (HIERACHY_CACHE) {
			int nEntries = HIERACHY_CACHE.size();
			if (nEntries >= CACHE_SIZE) {
				// find obsolete entries or remove entry that was least recently
				// accessed
				HierarchyCacheEntry oldest = null;
				List<HierarchyCacheEntry> obsoleteHierarchies = new ArrayList<HierarchyCacheEntry>(CACHE_SIZE);
				for (int i = 0; i < nEntries; i++) {
					HierarchyCacheEntry entry = (HierarchyCacheEntry) HIERACHY_CACHE.get(i);
					ITypeHierarchy curr = entry.getTypeHierarchy();
					if (!curr.exists() || hierarchy.contains(curr.getType())) {
						obsoleteHierarchies.add(entry);
					}
					else {
						if (oldest == null || entry.getLastAccess() < oldest.getLastAccess()) {
							oldest = entry;
						}
					}
				}
				if (!obsoleteHierarchies.isEmpty()) {
					for (int i = 0; i < obsoleteHierarchies.size(); i++) {
						removeHierarchyEntryFromCache((HierarchyCacheEntry) obsoleteHierarchies.get(i));
					}
				}
				else if (oldest != null) {
					removeHierarchyEntryFromCache(oldest);
				}
			}
			HierarchyCacheEntry newEntry = new HierarchyCacheEntry(hierarchy);
			HIERACHY_CACHE.add(newEntry);
		}
	}

	private static ITypeHierarchy findTypeHierarchyInCache(IType type) {
		synchronized (HIERACHY_CACHE) {
			for (int i = HIERACHY_CACHE.size() - 1; i >= 0; i--) {
				HierarchyCacheEntry curr = (HierarchyCacheEntry) HIERACHY_CACHE.get(i);
				ITypeHierarchy hierarchy = curr.getTypeHierarchy();
				if (!hierarchy.exists()) {
					removeHierarchyEntryFromCache(curr);
				}
				else {
					if (hierarchy.contains(type)) {
						curr.markAsAccessed();
						return hierarchy;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get the {@link ITypeHierarchy} for the given {@link IType}.
	 * <p>
	 * If no hierarchy can be found in the cache a new one will be created.
	 * <p>
	 * Calling this method is equivalent to calling {@link #getTypeHierarchy(IType, null)}.
	 * @param type the {@link IType} to get the super type hierarchy for
	 * @return the {@link ITypeHierarchy} for the given <code>type</code>
	 */
	public static ITypeHierarchy getTypeHierarchy(IType type) throws JavaModelException {
		return getTypeHierarchy(type, null);
	}

	/**
	 * Get the {@link ITypeHierarchy} for the given {@link IType}.
	 * <p>
	 * If no hierarchy can be found in the cache a new one will be created.
	 * @param type the {@link IType} to get the super type hierarchy for
	 * @param progressMonitor a {@link IProgressMonitor} instance to report progress
	 * @return the {@link ITypeHierarchy} for the given <code>type</code>
	 */
	public static ITypeHierarchy getTypeHierarchy(IType type, IProgressMonitor progressMonitor)
			throws JavaModelException {
		ITypeHierarchy hierarchy = findTypeHierarchyInCache(type);
		if (hierarchy == null) {
			hierarchy = type.newTypeHierarchy(progressMonitor);
			addTypeHierarchyToCache(hierarchy);
		}
		return hierarchy;
	}

	/**
	 * Checks if a {@link ITypeHierarchy} for a given {@link IType} is in the internal cache.
	 * @param type the {@link IType} to check if a {@link ITypeHierarchy} is cached
	 * @return true if a {@link ITypeHierarchy} is cached
	 */
	public static boolean hasInCache(IType type) {
		return findTypeHierarchyInCache(type) != null;
	}

	private static void removeHierarchyEntryFromCache(HierarchyCacheEntry entry) {
		synchronized (HIERACHY_CACHE) {
			entry.dispose();
			HIERACHY_CACHE.remove(entry);
		}
	}
}
