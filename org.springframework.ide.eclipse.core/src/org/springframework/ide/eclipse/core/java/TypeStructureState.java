/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.project.IProjectContributor;

/**
 * State that makes the {@link TypeStructureCache} accessible to {@link IProjectContributor}s.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class TypeStructureState {

	private boolean useChangeDetectionInJavaFiles = false;

	public TypeStructureState() {
		this.useChangeDetectionInJavaFiles = SpringCore.getDefault().getPluginPreferences()
				.getBoolean(SpringCore.USE_CHANGE_DETECTION_IN_JAVA_FILES);
	}

	public boolean hasStructuralChanges(IResource resource) {
		return !useChangeDetectionInJavaFiles || TypeStructureCache.hasStructuralChanges(resource);
	}

}
