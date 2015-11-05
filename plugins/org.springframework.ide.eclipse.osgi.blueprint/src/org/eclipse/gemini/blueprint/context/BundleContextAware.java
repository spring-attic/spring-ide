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

package org.eclipse.gemini.blueprint.context;

import org.osgi.framework.BundleContext;

/**
 * Interface that enables beans to find the bundle context they are defined in.
 * 
 * Note that in most circumstances there is no need for a bean to implement this
 * interface.
 * 
 * @author Adrian Colyer
 */
public interface BundleContextAware {

	/**
	 * Set the {@link BundleContext} that this bean runs in. Normally this can
	 * be used to initialize an object.
	 * 
	 * @param bundleContext the <code>BundleContext</code> object to be used
	 * by this object
	 */
	 void setBundleContext(BundleContext bundleContext);
}
