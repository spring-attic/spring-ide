/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The Constant PLUGIN_ID.
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.webflow.core";

	/**
	 * The plugin.
	 */
	private static Activator plugin;

	/**
	 * The singleton beans model.
	 */
	private final static WebflowModel WEBFLOW_MODEL = new WebflowModel();

	/**
	 * The Constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		WEBFLOW_MODEL.startup();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		WEBFLOW_MODEL.shutdown();
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public static IWebflowModel getModel() {
		return WEBFLOW_MODEL;
	}

	/**
	 * Gets the default.
	 * 
	 * @return the default
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * 
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Writes the message to the plug-in's log.
	 * 
	 * @param exception
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}

	/**
	 * 
	 * 
	 * @param exception
	 */
	public static void log(Throwable exception) {
		getDefault().getLog().log(
				createErrorStatus("Internal Error", exception));
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in.
	 * 
	 * @param exception
	 * @param message
	 * 
	 * @return
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
	}
	

	public static String getPluginVersion() {
		Bundle bundle = getDefault().getBundle();
		return (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
	}

}
