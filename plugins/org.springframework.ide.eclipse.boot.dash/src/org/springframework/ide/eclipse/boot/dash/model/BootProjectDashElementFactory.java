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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;

import com.google.common.collect.MapMaker;

/**
 * Manages the creating of BootProjectDashElement. It keeps track of all the created instances and
 * ensures that if an element represents the same entity then the instance is reused.
 *
 * @author Kris De Volder
 */
public class BootProjectDashElementFactory {

	private LocalBootDashModel model;
	private IScopedPropertyStore<IProject> projectProperties;

	private Map<IProject, BootProjectDashElement> cache;

	public BootProjectDashElementFactory(LocalBootDashModel model, IScopedPropertyStore<IProject> projectProperties) {
		this.cache = new MapMaker()
				.concurrencyLevel(1) //single thread only so don't waste space for 'connurrencyLevel' support
				.weakValues()
				.makeMap();
		this.model = model;
		this.projectProperties = projectProperties;
	}

	public synchronized BootDashElement createOrGet(IProject p) {
		if (BootPropertyTester.isBootProject(p)) {
			BootProjectDashElement el = cache.get(p);
			if (el==null) {
				cache.put(p, el = new BootProjectDashElement(p, model, projectProperties, this));
			}
			return el;
		}
		return null;
	}

	public void dispose() {
		cache = null;
	}

	/**
	 * Clients should call this when elements are no longer relevant
	 * @param e
	 */
	public void disposed(BootProjectDashElement e) {
		Map<IProject, BootProjectDashElement> c = cache;
		if (c!=null) {
			c.remove(e.getProject());
		}
	}

}
