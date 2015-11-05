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

import java.util.Collection;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility class for creating OSGi filters. This class allows filter creation and concatenation from common parameters
 * such as class names.
 * 
 * @author Costin Leau
 */
public abstract class OsgiFilterUtils {

	private static final char FILTER_BEGIN = '(';

	private static final char FILTER_END = ')';

	private static final String FILTER_AND_CONSTRAINT = "(&";

	private static final String EQUALS = "=";

	/**
	 * Adds the given class as an 'and'(&amp;) {@link Constants#OBJECTCLASS} constraint to the given filter. At least
	 * one parameter must be valid (non-<code>null</code>).
	 * 
	 * @param clazz class name (can be <code>null</code>)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return OSGi filter containing the {@link Constants#OBJECTCLASS} constraint and the given filter
	 */
	public static String unifyFilter(String clazz, String filter) {
		return unifyFilter(new String[] { clazz }, filter);
	}

	/**
	 * Adds the given class to the given filter. At least one parameter must be valid (non-<code>null</code>).
	 * 
	 * @param clazz fully qualified class name (can be <code>null</code>)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 * @see #unifyFilter(String, String)
	 */
	public static String unifyFilter(Class<?> clazz, String filter) {
		if (clazz != null)
			return unifyFilter(clazz.getName(), filter);
		return unifyFilter((String) null, filter);
	}

	/**
	 * Adds the given classes to the given filter. At least one parameter must be valid (non-<code>null</code>).
	 * 
	 * @param classes array of fully qualified class names (can be <code>null</code>/empty)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 * @see #unifyFilter(String[], String)
	 */
	public static String unifyFilter(Class<?>[] classes, String filter) {
		if (ObjectUtils.isEmpty(classes))
			return unifyFilter(new String[0], filter);

		String classNames[] = new String[classes.length];
		for (int i = 0; i < classNames.length; i++) {
			if (classes[i] != null)
				classNames[i] = classes[i].getName();
		}
		return unifyFilter(classNames, filter);
	}

	/**
	 * Adds the given classes as an 'and'(&amp;) {@link Constants#OBJECTCLASS} constraint to the given filter. At least
	 * one parameter must be valid (non-<code>null</code>).
	 * 
	 * @param classes array of fully qualified class names (can be <code>null</code>/empty)
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 */
	public static String unifyFilter(String[] classes, String filter) {
		return unifyFilter(Constants.OBJECTCLASS, classes, filter);
	}

	/**
	 * Concatenates the given strings with an 'and'(&amp;) constraint under the given key to the given filter. At least
	 * one of the items/filter parameters must be valid (non-<code>null</code>).
	 * 
	 * @param key the key under which the items are being concatenated (required)
	 * @param items an array of strings concatenated to the existing filter
	 * @param filter valid OSGi filter (can be <code>null</code>)
	 * @return an OSGi filter concatenating the given parameters
	 */
	public static String unifyFilter(String key, String[] items, String filter) {
		boolean filterHasText = StringUtils.hasText(filter);

		if (items == null)
			items = new String[0];

		// number of valid (not-null) classes
		int itemName = items.length;

		for (int i = 0; i < items.length; i++) {
			if (items[i] == null)
				itemName--;
		}

		if (itemName == 0)
			// just return the filter
			if (filterHasText)
				return filter;
			else
				throw new IllegalArgumentException("at least one parameter has to be not-null");

		Assert.hasText(key, "key is required");

		// do a simple filter check - starts with ( and ends with )
		if (filterHasText && !(filter.charAt(0) == FILTER_BEGIN && filter.charAt(filter.length() - 1) == FILTER_END)) {
			throw new IllegalArgumentException("invalid filter: " + filter);
		}

		// the item will be added in a sub-filter which does searching only
		// after the key. For classes these will look like:
		// 
		// i.e.
		// (&(objectClass=java.lang.Object)(objectClass=java.lang.Cloneable))
		//
		// this sub filter will be added with a & constraint to the given filter
		// if
		// that one exists
		// i.e. (&(&(objectClass=MegaObject)(objectClass=SuperObject))(<given
		// filter>))

		StringBuilder buffer = new StringBuilder();

		// a. big & constraint
		// (&
		if (filterHasText)
			buffer.append(FILTER_AND_CONSTRAINT);

		boolean moreThenOneClass = itemName > 1;

		// b. create key sub filter (only if we have more then one class
		// (&(&
		if (moreThenOneClass) {
			buffer.append(FILTER_AND_CONSTRAINT);
		}

		// parse the classes and add the item name under the given key
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				// (objectClass=
				buffer.append(FILTER_BEGIN);
				buffer.append(key);
				buffer.append(EQUALS);
				// <actual value>
				buffer.append(items[i]);
				// )
				buffer.append(FILTER_END);
			}
		}

		// c. close the classes sub filter
		// )
		if (moreThenOneClass) {
			buffer.append(FILTER_END);
		}

		// d. add the rest of the filter
		if (filterHasText) {
			buffer.append(filter);
			// e. close the big filter
			buffer.append(FILTER_END);
		}

		return buffer.toString();

	}

	/**
	 * Validates the given String as a OSGi filter.
	 * 
	 * @param filter OSGi filter
	 * @return true if the filter is valid, false otherwise
	 */
	public static boolean isValidFilter(String filter) {
		try {
			createFilter(filter);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * Creates an OSGi {@link Filter} from the given String. Translates the {@link InvalidSyntaxException} checked
	 * exception into an unchecked {@link IllegalArgumentException}.
	 * 
	 * @param filter OSGi filter given as a String
	 * @return OSGi filter (as <code>Filter</code>)
	 */
	public static Filter createFilter(String filter) {
		Assert.hasText(filter, "invalid filter");
		try {
			return FrameworkUtil.createFilter(filter);
		} catch (InvalidSyntaxException ise) {
			throw (RuntimeException) new IllegalArgumentException("invalid filter: " + ise.getFilter()).initCause(ise);
		}
	}

	/**
	 * Creates a filter (as String) that matches the properties (expect the service id) of service reference.
	 * 
	 * @param reference
	 * @return
	 */
	public static String getFilter(ServiceReference reference) {
		String[] propertyKeys = reference.getPropertyKeys();
		// allocate some space based on the array length
		StringBuilder sb = new StringBuilder(propertyKeys.length << 3);
		sb.append("(&");
		for (String key : propertyKeys) {
			if (!Constants.SERVICE_ID.equals(key)) {
				Object value = reference.getProperty(key);
				Class<?> cl = value.getClass();
				Iterable it;
				// array
				if (cl.isArray()) {
					Object[] array = ObjectUtils.toObjectArray(value);
					for (Object item : array) {
						sb.append("(");
						sb.append(key);
						sb.append("=");
						sb.append(item);
						sb.append(")");
					}
				}

				// collection
				else if (Collection.class.isAssignableFrom(cl)) {
					Collection<?> c = (Collection) value;
					for (Object item : c) {
						sb.append("(");
						sb.append(key);
						sb.append("=");
						sb.append(item);
						sb.append(")");
					}
				}

				// scalar/primitive
				else {
					sb.append("(");
					sb.append(key);
					sb.append("=");
					sb.append(value);
					sb.append(")");
				}
			}
		}

		sb.append(")");
		return sb.toString();
	}
}