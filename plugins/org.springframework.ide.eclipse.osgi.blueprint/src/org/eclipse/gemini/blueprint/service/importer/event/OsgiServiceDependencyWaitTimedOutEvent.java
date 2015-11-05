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

/**
 * Event raised when an OSGi service dependency could not be found in a certain
 * amount of time. Normally thrown by OSGi importers, this event allows
 * notifications of potential failures inside the application context due to
 * missing (but required) OSGi dependencies.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDependencyWaitTimedOutEvent extends OsgiServiceDependencyEvent {

	private final long elapsedTime;


	/**
	 * Constructs a new <code>OsgiServiceDependencyWaitTimedOutEvent</code>
	 * instance.
	 * 
	 * @param source event source (usually a service importer)
	 * @param dependency service dependency description
	 * @param elapsedTime time spent waiting
	 */
	public OsgiServiceDependencyWaitTimedOutEvent(Object source, OsgiServiceDependency dependency, long elapsedTime) {
		super(source, dependency);
		this.elapsedTime = elapsedTime;
	}

	/**
	 * Returns the time (in milliseconds) the source waited for OSGi service to
	 * appear before failing.
	 * 
	 * @return Returns the timeToWait
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
}
