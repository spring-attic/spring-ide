/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.project.IProjectContributor;

/**
 * State that makes the {@link TypeStructureCache} accessible to {@link IProjectContributor}s.
 * @author Christian Dupuis
 * @since 2.2.0
 */
@SuppressWarnings("deprecation")
public class TypeStructureState {

	private boolean useChangeDetectionInJavaFiles = false;

	// Internal cache to speed up the checking
	private Map<IResource, Boolean> resultsCache = new ConcurrentHashMap<IResource, Boolean>();

	public TypeStructureState() {
		this.useChangeDetectionInJavaFiles = SpringCore.getDefault().getPluginPreferences().getBoolean(
				SpringCore.USE_CHANGE_DETECTION_IN_JAVA_FILES);
	}

	/**
	 * Checks the given java elements represented as {@link IResource} if any structural changes occurred since the last
	 * check.
	 * @param resource the resource to check for changes
	 * @param flags flags indicating interest in special changes
	 * @return <code>true</code> if any structure change occured
	 * @see ITypeStructureCache#FLAG_ANNOTATION
	 * @see ITypeStructureCache#FLAG_ANNOTATION_VALUE
	 * @see ITypeStructureCache#FLAG_TAB_BITS
	 */
	public boolean hasStructuralChanges(IResource resource, int flags) {
		// First check the internal cache for faster access
		if (!resultsCache.containsKey(resource)) {
			// Only if not in cache do the calculation
			boolean result = !useChangeDetectionInJavaFiles
					|| SpringCore.getTypeStructureCache().hasStructuralChanges(resource, flags);
			// Store value in cache and proceed;
			resultsCache.put(resource, result);
		}

		return resultsCache.get(resource);
	}

	/**
	 * Checks the given java elements represented as {@link IResource} if any structural changes occurred since the last
	 * check.
	 * @param resource the resource to check for changes
	 * @return <code>true</code> if any structure change occured
	 */
	public boolean hasStructuralChanges(IResource resource) {
		return hasStructuralChanges(resource, 0);
	}

}
