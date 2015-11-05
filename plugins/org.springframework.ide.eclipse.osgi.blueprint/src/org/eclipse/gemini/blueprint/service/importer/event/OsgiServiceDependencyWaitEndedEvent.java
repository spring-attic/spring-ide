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
 * Importer event indicating that the wait for a given dependency has ended
 * (successfully), namely the dependency was found before the time allocated for
 * it elapsed.
 * 
 * @author Costin Leau
 * 
 */
public class OsgiServiceDependencyWaitEndedEvent extends OsgiServiceDependencyEvent {

	private final long waitedTime;


	/**
	 * Constructs a new <code>OsgiServiceDependencyWaitEndedEvent</code>
	 * instance.
	 * 
	 * @param source event source (usually the service importer)
	 * @param dependency dependency description
	 * @param elapsedTime time to wait
	 */
	public OsgiServiceDependencyWaitEndedEvent(Object source, OsgiServiceDependency dependency, long elapsedTime) {
		super(source, dependency);
		this.waitedTime = elapsedTime;
	}

	/**
	 * Returns the time spent (in milliseconds) waiting, until service was found
	 * (and the dependency considered satisfied).
	 * 
	 * @return Returns the time (in milliseconds) spent waiting for the OSGi
	 * service to appear
	 */
	public long getElapsedTime() {
		return waitedTime;
	}
}
