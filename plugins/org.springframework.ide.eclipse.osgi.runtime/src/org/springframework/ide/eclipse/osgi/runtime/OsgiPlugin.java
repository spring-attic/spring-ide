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
package org.springframework.ide.eclipse.osgi.runtime;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Bundle Activator for the osgi redeploy plugin.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class OsgiPlugin extends AbstractUIPlugin {

	/** The bundle symbolic name of this plugin */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.osgi.runtime";

	/**
	 * The variable to be used in the Eclipse UI to get replaced with the
	 * configured port
	 */
	public static final String PORT_PREFERENCE_KEY = "console.port";

	/** The default Equinox console port */
	public static final String DEFAULT_PORT = "2000";

	// The shared instance
	private static OsgiPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Install default port in preference store
		getPreferenceStore().setDefault(PORT_PREFERENCE_KEY, DEFAULT_PORT);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static OsgiPlugin getDefault() {
		return plugin;
	}

}
