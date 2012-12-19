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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * @author Leo Dos Santos
 */
public class LiveBean extends AbstractLiveBeansModelElement {

	public static final String ATTR_BEAN = "bean";

	public static final String ATTR_SCOPE = "scope";

	public static final String ATTR_TYPE = "type";

	public static final String ATTR_RESOURCE = "resource";

	public static final String ATTR_DEPENDENCIES = "dependencies";

	public static final String ATTR_APPLICATION = "application name";

	private final String beanId;

	private String displayName;

	private final Set<LiveBean> dependencies;

	private final boolean innerBean;

	public LiveBean(String id) {
		this(id, false);
	}

	public LiveBean(String id, boolean innerBean) {
		super();
		this.beanId = id;
		this.innerBean = innerBean;
		dependencies = new HashSet<LiveBean>();
		attributes.put(ATTR_BEAN, id);
	}

	public void addDependency(LiveBean dependency) {
		dependencies.add(dependency);
	}

	public String getApplicationName() {
		return attributes.get(ATTR_APPLICATION);
	}

	public String getBeanType() {
		return attributes.get(ATTR_TYPE);
	}

	public Set<LiveBean> getDependencies() {
		return dependencies;
	}

	public String getDisplayName() {
		// compute the display name the first time it's needed
		if (displayName == null) {
			// truncate Class names and name with multiple segments
			if ((getBeanType() != null && beanId.contains(getBeanType())) || StringUtils.countMatches(beanId, ".") > 1) {
				int index = beanId.lastIndexOf('.');
				if (index >= 0) {
					displayName = beanId.substring(index + 1, beanId.length());
				}
			}
			if (displayName == null) {
				displayName = beanId;
			}
		}
		return displayName;
	}

	public String getId() {
		return beanId;
	}

	public String getResource() {
		return attributes.get(ATTR_RESOURCE);
	}

	public String getScope() {
		return attributes.get(ATTR_SCOPE);
	}

	public boolean isInnerBean() {
		return innerBean;
	}

}
