/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.ide.eclipse.core.ui.properties;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Utility class for accessing project properties.
 * 
 * @author Pierre-Antoine Grégoire
 */
public final class ProjectPropertiesFacade {
	private ProjectPropertiesFacade() {
	}

	public static Object getProjectProperty(Map defaultValues, IProject project, String propertyName, boolean failIfNull) {
		Object result;
		try {
			result = project.getPersistentProperty(new QualifiedName("", propertyName));
			if (failIfNull && result == null) {
				throw new NullPointerException("No such property as " + propertyName + " defined for project" + project);
			} else if (result == null) {
				initPropertiesForProjectWithDefaultValues(defaultValues, project);
				result = project.getPersistentProperty(new QualifiedName("", propertyName));
			}
		} catch (CoreException e) {
			if (failIfNull) {
				throw new NullPointerException("No such property as " + propertyName + " defined for project" + project);
			} else {
				result = defaultValues.get(propertyName);
			}
		}
		return result;
	}

	public static Object getDefaultPropertyValue(Map defaultValues, String propertyName) {
		return defaultValues.get(propertyName);
	}

	public static void setDefaultPropertyValue(Map defaultValues, IProject project, String propertyName, String value) {
		defaultValues.put(propertyName, value);
		initPropertiesForProjectWithDefaultValues(defaultValues, project);
	}

	public static void setProjectProperty(Map defaultValues, IProject project, String propertyName, String value) throws CoreException {
		project.setPersistentProperty(new QualifiedName("", propertyName), value);
	}

	public static void initPropertiesForProjectWithDefaultValues(Map defaultValues, IProject project) {
		String key = null;
		int errorCounter = 0;
		for (Iterator it = defaultValues.keySet().iterator(); it.hasNext();) {
			key = (String) it.next();
			try {
				project.setPersistentProperty(new QualifiedName("", key), (String) defaultValues.get(key));
			} catch (CoreException e) {
				errorCounter++;
			}
		}
		if (errorCounter > 0) {
			throw new NullPointerException("Did not load " + errorCounter + " properties for project " + project);
		}
	}
}
