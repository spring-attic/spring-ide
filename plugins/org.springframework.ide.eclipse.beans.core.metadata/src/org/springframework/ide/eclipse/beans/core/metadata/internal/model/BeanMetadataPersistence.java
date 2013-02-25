/*******************************************************************************
 * Copyright (c) 2010, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.beans.core.metadata.BeansMetadataPlugin;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;

/**
 * Stores and loads the {@link IBeanMetadata}s from the persisted file.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.5
 */
public class BeanMetadataPersistence {

	private static final String STATE_FILE_NAME = ".state";

	private static final String METADATA_FOLDER_NAME = "/metadata/";

	private static final String BEANPROPERTIES_FOLDER_NAME = "/properties/";

	public static void storeMetadata(Map<String, BeanMetadataHolder> metaData) {
		File file = BeansMetadataPlugin.getDefault().getStateLocation().append(METADATA_FOLDER_NAME + STATE_FILE_NAME)
				.toFile();
		store(metaData, file);
	}

	public static void storeProperties(Map<String, BeanPropertyDataHolder> properties) {
		File file = BeansMetadataPlugin.getDefault().getStateLocation()
				.append(BEANPROPERTIES_FOLDER_NAME + STATE_FILE_NAME).toFile();
		store(properties, file);
	}

	private static void store(Object obj, File file) {
		ObjectOutputStream out = null;

		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(obj);
		}
		catch (IOException e) {
			BeansMetadataPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, BeansMetadataPlugin.PLUGIN_ID,
					"Exception saving meta data model for class " + obj.getClass(), e));
		}
		finally {
			try {
				if (out != null)
					out.close();
			}
			catch (IOException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, BeanMetadataHolder> loadMetadata() {
		File f = BeansMetadataPlugin.getDefault().getStateLocation().append(METADATA_FOLDER_NAME + STATE_FILE_NAME)
				.toFile();
		Map<String, BeanMetadataHolder> metaData = load(f, Map.class);
		if (metaData != null && metaData.size() > 0) {
			return metaData;
		}
		// create new empty model
		return new ConcurrentHashMap<String, BeanMetadataHolder>();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, BeanPropertyDataHolder> loadProperties() {
		File f = BeansMetadataPlugin.getDefault().getStateLocation().append(BEANPROPERTIES_FOLDER_NAME + STATE_FILE_NAME)
				.toFile();
		Map<String, BeanPropertyDataHolder> metaData = load(f, Map.class);
		if (metaData != null && metaData.size() > 0) {
			return metaData;
		}
		// create new empty model
		return new ConcurrentHashMap<String, BeanPropertyDataHolder>();
	}

	public static <T> T load(File file, Class<T> clazz) {
		ObjectInputStream in = null;

		try {
			if (file.exists()) {
				in = new ObjectInputStream(new FileInputStream(file));
				return (T) in.readObject();
			}
		}
		catch (Exception e) {
			BeansMetadataPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, BeansMetadataPlugin.PLUGIN_ID,
					"Exception restoring meta data model for class " + clazz, e));
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (IOException e) {
			}
		}
		return null;
	}

}
