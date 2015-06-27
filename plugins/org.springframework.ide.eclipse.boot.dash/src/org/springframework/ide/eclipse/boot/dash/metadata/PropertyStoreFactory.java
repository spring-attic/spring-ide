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
package org.springframework.ide.eclipse.boot.dash.metadata;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Provides static helper methods to create IPropertyStore instances,
 */
public class PropertyStoreFactory {

	private static final String QUALIFIER = "microservice-metadata";

	public static IPropertyStore<IProject> createForProjects() {
		return new PreferenceBasedStore<IProject>() {
			protected IEclipsePreferences createPrefs(IProject p) {
				IEclipsePreferences prefs = new ProjectScope(p).getNode(QUALIFIER);
				return prefs;
			}
		};
	}

}
