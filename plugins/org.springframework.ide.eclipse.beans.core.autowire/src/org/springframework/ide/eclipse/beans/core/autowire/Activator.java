/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Activator for the beans.core.autowire plugin.
 * @author Christian Dupuis
 * @author Jared Rodriguez
 * @since 2.0.5
 */
public class Activator extends Plugin {
	
	/** The symbolic name of the bundle */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.core.autowire";
	
	/** The shared instance */
	private static Activator plugin;
	
	/** 
	 * Starts the plugin.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	/**
	 * Stops the plugin.
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
