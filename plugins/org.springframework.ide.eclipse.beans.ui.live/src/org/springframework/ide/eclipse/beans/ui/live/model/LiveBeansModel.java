/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A model of a running Spring application to be graphed in the Live Beans Graph
 * 
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansModel implements Comparable<LiveBeansModel> {

	private final List<LiveBean> beans;

	private final List<LiveBeansGroup> contexts;

	private final List<LiveBeansGroup> resources;

	private final TypeLookup typeLookup;

	public LiveBeansModel(TypeLookup typeLookup) {
		this.beans = new ArrayList<LiveBean>();
		this.contexts = new ArrayList<LiveBeansGroup>();
		this.resources = new ArrayList<LiveBeansGroup>();
		this.typeLookup = typeLookup;
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
		if (typeLookup != null) {
			return typeLookup.getApplicationName();
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

	public TypeLookup getWorkspaceContext() {
		return typeLookup;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiveBeansModel) {
			LiveBeansModel other = (LiveBeansModel) obj;
			// Should be enough to compare contexts only since this is close to raw JSON data
			return contexts.equals(other.contexts) && Objects.equals(typeLookup, other.typeLookup);
		}
		return super.equals(obj);
	}

	
}
