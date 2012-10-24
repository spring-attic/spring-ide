/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Leo Dos Santos
 */
public class LiveBean implements IAdaptable {

	public static final String ATTR_BEAN = "bean";

	public static final String ATTR_SCOPE = "scope";

	public static final String ATTR_TYPE = "type";

	public static final String ATTR_RESOURCE = "resource";

	public static final String ATTR_DEPENDENCIES = "dependencies";

	public static final String ATTR_APPLICATION = "application name";

	private final String beanId;

	private final Set<LiveBean> dependencies;

	private final Map<String, String> attributes;

	public LiveBean(String id) {
		this.beanId = id;
		dependencies = new HashSet<LiveBean>();
		attributes = new HashMap<String, String>();
		attributes.put(ATTR_BEAN, id);
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void addDependency(LiveBean dependency) {
		dependencies.add(dependency);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new LiveBeanPropertySource(this);
		}
		return null;
	}

	public String getApplicationName() {
		return attributes.get(ATTR_APPLICATION);
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getBeanType() {
		return attributes.get(ATTR_TYPE);
	}

	public Set<LiveBean> getDependencies() {
		return dependencies;
	}

	public String getId() {
		return beanId;
	}

	public String getResource() {
		return attributes.get(ATTR_RESOURCE);
	}

}
