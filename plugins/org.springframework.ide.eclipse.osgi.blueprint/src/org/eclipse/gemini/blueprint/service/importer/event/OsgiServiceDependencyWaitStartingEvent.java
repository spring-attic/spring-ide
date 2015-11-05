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
 * Dedicated event for OSGi dependencies that are imported in a timed manner.
 * The event indicates that a dependency is missing and a bean inside the
 * application context will start waiting for it, for a specified amount of time
 * (given as a maximum).
 * 
 * <p/> Note that the actual waiting starts shortly after the event is
 * dispatched however, there are no guarantees on when this will happen as it
 * depends on the number of listeners interested in this event (and the amount
 * of work done once the event is received).
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDependencyWaitStartingEvent extends OsgiServiceDependencyEvent {

	private final long timeToWait;


	/**
	 * Constructs a new <code>OsgiServiceDependencyWaitStartingEvent</code>
	 * instance.
	 * 
	 * @param source event source (usually the service importer)
	 * @param dependency dependency description
	 * @param timeToWait wait duration
	 */
	public OsgiServiceDependencyWaitStartingEvent(Object source, OsgiServiceDependency dependency, long timeToWait) {
		super(source, dependency);
		this.timeToWait = timeToWait;
	}

	/**
	 * Returns the time (in milliseconds) the source will wait for the OSGi
	 * service to appear.
	 * 
	 * @return Returns the timeToWait
	 */
	public long getTimeToWait() {
		return timeToWait;
	}
}
