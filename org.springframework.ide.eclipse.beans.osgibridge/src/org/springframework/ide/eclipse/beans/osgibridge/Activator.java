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

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.web.context.ContextLoaderListener;
import org.xml.sax.EntityResolver;

/**
 * OSGi {@link BundleActivator} that extends Spring's Dynamic Modules
 * {@link ContextLoaderListener} in order to get access to other bundles
 * {@link NamespaceHandlerResolver} and {@link EntityResolver}.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = 
		"org.springframework.ide.eclipse.beans.osgibridge";

	public static final String OSGI_EXTENDER_SYMBOLIC_NAME = 
		"org.springframework.bundle.osgi.extender";

	private static Activator plugin;

	private BundleContext context;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getBundleContext() {
		return context;
	}
}
