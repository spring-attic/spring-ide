/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

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
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;

/**
 * Stores and loads the {@link IBeanMetadata}s from the persisted file.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataPersistence {

	private static final String STATE_FILE_NAME = ".state";

	private static final String METADATA_FOLDER_NAME = "/metadata/";

	public static void storeMetaData(Map<String, BeanMetadataHolder> metaData) {
		ObjectOutputStream out = null;

		try {
			File folder = BeansCorePlugin.getDefault().getStateLocation().append(
					METADATA_FOLDER_NAME).toFile();
			File file = BeansCorePlugin.getDefault().getStateLocation().append(
					METADATA_FOLDER_NAME + STATE_FILE_NAME).toFile();
			if (!file.exists()) {
				folder.mkdirs();
				file.createNewFile();
			}
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(metaData);
		}
		catch (IOException e) {
			BeansCorePlugin.log(new Status(IStatus.ERROR, BeansCorePlugin.PLUGIN_ID,
					"Exception saving meta data model", e));
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
	public static Map<String, BeanMetadataHolder> loadMetaData() {
		ObjectInputStream in = null;

		try {
			File f = BeansCorePlugin.getDefault().getStateLocation().append(
					METADATA_FOLDER_NAME + STATE_FILE_NAME).toFile();
			if (f.exists()) {
				in = new ObjectInputStream(new FileInputStream(f));
				return (Map) in.readObject();
			}
		}
		catch (Exception e) {
			BeansCorePlugin.log(new Status(IStatus.ERROR, BeansCorePlugin.PLUGIN_ID,
					"Exception restoring meta data model", e));
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (IOException e) {
			}
		}
		// create new empty model
		return new ConcurrentHashMap<String, BeanMetadataHolder>();
	}

}
