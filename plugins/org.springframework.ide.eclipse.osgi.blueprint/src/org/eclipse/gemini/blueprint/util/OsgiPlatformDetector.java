/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Utility class that detects the running platform. Useful when certain quirks or tweaks have to made for a specific
 * implementations.
 * 
 * Currently we can detect Equinox, Knopflerfish and Felix platforms.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public abstract class OsgiPlatformDetector {

	private static final String[] EQUINOX_LABELS = new String[] { "Eclipse", "eclipse", "Equinox", "equinox", };

	private static final String[] KF_LABELS = new String[] { "Knopflerfish", "knopflerfish" };

	private static final String[] FELIX_LABELS = new String[] { "Apache Software Foundation", "Felix", "felix" };

	private static final boolean isR41, isR42;

	static {
		boolean methodAvailable = false;
		ClassLoader loader = Bundle.class.getClassLoader();
		try {
			methodAvailable = (Bundle.class.getMethod("start", new Class[] { int.class }) != null);
		} catch (Exception ex) {
		}

		isR41 = methodAvailable;
		isR42 = ClassUtils.isPresent("org.osgi.framework.BundleReference", loader);
	}

	/**
	 * Returns true if the given bundle context belongs to the Equinox platform.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @return true if the context indicates Equinox platform, false otherwise
	 */
	public static boolean isEquinox(BundleContext bundleContext) {
		return determinePlatform(bundleContext, EQUINOX_LABELS);
	}

	/**
	 * Returns true if the given bundle context belongs to the Knopflerfish platform.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @return true if the context indicates Knopflerfish platform, false otherwise
	 */
	public static boolean isKnopflerfish(BundleContext bundleContext) {
		return determinePlatform(bundleContext, KF_LABELS);
	}

	/**
	 * Returns true if the given bundle context belongs to the Felix platform.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @return true if the context indicates Felix platform, false otherwise
	 */
	public static boolean isFelix(BundleContext bundleContext) {
		return determinePlatform(bundleContext, FELIX_LABELS);
	}

	private static boolean determinePlatform(BundleContext context, String[] labels) {
		Assert.notNull(context);
		Assert.notNull(labels);

		String vendorProperty = context.getProperty(Constants.FRAMEWORK_VENDOR);
		if (vendorProperty == null) {
			return false; // might be running outside of container
		} else {
			// code defensively here to allow for variation in vendor name over
			// time
			if (containsAnyOf(vendorProperty, labels)) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsAnyOf(String source, String[] searchTerms) {
		for (int i = 0; i < searchTerms.length; i++) {
			if (source.indexOf(searchTerms[i]) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the OSGi platform version (using the manifest entries from the system bundle). The version can be empty.
	 * 
	 * @param bundleContext bundle context to inspect
	 * @return not-null system bundle version
	 */
	public static String getVersion(BundleContext bundleContext) {
		if (bundleContext == null)
			return "";

		// get system bundle
		Bundle sysBundle = bundleContext.getBundle(0);
		// force string conversion instead of casting just to be safe
		return "" + sysBundle.getHeaders().get(Constants.BUNDLE_VERSION);
	}

	/**
	 * Determines if the current running platform implements OSGi Release 4.1 API or not.
	 * 
	 * @return if the running platform implements OSGi 4.1 API
	 */
	public static boolean isR41() {
		return isR41;
	}

	/**
	 * Determines if the current running platform implements OSGi Release 4.2 API or not.
	 * 
	 * @return if the running platform implements OSGi 4.2 API
	 */
	public static boolean isR42() {
		return isR42;
	}
}