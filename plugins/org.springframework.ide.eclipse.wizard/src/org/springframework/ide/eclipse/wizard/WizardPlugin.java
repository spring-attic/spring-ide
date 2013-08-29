/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.wizard.template.BundleTemplateLocation;
import org.springsource.ide.eclipse.commons.content.core.ContentLocation;

/**
 * The activator class controls the plug-in life cycle
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class WizardPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.wizard"; //$NON-NLS-1$

	// The shared instance
	private static WizardPlugin plugin;

	/**
	 * Location of additional templates inside the bundle
	 */
	private BundleTemplateLocation bundleTemplateLocation;

	/**
	 * The constructor
	 */
	public WizardPlugin() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static WizardPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns one instance of template descriptor location inside the wizard
	 * bundle per runtime session. This location contains templates for simple
	 * template projects.
	 */
	public ContentLocation getTemplateContentLocation() {
		if (bundleTemplateLocation == null) {
			bundleTemplateLocation = new BundleTemplateLocation();
		}
		return bundleTemplateLocation;
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

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus(exception));
	}
}
