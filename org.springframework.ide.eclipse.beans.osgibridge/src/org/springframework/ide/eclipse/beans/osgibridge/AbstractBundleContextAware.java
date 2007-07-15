/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.osgibridge;

import org.osgi.framework.BundleContext;

/**
 * Abstract super class for components that need access to the current
 * {@link BundleContext}.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public abstract class AbstractBundleContextAware {
	
	/**
	 * The internal kept {@link BundleContext}
	 */ 
	private BundleContext bundleContext;
	
	/**
	 * Returns the {@link BundleContext}
	 * @return bundleContext
	 */
	protected BundleContext getBundleContext() {
		synchronized (bundleContext) {
			if (this.bundleContext == null) {
				this.bundleContext = Activator.getDefault().getBundleContext();
			}
		}
		return this.bundleContext;
	}
}