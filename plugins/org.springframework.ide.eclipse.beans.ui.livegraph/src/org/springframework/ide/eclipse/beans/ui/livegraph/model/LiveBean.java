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

/**
 * @author Leo Dos Santos
 */
public class LiveBean {

	private final String beanId;

	private final Set<LiveBean> children;

	private final Map<String, String> attributes; // TODO: will values be
													// Objects??

	public LiveBean(String id) {
		this.beanId = id;
		children = new HashSet<LiveBean>();
		attributes = new HashMap<String, String>();
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void addChild(LiveBean childId) {
		children.add(childId);
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public Set<LiveBean> getChildren() {
		return children;
	}

	public String getId() {
		return beanId;
	}

}
