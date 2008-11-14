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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

/**
 * {@link Resource} implementation for Eclipse {@link IFile file} handles.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
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
		else if (file instanceof ExternalFile) {
			try {
				return file.getContents();
			}
			catch (CoreException e) {
				throw new IOException(e.getMessage());
			}
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
		if (file instanceof ExternalFile) {
			return ((ExternalFile) file).getFilename();
		}
		return file.getProjectRelativePath().toString();
	}

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
	
	public IFile getRawFile() {
		return this.file;
	}
}
