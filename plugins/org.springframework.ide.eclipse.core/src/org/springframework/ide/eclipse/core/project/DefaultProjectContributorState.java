/*******************************************************************************
 * Copyright (c) 2009, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.project;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link IProjectContributorState} interface.
 * @author Christian Dupuis
 */
public class DefaultProjectContributorState implements IProjectContributorState {

	private Map<Class, Object> managedObjects = new HashMap<Class, Object>();

	private Map<Dictionary<String, String>, Object> managedObjectsWithFilters = new HashMap<Dictionary<String, String>, Object>();

	public <T> T get(Class<T> clazz) {
		if (managedObjects.containsKey(clazz)) {
			return (T) managedObjects.get(clazz);
		}
		for (Map.Entry<Class, Object> entry : managedObjects.entrySet()) {
			if (clazz.isAssignableFrom(entry.getKey())) {
				return (T) entry.getValue();
			}
		}
		return null;
	}

	public boolean hold(Object obj) {
		if (managedObjects.containsKey(obj.getClass())) {
			return false;
		}
		else {
			managedObjects.put(obj.getClass(), obj);
			return true;
		}
	}

	public <T> T get(Class<T> clazz, String filterText) {
		if (!StringUtils.hasLength(filterText)) {
			return null;
		}
		
		try {
			Filter filter = FrameworkUtil.createFilter(filterText);
			for (Map.Entry<Dictionary<String, String>, Object> entry : managedObjectsWithFilters.entrySet()) {
				if (filter.match(entry.getKey())) {
					return (T) entry.getValue();
				}
			}
		}
		catch (InvalidSyntaxException e) {
			SpringCore.log(e);
		}
		
		return null;
	}

	public boolean hold(Object obj, Dictionary<String, String> attibutes) {
		if (managedObjectsWithFilters.containsKey(attibutes)) {
			return false;
		}
		else {
			managedObjectsWithFilters.put(attibutes, obj);
			return true;
		}
	}

}