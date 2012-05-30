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
package org.springframework.ide.eclipse.roo.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.roo.ui.internal.listener.RooEarlyStartup;


/**
 * The activator class controls the plug-in life cycle
 * @author Christian Dupuis
 * @author Andrew Eisenberg
 */
public class RooUiActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.roo.ui";
	
	public static final String LEGACY_ID = "com.springsource.sts.roo.ui";

	// The shared instance
	private static RooUiActivator plugin;
	

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// should already have been called by early
		// start up, but this may have been disabled
		// by user
		RooEarlyStartup.registerListener();
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		RooEarlyStartup.unregisterListener();
	}

	public static RooUiActivator getDefault() {
		return plugin;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
}