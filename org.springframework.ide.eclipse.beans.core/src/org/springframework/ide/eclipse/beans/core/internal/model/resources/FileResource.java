/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.core.io.AbstractResource;

/**
 * Resource implementation for Eclipse file handles.
 * Obviously supports resolution as File, and also as URL.
 * @see org.eclipse.core.resources.IFile
 */
public class FileResource extends AbstractResource {

	private File file;

	/**
	 * Create a new FileResource.
	 * @param file a File handle
	 */
	public FileResource(IFile file) {
		if (file != null) {
			this.file = file.getLocation().toFile();
		}
	}

	/**
	 * Create a new FileResource.
	 * @param path a file path (relative to Eclipse workspace)
	 */
	public FileResource(String path) {
		if (path.charAt(0) != '/') {
			throw new IllegalArgumentException("Path has to be relative to " +
											   "Eclipse workspace");
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(path);
		if (member != null) {
			file = member.getFullPath().toFile();
		}
	}

	public boolean exists() {
		return (file != null && file.exists());
	}

	public InputStream getInputStream() throws IOException {
		if (file == null) {
			throw new FileNotFoundException("File not found");
		}
		return new FileInputStream(file);
	}

	public URL getURL() throws IOException {
		if (file == null) {
			throw new FileNotFoundException("File not found");
		}
		return new URL(URL_PROTOCOL_FILE + ":" + file.getAbsolutePath());
	}

	public File getFile() {
		return file;
	}

	public String getDescription() {
		return "file [" + (file != null ? file.getAbsolutePath() : "") + "]";
	}

	public boolean equals(Object obj) {
		return (obj == this || (obj instanceof FileResource &&
									 (((FileResource) obj).file).equals(file)));
	}

	public int hashCode() {
		return (file != null ? file.hashCode() : 0);
	}
}
