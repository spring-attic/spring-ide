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

package org.springframework.ide.eclipse.beans.core.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Resource implementation for Eclipse file handles.
 * Obviously supports resolution as File, and also as URL.
 * @see org.eclipse.core.resources.IFile
 */
public class FileResource extends AbstractResource {

	private final File file;

	/**
	 * Create a new FileResource.
	 * @param file a File handle
	 */
	public FileResource(IFile file) {
		this.file = file.getLocation().toFile();
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
		this.file = root.findMember(path).getFullPath().toFile();
	}

	public boolean exists() {
		return this.file.exists();
	}

	public InputStream getInputStream() throws IOException {
			return new FileInputStream(this.file);
	}

	public URL getURL() throws IOException {
		return new URL(URL_PROTOCOL_FILE + ":" + this.file.getAbsolutePath());
	}

	public File getFile() {
		return file;
	}

	public Resource createRelative(String relativePath) {
		File parent = this.file.getParentFile();
		if (parent != null) {
			return new FileSystemResource(new File(parent, relativePath));
		}
		else {
			return new FileSystemResource(relativePath);
		}
	}

	public String getFilename() {
		return this.file.getName();
	}

	public String getDescription() {
		return "file [" + this.file.getAbsolutePath() + "]";
	}

	public boolean equals(Object obj) {
		return (obj == this || (obj instanceof FileResource &&
								  this.file.equals(((FileResource) obj).file)));
	}

	public int hashCode() {
		return this.file.hashCode();
	}
}
