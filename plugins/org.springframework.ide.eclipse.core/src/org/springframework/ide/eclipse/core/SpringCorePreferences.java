/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Christian Dupuis
 * @since 2.0
 * @deprecated Moved to eclipse-integration-commons SpringCorePreferences
 */
public class SpringCorePreferences {
	
	private final String propertyNamespace;

	private IEclipsePreferences preferences;
	
	private SpringCorePreferences(IProject project, String qualifier) {
		this.propertyNamespace = qualifier + '.';
		this.preferences = getEclipsePreferences(project, qualifier);
	}
	
	public static SpringCorePreferences getProjectPreferences(IProject project) {
		return getProjectPreferences(project, SpringCore.PLUGIN_ID);
	}

	public static SpringCorePreferences getProjectPreferences(IProject project, String qualifier) {
		return new SpringCorePreferences(project, qualifier);
	}
	
	private IEclipsePreferences getEclipsePreferences(IProject project, String qualifier) {
		IScopeContext context = new ProjectScope(project);
		IEclipsePreferences node = context.getNode(qualifier);
		return node;
	}
	
	public void putString(String key, String value) {
		if (key == null || value == null) {
			return;
		}
		try { 
			this.preferences.put(propertyNamespace + key, value);
			this.preferences.flush();
		}
		catch (BackingStoreException e) {
			SpringCore.log(e);
		}
	}

	public void putBoolean(String key, boolean value) {
		if (key == null) {
			return;
		}
		try {
			this.preferences.putBoolean(propertyNamespace + key, value);
			this.preferences.flush();
		}
		catch (BackingStoreException e) {
			SpringCore.log(e);
		}
	}
	
	public String getString(String key, String defaultValue) {
		return this.preferences.get(propertyNamespace + key, defaultValue);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return this.preferences.getBoolean(propertyNamespace + key, defaultValue);
	}
	
	public IEclipsePreferences getProjectPreferences() {
		return this.preferences;
	}
	
}
