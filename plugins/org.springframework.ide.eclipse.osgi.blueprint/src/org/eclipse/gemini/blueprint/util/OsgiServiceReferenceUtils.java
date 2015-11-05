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

import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;
import org.eclipse.gemini.blueprint.util.internal.ServiceReferenceBasedMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Utility class for retrieving OSGi service references. This class offers a unified filter-based access for OSGi
 * services as well as translation of checked exceptions {@link InvalidSyntaxException} into unchecked ones.
 * 
 * <p/>
 * 
 * This classes uses {@link OsgiFilterUtils} underneath to allow multiple class names to be used for service reference
 * lookup.
 * 
 * @see OsgiFilterUtils
 * @author Costin Leau
 * 
 */
public abstract class OsgiServiceReferenceUtils {

	private static final Log log = LogFactory.getLog(OsgiServiceReferenceUtils.class);

	/**
	 * Returns a reference to the <em>best matching</em> service for the given class names.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param classes array of fully qualified class names
	 * @return reference to the <em>best matching</em> service
	 */
	public static ServiceReference getServiceReference(BundleContext bundleContext, String[] classes) {
		return getServiceReference(bundleContext, classes, null);
	}

	/**
	 * Returns a reference to the <em>best matching</em> service for the given class and OSGi filter.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param clazz fully qualified class name (can be <code>null</code>)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return reference to the <em>best matching</em> service
	 */
	public static ServiceReference getServiceReference(BundleContext bundleContext, String clazz, String filter) {
		ServiceReference[] refs = getServiceReferences(bundleContext, clazz, filter);

		// pick the best service
		return getServiceReference(refs);
	}

	public static ServiceReference getServiceReference(ServiceReference... references) {
		if (ObjectUtils.isEmpty(references)) {
			return null;
		}

		ServiceReference winningReference = references[0];

		if (references.length > 1) {
			long winningId = getServiceId(winningReference);
			int winningRanking = getServiceRanking(winningReference);

			// start iterating in order to find the best match
			for (int i = 1; i < references.length; i++) {
				ServiceReference reference = references[i];
				int serviceRanking = getServiceRanking(reference);
				long serviceId = getServiceId(reference);

				if ((serviceRanking > winningRanking) || (serviceRanking == winningRanking && winningId > serviceId)) {
					winningReference = reference;
					winningId = serviceId;
					winningRanking = serviceRanking;
				}
			}
		}
		return winningReference;
	}

	/**
	 * Returns a reference to the <em>best matching</em> service for the given classes and OSGi filter.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param classes array of fully qualified class names
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return reference to the <em>best matching</em> service
	 */
	public static ServiceReference getServiceReference(BundleContext bundleContext, String[] classes, String filter) {
		// use #getServiceReference(BundleContext, String, String) method to
		// speed the service lookup process by
		// giving one class as a hint to the OSGi implementation

		String clazz = (ObjectUtils.isEmpty(classes) ? null : classes[0]);

		return getServiceReference(bundleContext, clazz, OsgiFilterUtils.unifyFilter(classes, filter));
	}

	/**
	 * Returns a reference to the <em>best matching</em> service for the given OSGi filter.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return reference to the <em>best matching</em> service
	 */
	public static ServiceReference getServiceReference(BundleContext bundleContext, String filter) {
		return getServiceReference(bundleContext, (String) null, filter);
	}

	/**
	 * Returns references to <em>all</em> services matching the given class names.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param classes array of fully qualified class names
	 * @return non-<code>null</code> array of references to matching services
	 */
	public static ServiceReference[] getServiceReferences(BundleContext bundleContext, String[] classes) {
		return getServiceReferences(bundleContext, classes, null);
	}

	/**
	 * Returns references to <em>all</em> services matching the given class name and OSGi filter.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param clazz fully qualified class name (can be <code>null</code>)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return non-<code>null</code> array of references to matching services
	 */
	public static ServiceReference[] getServiceReferences(BundleContext bundleContext, String clazz, String filter) {
		Assert.notNull(bundleContext, "bundleContext should be not null");

		try {
			ServiceReference[] refs = bundleContext.getServiceReferences(clazz, filter);
			return (refs == null ? new ServiceReference[0] : refs);
		} catch (InvalidSyntaxException ise) {
			throw (RuntimeException) new IllegalArgumentException("invalid filter: " + ise.getFilter()).initCause(ise);
		}
	}

	/**
	 * Returns references to <em>all</em> services matching the given class names and OSGi filter.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param classes array of fully qualified class names
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return non-<code>null</code> array of references to matching services
	 */
	public static ServiceReference[] getServiceReferences(BundleContext bundleContext, String[] classes, String filter) {
		// use #getServiceReferences(BundleContext, String, String) method to
		// speed the service lookup process by
		// giving one class as a hint to the OSGi implementation
		// additionally this allows type filtering

		String clazz = (ObjectUtils.isEmpty(classes) ? null : classes[0]);
		return getServiceReferences(bundleContext, clazz, OsgiFilterUtils.unifyFilter(classes, filter));
	}

