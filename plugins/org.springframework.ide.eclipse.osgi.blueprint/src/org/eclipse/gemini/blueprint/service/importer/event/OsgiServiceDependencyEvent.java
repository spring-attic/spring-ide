/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.event;

import org.eclipse.gemini.blueprint.service.importer.OsgiServiceDependency;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

/**
 * Base event type used for sending dependencies notifications. Usually these
 * are sent by Spring-DM OSGi importers.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiServiceDependencyEvent extends ApplicationEvent {

	private final OsgiServiceDependency dependency;


	/**
	 * Constructs a new <code>OsgiServiceDependencyEvent</code> instance.
	 * 
	 * @param source event source (usually the service importer)
	 * @param dependency dependency description
	 */
	public OsgiServiceDependencyEvent(Object source, OsgiServiceDependency dependency) {
		super(source);
		Assert.notNull(dependency);
		this.dependency = dependency;
	}

	/**
	 * Returns the OSGi service dependency filter for which this event is
	 * triggered.
	 * 
	 * @return Returns the dependencyServiceFilter
	 */
	public OsgiServiceDependency getServiceDependency() {
		return dependency;
	}
}
