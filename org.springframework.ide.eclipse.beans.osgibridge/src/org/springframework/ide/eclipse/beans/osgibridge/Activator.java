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
import org.springframework.osgi.extender.ContextLoaderListener;

/**
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class Activator extends ContextLoaderListener {
 
	public static final String PLUGIN_ID = 
		"org.springframework.ide.eclipse.beans.osgibridge";

	private static Activator plugin;

	private BundleContext bundleContext;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		this.bundleContext = null;
	}

	public static Activator getDefault() {
		return plugin;
	}
 
	public BundleContext getBundleContext() {
		return bundleContext;
	}
}
