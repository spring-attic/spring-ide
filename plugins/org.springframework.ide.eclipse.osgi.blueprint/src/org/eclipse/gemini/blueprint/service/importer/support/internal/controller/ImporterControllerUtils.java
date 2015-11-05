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

package org.eclipse.gemini.blueprint.service.importer.support.internal.controller;

import java.lang.reflect.Field;

/**
 * Importer-only delegate (it would be nice to have generics).
 * 
 * @author Costin Leau
 * 
 */
public abstract class ImporterControllerUtils {

	private static final String FIELD_NAME = "controller";
	private static final Field singleProxyField, collectionProxyField;
	private static final Class<?> singleImporter;

	static {
		Class<?> clazz = null;
		String singleImporterName = "org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceProxyFactoryBean";
		String multiImporterName =
				"org.eclipse.gemini.blueprint.service.importer.support.OsgiServiceCollectionProxyFactoryBean";
		try {
			ClassLoader cl = ImporterControllerUtils.class.getClassLoader();
			clazz = cl.loadClass(singleImporterName);
			singleImporter = clazz;
			singleProxyField = clazz.getDeclaredField(FIELD_NAME);
			singleProxyField.setAccessible(true);

			clazz = cl.loadClass(multiImporterName);
			collectionProxyField = clazz.getDeclaredField(FIELD_NAME);
			collectionProxyField.setAccessible(true);
		} catch (Exception ex) {
			throw (RuntimeException) new IllegalStateException("Cannot read field [" + FIELD_NAME + "] on class ["
					+ clazz + "]").initCause(ex);
		}
	}

	public static ImporterInternalActions getControllerFor(Object importer) {
		Field field = (singleImporter == importer.getClass() ? singleProxyField : collectionProxyField);
		try {
			return (ImporterInternalActions) field.get(importer);
		} catch (IllegalAccessException iae) {
			throw (RuntimeException) new IllegalArgumentException("Cannot access field [" + FIELD_NAME
					+ "] on object [" + importer + "]").initCause(iae);
		}
	}
}
