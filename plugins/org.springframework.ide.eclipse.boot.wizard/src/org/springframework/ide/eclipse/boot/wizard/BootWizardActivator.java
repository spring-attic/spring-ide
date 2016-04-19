/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

public class BootWizardActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.wizard"; //$NON-NLS-1$
	private static BootWizardActivator plugin;

	public BootWizardActivator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static BootWizardActivator getDefault() {
		return plugin;
	}

	public static IStatus createErrorStatus(Throwable exception) {
		String message = exception.getMessage();
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static IStatus createInfoStatus(String msg) {
		return new Status(IStatus.INFO, PLUGIN_ID, msg);
	}

	public static void log(Throwable e) {
		getDefault().getLog().log(createErrorStatus(e));
	}

	public static void info(String msg) {
		getDefault().getLog().log(createInfoStatus(msg));
	}

	public static URLConnectionFactory getUrlConnectionFactory() {
		final String userAgent = "STS/"+BootWizardActivator.getDefault().getBundle().getVersion();
		//TODO: post 3.7.2 the URLConnectionFactory in master will have support for adding userAgent string
		//  so we do not have to implement it here by subclassing.
		return new URLConnectionFactory() {
			@Override
			public URLConnection createConnection(URL url) throws IOException {
				URLConnection conn = super.createConnection(url);
				conn.addRequestProperty("User-Agent", userAgent);
				return conn;
			}
		};
	}
}
