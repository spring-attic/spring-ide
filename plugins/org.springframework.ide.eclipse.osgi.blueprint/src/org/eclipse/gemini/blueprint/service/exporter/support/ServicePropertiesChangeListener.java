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

import java.util.EventListener;

/**
 * Listener for receiving service properties change events.
 * 
 * @author Costin Leau
 */
public interface ServicePropertiesChangeListener extends EventListener {

	/**
	 * Notifies when the service properties have been changed.
	 * 
	 * @param event a service properties change event
	 */
	void propertiesChange(ServicePropertiesChangeEvent event);
}
