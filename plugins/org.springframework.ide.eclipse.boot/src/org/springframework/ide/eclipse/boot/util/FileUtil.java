/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {

	public static boolean isJarFile(File jarFile) {
		try {
			return jarFile!=null && jarFile.isFile() && jarFile.toString().toLowerCase().endsWith(".jar");
		} catch (Throwable e) {
			org.springsource.ide.eclipse.commons.livexp.util.Log.log(e);
			return false;
		}
	}

	/**
	 * Creates a temporary folder that gets deleted on VM shutdown. <b/> WARNING:
	 * any files created in this temporary folder will also be deleted on shutdown.
	 *
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static File createTempFolder(String name) throws IOException {
		File tempFolder = File.createTempFile(name, null);
		tempFolder.delete();
		tempFolder.mkdirs();
		if (!tempFolder.exists()) {
			throw new IOException("Failed to create temporary jar file when packaging application for deployment: "
							+ tempFolder.getAbsolutePath());
		}
		deleteOnShutdown(tempFolder);
		return tempFolder;
	}

	private static void deleteOnShutdown(File tempFolder) {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				deleteResource(tempFolder);
			}));
		} catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
			org.springsource.ide.eclipse.commons.livexp.util.Log.log(e);
		}
	}

	private static void deleteResource(File file) {
		if (file == null || !file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File content : files) {
					deleteResource(content);
				}
			}
		}
		file.delete();
	}
}
