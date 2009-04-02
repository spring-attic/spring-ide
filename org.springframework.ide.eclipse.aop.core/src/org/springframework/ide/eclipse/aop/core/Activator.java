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
package org.springframework.ide.eclipse.aop.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;
import org.springframework.ide.eclipse.core.MessageUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.aop.core";
	
	public static final String PERSIST_AOP_MODEL_PREFERENCE = PLUGIN_ID + ".persistModel";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	// The shared instance
	private static Activator plugin;

	private static AopReferenceModel model;
	
	/** Resource bundle */
	private ResourceBundle resourceBundle;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
		model = new AopReferenceModel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		} catch (MissingResourceException e) {
			resourceBundle = null;
		}
		// add default value
		getPreferenceStore().setDefault(PERSIST_AOP_MODEL_PREFERENCE, true);

		Job modelJob = new Job("Initializing Aop Model") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				model.start();
				return Status.OK_STATUS;
			}
		};
//		modelJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		modelJob.setSystem(true);
		modelJob.setPriority(Job.INTERACTIVE);
		modelJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		model.shutdown();
		plugin = null;
		resourceBundle = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static IAopReferenceModel getModel() {
		return model;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(
				createErrorStatus("Internal Error", exception));
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}
	
	public static String getFormattedMessage(String key, Object... args) {
		return MessageUtils.format(getResourceString(key), args);
	}
	
	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		String bundleString;
		ResourceBundle bundle = getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				bundleString = bundle.getString(key);
			} catch (MissingResourceException e) {
				log(e);
				bundleString = "!" + key + "!";
			}
		} else {
			bundleString = "!" + key + "!";
		}
		return bundleString;
	}
	
	/**
	 * Returns the plugin's resource bundle,
	 */
	public final ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
