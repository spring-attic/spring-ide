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
package org.eclipse.swt.browser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

/**
 * THis class is loaded by SWT just prior to instantiating the first browser
 * widget. This is the documented mechanism to do things like set some
 * system properties that are needed to make the browser widget work
 * properly.
 * 
 * See here for some info on the mechanism:
 *   http://www.eclipse.org/swt/faq.php#browserspecifydefault
 */
public class BrowserInitializer {
	
	private static final String XUL_RUNNER_BUNDLE = "org.springframework.ide.eclipse.xulrunner";
	
	/**
	 * Plugin ID used for error log purposes. Technically this code belong to "org.eclipse.swt" because
	 * its in a plugin fragment. But we log errors in some STS plugin as the errors we are producing
	 * here are not Eclipse errors and shouldn't look like they are. 
	 */
	private static final String PLUGIN_ID = "org.springframework.ide.eclipse.gettingstarted";
	private static ILog log;
	
	/**
	 * Set system property but only if its not already set. This allows people to
	 * still futz with the STS or eclipse.ini file in case we are getting it wrong
	 * for their system.
	 */
	private static void maybeSet(String prop, String val) {
		String v = System.getProperty(prop);
		if (v==null) {
			System.setProperty(prop, val);
		}
	}
	
	static {
		String path = getXULRunnerPath();
		if (path!=null) {
			//Even if user has webkit libs... don't use... they seem often to cause crashes.
			maybeSet("org.eclipse.swt.browser.DefaultType", "mozilla");
			maybeSet("org.eclipse.swt.browser.XULRunnerPath", path);
			
			//MacOS and windows browser picks up proxy info from the OS.
			//On Linux it needs to be done by setting two system properties
			//See: http://www.eclipse.org/swt/faq.php#browserproxy
			//Prop names: 'network.proxy_host' and 'network.proxy_port'.
			//Setting these will make browser use them for all http, https and ftp
			//connections. So its not possible to set them to different values.
			// (Note my xulrunner did seem to pickup on http proxy from OS without the
			//proxy. But not so for https).

			
			maybeSet("org.eclipse.swt.browser.XULRunnerPath", path);
			
		}

		String os = Platform.getOS();

		//On linux... a special system properties must be set to make the browser
		//aware of proxy. Unfortunately it is not possible to set different proxies
		//for http and https. See
		//   http://www.eclipse.org/swt/faq.php#browserproxy
		if ("linux".equals(os)) {
		
			//Note... we are assuming here that something is setting the traditional
			// Java system properties for proxies. Generally this should be the case
			// because eclipse sets them automatically if proxy is configured.
			// It seems this happens quite early on during application startup.
			
			Properties props = System.getProperties();
			for (Object _key : props.keySet()) {
				String key = (String) _key;
				if (key.toLowerCase().contains("prox")) {
					System.out.println(key+"="+props.getProperty(key));
				}
			}
			
			String proxyHost = getSameProp("http.proxyHost", "https.proxyHost", "ftp.proxyHost");
			String proxyPort = getSameProp("http.proxyPort", "https.proxyPort", "ftp.proxyPort");
			if (proxyHost!=null) {
				maybeSet("network.proxy_host", proxyHost);
				
			}
			if (proxyPort!=null) {
				maybeSet("network.proxy_port", proxyPort);
			}
					
		}
	}
	
	private static String getXULRunnerPath() {
		String osArch = Platform.getOS()+"-"+Platform.getOSArch();
		Bundle bundle = Platform.getBundle(XUL_RUNNER_BUNDLE);
		if (bundle!=null) {
			URL entry = bundle.getEntry(osArch);
			try {
				if (entry!=null) {
					File file = new File(FileLocator.toFileURL(entry).toURI());
					if (file.exists()) {
						return file.getAbsolutePath();
					}
				}
			} catch (URISyntaxException e) {
				log(e);
				//Shouldn't be possible!
			} catch (IOException e) {
				//Probaly thrown by new File(uri) call. It means the uri isn't pointing to a file.
				// that probably means the bundle isn't 'exploded'.
				log(new Error("Bundle '"+XUL_RUNNER_BUNDLE+"' not exploded?", e));
			}
		}
		return null;
	}

	/**
	 * Fetch one of the listed properties, assuming they are all set to the same value (because the
	 * browser in Linux doesn't support setting different proxies by protocol...)
	 * <p>
	 * If not all of them are the same a warning will be logged and the first non-null value will
	 * be returned. (It is probably better to set a proxy and make some stuff work than not set
	 * a proxy at all).
	 */
	private static String getSameProp(String... props) {
		String chosenProp = null;
		String value = null;
		for (int i = 0; i < props.length; i++) {
			String nextValue = System.getProperty(props[i]);
			if (chosenProp!=null && !equal(value, nextValue)) {
				warn("System properties have different values: \n"+chosenProp+ " = "+value+"\n" +
						props[i] + " = "+nextValue + "\n" +
						"Browser widget doesn't support that so will use "+props[i]+" = "+value+" instead.");
			}
			//Only allowed to pick one non-null value once:
			if (chosenProp==null && nextValue!=null) {
				chosenProp = props[i];
				value = nextValue;
			}
		}
		return value;
	}

	private static boolean equal(String value, String nextValue) {
		if (value==null){
			return value == nextValue;
		} else {
			return value.equals(nextValue);
		}
	}

	private static void log(Throwable e) {
		log(createErrorStatus(e));
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
	
	private static void log(IStatus status) {
		if (log==null) {
			log = Platform.getLog(Platform.getBundle(PLUGIN_ID));
		}
		log.log(status);
	}
	
	private static void warn(String message) {
		log(new Status(IStatus.WARNING, PLUGIN_ID, message));
	}


}
