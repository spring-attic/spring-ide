/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Christian Dupuis
 * @since 2.0
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
		try {
			this.preferences.put(propertyNamespace + key, value);
			this.preferences.flush();
		}
		catch (BackingStoreException e) {
			SpringCore.log(e);
		}
	}

	public void putBoolean(String key, boolean value) {
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
	
}
