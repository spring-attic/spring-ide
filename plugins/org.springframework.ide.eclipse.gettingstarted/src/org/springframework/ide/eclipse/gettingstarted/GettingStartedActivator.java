/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.gettingstarted.preferences.GettingStartedPreferences;

/**
 * The activator class controls the plug-in life cycle
 */
public class GettingStartedActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.gettingstarted"; //$NON-NLS-1$

	// The shared instance
	private static GettingStartedActivator plugin;

	private GettingStartedPreferences prefs;

//	private ServiceTracker tracker;
	
	/**
	 * The constructor
	 */
	public GettingStartedActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GettingStartedActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus(exception));
	}
	
	public static void warn(String msg) {
		getDefault().getLog().log(createWarningStatus(msg));
	}

	private static IStatus createWarningStatus(String msg) {
		return new Status(IStatus.WARNING, PLUGIN_ID, msg);
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(Throwable exception) {
		String message = exception.getMessage();
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	
	public GettingStartedPreferences getPreferences() {
		if (prefs==null) {
			prefs = new GettingStartedPreferences(InstanceScope.INSTANCE.getNode(GettingStartedActivator.PLUGIN_ID));
		}
		return prefs;
	}
	
//	@SuppressWarnings("unchecked")
//	public synchronized IProxyService getProxyService() {
//		if (proxyService == null) {
//			if (tracker == null) {
//				tracker = new ServiceTracker(getBundle().getBundleContext(), IProxyService.class.getName(), null);
//				tracker.open();
//			}
//
//			proxyService = (IProxyService) tracker.getService();
//		}
//		return proxyService;
//	}

	
}
