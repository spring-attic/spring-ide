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
package org.springframework.ide.eclipse.wizard.template.infrastructure.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class FileUtil {
	private static String[] BINARY_EXTENSIONS = new String[] { "jar", "gif", "jpg", "jpeg", ".class", "png" };

	@Deprecated
	public static void copy(File source, File target) throws IOException {
		// TODO: copy in FileUtil in commons, this one should be removed.
		FileInputStream sourceOutStream = new FileInputStream(source);
		FileOutputStream targetOutStream = new FileOutputStream(target);
		FileChannel sourceChannel = sourceOutStream.getChannel();
		FileChannel targetChannel = targetOutStream.getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
		sourceChannel.close();
		targetChannel.close();
		sourceOutStream.close();
		targetOutStream.close();
	}

	@Deprecated
	public static boolean isBinaryFile(File file) {
		// TODO: copy in FileUtil in commons, this one should be removed.
		String extension = FileUtil.getExtension(file);
		if (extension != null) {
			for (String binaryExtension : BINARY_EXTENSIONS) {
				if (binaryExtension.equals(extension)) {
					return true;
				}
			}
		}
		return false;
	}

	@Deprecated
	public static String getExtension(File file) {
		// TODO: copy in FileUtil in commons, this one should be removed.
		String fileName = file.getName();
		int extensionIndex = fileName.lastIndexOf('.');
		if (extensionIndex == -1) {
			return null;
		}
		return fileName.substring(extensionIndex + 1);
	}
}