	/**
	 * Returns references to <em>all</em> services matching the OSGi filter.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return non-<code>null</code> array of references to matching services
	 */
	public static ServiceReference[] getServiceReferences(BundleContext bundleContext, String filter) {
		return getServiceReferences(bundleContext, (String) null, filter);
	}

	/**
	 * Returns the service id ({@link Constants#SERVICE_ID}) of the given service reference.
	 * 
	 * @param reference OSGi service reference
	 * @return service id
	 */
	public static long getServiceId(ServiceReference reference) {
		Assert.notNull(reference);
		return ((Long) reference.getProperty(Constants.SERVICE_ID)).longValue();
	}

	/**
	 * Returns the service ranking ({@link Constants#SERVICE_RANKING}) of the given service reference.
	 * 
	 * @param reference OSGi service reference
	 * @return service ranking
	 */
	public static int getServiceRanking(ServiceReference reference) {
		Assert.notNull(reference);

		Object ranking = reference.getProperty(Constants.SERVICE_RANKING);
		// if the property is not supplied or of incorrect type, use a
		// default
		return ((ranking != null && ranking instanceof Integer) ? ((Integer) ranking).intValue() : 0);
	}

	/**
	 * Returns the advertised class names ({@link Constants#OBJECTCLASS}) of the given service reference.
	 * 
	 * @param reference OSGi service reference
	 * @return service advertised class names
	 */
	public static String[] getServiceObjectClasses(ServiceReference reference) {
		Assert.notNull(reference);
		return (String[]) reference.getProperty(Constants.OBJECTCLASS);
	}

	/**
	 * Returns a {@link Map} containing the properties available for the given service reference. This method takes a
	 * snapshot of the properties; future changes to the service properties will not be reflected in the returned
	 * dictionary.
	 * 
	 * <p/> Note that the returned type implements the {@link java.util.Map} interface also.
	 * 
	 * @param reference OSGi service reference
	 * @return a <code>Dictionary</code> containing the service reference properties taken as a snapshot
	 */
	public static Dictionary getServicePropertiesSnapshot(ServiceReference reference) {
		return new MapBasedDictionary(getServicePropertiesSnapshotAsMap(reference));
	}

	/**
	 * Returns a {@link Map} containing the properties available for the given service reference. This method takes a
	 * snapshot of the properties; future changes to the service properties will not be reflected in the returned
	 * dictionary.
	 * 
	 * @param reference OSGi service reference
	 * @return a <code>Map</code> containing the service reference properties taken as a snapshot
	 */
	public static Map getServicePropertiesSnapshotAsMap(ServiceReference reference) {
		Assert.notNull(reference);
		String[] keys = reference.getPropertyKeys();

		Map map = new LinkedHashMap(keys.length);

		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], reference.getProperty(keys[i]));
		}

		// mark it as read-only
		map = Collections.unmodifiableMap(map);
		return map;
	}

	/**
	 * Returns a {@link Dictionary} containing the properties available for the given service reference. The returned
	 * object will reflect any updates made to to the <code>ServiceReference</code> through the owning
	 * <code>ServiceRegistration</code>.
	 * 
	 * 
	 * <p/> Note that the returned type implements the {@link java.util.Map} interface also.
	 * 
	 * @param reference OSGi service reference
	 * @return a <code>Dictionary</code> containing the latest service reference properties
	 */
	public static Dictionary getServiceProperties(ServiceReference reference) {
		return new MapBasedDictionary(getServicePropertiesAsMap(reference));
	}

	/**
	 * Returns a {@link Map} containing the properties available for the given service reference. The returned object
	 * will reflect any updates made to to the <code>ServiceReference</code> through the owning
	 * <code>ServiceRegistration</code>. Consider using {@link #getServiceProperties(ServiceReference)} which returns an
	 * object that extends {@link Dictionary} as well as implements the {@link Map} interface.
	 * 
	 * @param reference OSGi service reference
	 * @return a <code>Map</code> containing the latest service reference properties
	 * @see #getServiceProperties(ServiceReference)
	 */
	public static Map getServicePropertiesAsMap(ServiceReference reference) {
		Assert.notNull(reference);
		return new ServiceReferenceBasedMap(reference);

	}

	/**
	 * Checks if the given filter matches at least one OSGi service or not.
	 * 
	 * @param bundleContext OSGi bundle context
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return true if the filter matches at least one OSGi service, false otherwise
	 */
	public static boolean isServicePresent(BundleContext bundleContext, String filter) {
		return !ObjectUtils.isEmpty(getServiceReferences(bundleContext, filter));
	}
}