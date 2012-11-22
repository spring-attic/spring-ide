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
package org.springframework.ide.eclipse.roo.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.springframework.ide.eclipse.roo.core.model.RooInstallManager;
import org.springframework.roo.felix.HttpPgpUrlStreamHandlerServiceImpl;
import org.springframework.roo.felix.pgp.PgpServiceImpl;
import org.springframework.roo.url.stream.UrlInputStreamService;


/**
 * The activator class controls the plug-in life cycle
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class RooCoreActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.roo.core";
	
	public static String NATURE_ID = "com.springsource.sts.roo.core.nature";

	/** The identifier for enablement of project versus workspace settings */
	public static final String PROJECT_PROPERTY_ID = "use.default.install";

	public static final String ROO_INSTALL_PROPERTY = PLUGIN_ID + ".install.name";
	
	// The shared instance
	private static RooCoreActivator plugin;

	private RooInstallManager installManager;

	public RooInstallManager getInstallManager() {
		return installManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.installManager = new RooInstallManager();
		this.installManager.start();
		
		Job registerJob = new Job("Initializing Spring Roo Tooling") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				registerHttpPgpUrlHandler(context);
				return Status.OK_STATUS;
			}
			
		};
		registerJob.setSystem(true);
		registerJob.schedule();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	private void invokeMethod(String fieldName, Object target) {
		try {
			Method method = target.getClass().getDeclaredMethod(fieldName);
			method.setAccessible(true);
			method.invoke(target);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registerHttpPgpUrlHandler(BundleContext context) {
		Hashtable<String,String> dict = new Hashtable<String,String>();
		dict.put(URLConstants.URL_HANDLER_PROTOCOL, "httppgp");
		
		
		// Create our own forwarding UrlInputStreamService
		UrlInputStreamService inputStreamService = new UrlInputStreamService() {
			public InputStream openConnection(URL url) throws IOException {
				return url.openStream();
			}

			public String getUrlCannotBeOpenedMessage(URL url) {
				return "Cannot open URL " + url.toString();
			}
		};
		
		// Setup the PGP verification service
		PgpServiceImpl pgpService = new PgpServiceImpl();
		setField("context", pgpService, context);
		setField("urlInputStreamService", pgpService, inputStreamService);
		invokeMethod("trustDefaultKeys", pgpService);
		
		// Prepare the Http PGP Url Handler and register as OSGi service
		HttpPgpUrlStreamHandlerServiceImpl httpPgpService = new HttpPgpUrlStreamHandlerServiceImpl();
		setField("urlInputStreamService", httpPgpService, inputStreamService);
		setField("pgpService", httpPgpService, pgpService);
		
		context.registerService(URLStreamHandlerService.class.getName(), httpPgpService, dict);
	}
	
	
	private void setField(String fieldName, Object target, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static RooCoreActivator getDefault() {
		return plugin;
	}

	public static InputStream getBundleInputStream(String location) {
		try {
			return getDefault().getBundle().getEntry(location).openStream();
		}
		catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAutoDeployBundleLocations() {
		List<String> result = new ArrayList<String>();
		Enumeration<String> libs = getDefault().getBundle().getEntryPaths("/bundle/");
		while (libs.hasMoreElements()) {
			String lib = libs.nextElement();
			if (lib.endsWith(".jar")) {
				result.add(lib);
			}
		}
		return result;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus("Internal Error", exception));
	}
}
