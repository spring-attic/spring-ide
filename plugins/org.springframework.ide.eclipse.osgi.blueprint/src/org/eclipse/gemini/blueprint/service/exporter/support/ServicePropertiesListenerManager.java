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

package org.eclipse.gemini.blueprint.service.exporter.support;


/**
 * Service properties change listener manager.
 * 
 * @author Costin Leau
 */
public interface ServicePropertiesListenerManager {

	/**
	 * Adds a listener to be notified of any service properties changes.
	 * 
	 * @param listener service properties change listener
	 */
	void addListener(ServicePropertiesChangeListener listener);

	/**
	 * Removes a listener interested in service properties changes.
	 * 
	 * @param listener service properties change listener
	 */
	void removeListener(ServicePropertiesChangeListener listener);
}
