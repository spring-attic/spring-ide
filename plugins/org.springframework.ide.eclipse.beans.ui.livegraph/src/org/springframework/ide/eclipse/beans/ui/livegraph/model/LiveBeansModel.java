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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A model of a running Spring application to be graphed in the Live Beans Graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansModel implements Comparable<LiveBeansModel> {

	private final List<LiveBean> beans;

	private final List<LiveBeansGroup> contexts;

	private final List<LiveBeansGroup> resources;

	private final LiveBeansSession session;

	public LiveBeansModel(LiveBeansSession session) {
		this.beans = new ArrayList<LiveBean>();
		this.contexts = new ArrayList<LiveBeansGroup>();
		this.resources = new ArrayList<LiveBeansGroup>();
		this.session = session;
	}

	public void addBeans(Collection<LiveBean> beansToAdd) {
		beans.addAll(beansToAdd);
	}

	public void addContexts(Collection<? extends LiveBeansGroup> contextsToAdd) {
		contexts.addAll(contextsToAdd);
	}

	public void addResources(Collection<? extends LiveBeansGroup> resourcesToAdd) {
		resources.addAll(resourcesToAdd);
	}

	public int compareTo(LiveBeansModel o) {
		return getApplicationName().compareTo(o.getApplicationName());
	}

	public String getApplicationName() {
		if (session != null) {
			return session.getApplicationName();
		}
		return "";
	}

	public List<LiveBean> getBeans() {
		return beans;
	}

	public List<LiveBeansGroup> getBeansByContext() {
		return contexts;
	}

	public List<LiveBeansGroup> getBeansByResource() {
		return resources;
	}

	public LiveBeansSession getSession() {
		return session;
	}

}
