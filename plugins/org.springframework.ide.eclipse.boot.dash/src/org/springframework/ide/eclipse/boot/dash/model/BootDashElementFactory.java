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
 * <p>
 * To avoid the element cache from becoming a memory leak, clients are expected to call 'retainOnly'
 * at points in time where they have a complete picture of all relevant elements to retain (whereas
 * any others can this be safely discarded at this point).
 *
 * @author Kris De Volder
 */
public class BootDashElementFactory {

	private LocalBootDashModel model;
	private IScopedPropertyStore<IProject> projectProperties;

	private Map<IProject, BootProjectDashElement> cache;

	public BootDashElementFactory(LocalBootDashModel model, IScopedPropertyStore<IProject> projectProperties) {
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
				cache.put(p, el = new BootProjectDashElement(p, model, projectProperties));
			}
			return el;
		}
		return null;
	}

	public void dispose() {
		cache = null;
	}

}
