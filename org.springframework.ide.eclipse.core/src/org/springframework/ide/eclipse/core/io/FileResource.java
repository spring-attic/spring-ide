/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core.io;

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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

/**
 * {@link Resource} implementation for Eclipse {@link IFile file} handles.
 * 
 * @author Torsten Juergeleit
 */
public class FileResource extends AbstractResource implements IAdaptable {

	private IFile file;

	/**
	 * Create a new FileResource.
	 * @param file  a file
	 */
	public FileResource(IFile file) {
		this.file = file;
	}

	/**
	 * Create a new FileResource.
	 * @param path a file path (relative to Eclipse workspace)
	 */
	public FileResource(String path) {
		if (path.charAt(0) != '/') {
			throw new IllegalArgumentException("Path '" + path
					+ "' has to be relative to Eclipse workspace");
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(path);
		if (member != null && member instanceof IFile) {
			file = (IFile) member;
		}
	}

	@Override
	public boolean exists() {
		return (file != null && file.exists());
	}

	public InputStream getInputStream() throws IOException {
		if (file == null) {
			throw new FileNotFoundException("File not found");
		}
		return new FileInputStream(getFile());
	}

	@Override
	public URL getURL() throws IOException {
		if (file == null) {
			throw new FileNotFoundException("File not found");
		}
		return new URL(ResourceUtils.URL_PROTOCOL_FILE + ":"
				+ file.getRawLocation());
	}

	@Override
	public File getFile() throws IOException {
		if (file == null) {
			throw new FileNotFoundException("File not found");
		}
		return file.getLocation().toFile();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		if (file == null) {
			throw new IllegalStateException("File not found");
		}
		IFile relativeFile = file.getParent().getFile(
				new Path(relativePath));
		if (relativeFile != null) {
			return new FileResource(relativeFile);
		}
		throw new FileNotFoundException("Cannot create relative resource '"
				+ relativePath + "' for " + getDescription());
	}

	@Override
	public String getFilename() {
		if (file == null) {
			throw new IllegalStateException("File not found");
		}
		return file.getName();
	}

	@Override
	public String getDescription() {
		return "file [" + (file != null ? file.getRawLocation() : "") + "]";
	}

	/**
	 * Adapts to {@link IResource} or {@link IFile}.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IResource.class) || adapter.equals(IFile.class)) {
			return file;
		}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FileResource)) {
			return false;
		}
		FileResource that = (FileResource) other;
		return ObjectUtils.nullSafeEquals(this.file, that.file);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(file);
	}
}
