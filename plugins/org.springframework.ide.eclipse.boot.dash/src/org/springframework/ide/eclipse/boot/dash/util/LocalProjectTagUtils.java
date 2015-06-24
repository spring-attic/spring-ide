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
package org.springframework.ide.eclipse.boot.dash.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Utility methods for local Boot projects to load/persists various settings 
 * 
 * @author Alex Boyko
 *
 */
public class LocalProjectTagUtils {
	
	/**
	 * The name of the file where various micro-service meta-data is stored (project/.settings/QUALIFIER.prefs)
	 */
	private static final String QUALIFIER = "microservice-metadata";
	
	/**
	 * Preference key for tags string
	 */
	private static final String TAGS_PROPERTY_KEY = "tags";
	
	/**
	 * String separator between tags string representation
	 */
	public static String SEPARATOR = " ";
	
	/**
	 * Loads tags for specific project
	 * @param project the workspace project
	 * @return array of tags
	 */
	public static String[] loadTags(IProject project) {
		return new ProjectScope(project).getNode(QUALIFIER).get(TAGS_PROPERTY_KEY, "").split(SEPARATOR);
	}
	
	/**
	 * Persists array of tags for a project
	 * @param project the workspace project
	 * @param tags the tags to persist
	 * @throws CoreException
	 */
	public static void saveTags(IProject project, String[] tags) throws CoreException {
		IEclipsePreferences prefs = new ProjectScope(project).getNode(QUALIFIER);
		if (tags.length == 0) {
			prefs.remove(TAGS_PROPERTY_KEY);
		} else {
			prefs.put(TAGS_PROPERTY_KEY, StringUtils.join(tags, SEPARATOR));
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "Failed to persist tags", e));
		}
	}
	
}
