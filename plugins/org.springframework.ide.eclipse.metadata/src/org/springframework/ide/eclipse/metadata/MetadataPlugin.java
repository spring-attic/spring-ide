/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The bundle activator for the metadata plugin
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class MetadataPlugin extends AbstractUIPlugin {

	/** The bundle symbolic name of this plugin */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.metadata"; //$NON-NLS-1$
	
	/** The shared instance */
	private static MetadataPlugin plugin;
	
	/**
	 * Starts the bundle.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	/**
	 * Initialize the bundle's {@link ImageRegistry}.
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		MetadataUIImages.initializeImageRegistry(registry);
	}

	/**
	 * Stops the bundle.
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/** 
	 * Returns the shared instance.
	 */
	public static MetadataPlugin getDefault() {
		return plugin;
	}

}
