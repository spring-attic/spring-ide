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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

/**
 * {@link Resource} abstraction for Eclipse {@link IResource} implementations.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class EclipseResource extends AbstractResource implements IAdaptable {

	private IResource resource;
	
	public EclipseResource(IResource resource) {
		this.resource = resource;
	}

	public EclipseResource(String path) {
		if (path.charAt(0) != '/') {
			throw new IllegalArgumentException("Path '" + path
					+ "' has to be relative to Eclipse workspace");
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(path);
		if (member != null && member instanceof IResource) {
			resource = (IResource) member;
		}
	}

	@Override
	public boolean exists() {
		return resource != null && resource.exists();
	}

	public InputStream getInputStream() throws IOException {
		if (resource == null || !(resource instanceof IFile)) {
			throw new FileNotFoundException("File not found");
		}
		return new FileInputStream(getFile());
	}

	@Override
	public URL getURL() throws IOException {
		if (resource == null || !(resource instanceof IFile)) {
			throw new FileNotFoundException("File not found");
		}
		return new URL(ResourceUtils.URL_PROTOCOL_FILE + ":"
				+ resource.getRawLocation());
	}

	@Override
	public File getFile() throws IOException {
		if (resource == null || !(resource instanceof IFile)) {
			throw new FileNotFoundException("File not found");
		}
		return resource.getLocation().toFile();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		if (resource == null) {
			throw new IllegalStateException("File not found");
		}
		IFile relativeFile = resource.getParent().getFile(
				new Path(relativePath));
		if (relativeFile != null) {
			return new EclipseResource(relativeFile);
		}
		throw new FileNotFoundException("Cannot create relative resource '"
				+ relativePath + "' for " + getDescription());
	}

	@Override
	public String getFilename() {
		if (resource == null) {
			throw new IllegalStateException("File not found");
		}
		return resource.getName();
	}

	public String getDescription() {
		return "resource [" + (resource != null ? resource.getRawLocation() : "") + "]";
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof EclipseResource)) {
			return false;
		}
		EclipseResource that = (EclipseResource) other;
		return ObjectUtils.nullSafeEquals(this.resource, that.resource);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(resource);
	}
	
	public IResource getRawResource() {
		return resource;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IResource.class)) {
			return resource;
		}
		return null;
	}
}
