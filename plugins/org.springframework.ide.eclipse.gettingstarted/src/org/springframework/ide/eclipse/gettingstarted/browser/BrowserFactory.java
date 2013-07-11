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
package org.springframework.ide.eclipse.gettingstarted.browser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;

//TODO: This browserfactory works under the assumption that all browser are
//  created via this factory. This can however not be guaranteed if browsers
//  are created by non STS code. 
//
// Proper way to hook into browser initialization is documented here:
// http://www.eclipse.org/swt/faq.php
// in question "Q: How do I specify the default type of native renderer that is used by the Browser?"

/**
 * Try to hide platform related mess from client code that needs to create an SWT 
 * Browser widget. What this factory does is try and properly configure
 * xulrunner native libraries we can ship as an eclipse plugin.
 */
@SuppressWarnings("restriction")
public class BrowserFactory {

	private static final String XUL_RUNNER_BUNDLE = "org.springframework.ide.eclipse.xulrunner";
	//TODO: At the moment this XUL_RUNNER_BUNDLE does not exist (yet). Linux user if they experience
	// issues with the browser component will have to 
	// futz around downloading xulrunner 10.0.2 manually. Unzip it somewhere and
	// add system properties to STS.ini like this example:
	//    -Dorg.eclipse.swt.browser.XULRunnerPath=/home/kdvolder/Applications/xulrunner-10.0.2
	//    -Dorg.eclipse.swt.browser.DefaultType=mozilla
	
	private static class BrowserFactoryImplementation {
		STSBrowserViewer create(Composite parent) {
			STSBrowserViewer viewer = new STSBrowserViewer(parent, BrowserViewer.BUTTON_BAR|BrowserViewer.LOCATION_BAR);
			return viewer;
//			return new Browser(parent, SWT.NONE);
		}
	}
	
	/**
	 * Set system property but only if its not already set. This allows people to
	 * still futz with the STs or eclipse.ini file in case we got it wrong.
	 */
	private static void maybeSet(String prop, String val) {
		String v = System.getProperty(prop);
		if (v==null) {
			System.setProperty(prop, val);
		}
	}
	
	private static class LinuxBrowserFactoryImplementation extends BrowserFactoryImplementation {

		//In Linux there are issues depending on what native libraries people have installed.
		//We will try to avoid these by using our own bundled version of xulrunner.
		
		//Most of the info around how to do that is available from here
		//www.eclipse.org/swt/faq.php
		
		LinuxBrowserFactoryImplementation() {
// All this code is now in BrowserInitializer wher it should be to ensure it runs before
// any browser instantation even if it is done directly by Eclipse rather than STS code.
			
//			//To use xulrunner must use "mozilla" rather than "webkit".
//			String path = getXULRunnerPath();
//			if (path!=null) {
//				//Even if user has webkit libs... don't use... they seem often to cause crashes.
//				maybeSet("org.eclipse.swt.browser.DefaultType", "mozilla");
//				maybeSet("org.eclipse.swt.browser.XULRunnerPath", path);
//				
//				//MacOS and windows browser picks up proxy info from the OS.
//				//On Linux it needs to be done by setting two system properties
//				//See: http://www.eclipse.org/swt/faq.php#browserproxy
//				//Prop names: 'network.proxy_host' and 'network.proxy_port'.
//				//Setting these will make browser use them for all http, https and ftp
//				//connections. So its not possible to set them to different values.
//				// (Note my xulrunner did seem to pickup on http proxy from OS without the
//				//proxy. But not so for https).
//
//				
//				maybeSet("org.eclipse.swt.browser.XULRunnerPath", path);
//				
//			}
//
//			Properties props = System.getProperties();
//			for (Object _key : props.keySet()) {
//				String key = (String) _key;
//				if (key.toLowerCase().contains("prox")) {
//					System.out.println(key+"="+props.getProperty(key));
//				}
//			}
//			
		}

//		private String getXULRunnerPath() {
//			String osArch = Platform.getOS()+"-"+Platform.getOSArch();
//			Bundle bundle = Platform.getBundle(XUL_RUNNER_BUNDLE);
//			if (bundle!=null) {
//				URL entry = bundle.getEntry(osArch);
//				try {
//					if (entry!=null) {
//						File file = new File(FileLocator.toFileURL(entry).toURI());
//						if (file.exists()) {
//							return file.getAbsolutePath();
//						}
//					}
//				} catch (URISyntaxException e) {
//					//Shouldn't be possible!
//					GettingStartedActivator.log(e);
//				} catch (IOException e) {
//					//Probaly thrown by new File(uri) call. It means the uri isn't pointing to a file.
//					// that probably means the bundle isn't 'exploded'.
//					GettingStartedActivator.log(new Error("Bundle '"+XUL_RUNNER_BUNDLE+"' not exploded?", e));
//				}
//			}
//			return null;
//		}

//Actually, no need to override this. Code is same as superclass. The behavior is instead
// controlled entirely by system props.
//		Browser create(Composite body) {
//			return new Browser(body, SWT.NONE);
//		}
	}

	private static BrowserFactoryImplementation implementation;

	public static STSBrowserViewer create(Composite body) {
		STSBrowserViewer viewer = implementation().create(body);
		customizeBrowser(viewer.getBrowser());
		return viewer;
	}

	/**
	 * Add STS specific customizations to the browser to support integration of springsource.org
	 * webpages when shown inside STS browser / Dashboard.
	 */
	public static void customizeBrowser(Browser browser) {
		//Not platform-specific browser customizations added here.
		new BrowserCusomizer(browser);
	}

	private static BrowserFactoryImplementation implementation() {
		if (implementation == null) {
			String os = Platform.getOS();
			if ("linux".equals(os)) {
				implementation = new LinuxBrowserFactoryImplementation();
			} else {
				implementation = new BrowserFactoryImplementation();
			}
		}
		return implementation;
	}

}
