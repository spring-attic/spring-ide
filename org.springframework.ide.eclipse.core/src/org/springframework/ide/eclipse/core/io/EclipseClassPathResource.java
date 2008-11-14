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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Resource} abstraction that uses a file path and tries to resolve that
 * path to a Resource implementation on an {@link IProject} source folder
 * configuration.
 * <p>
 * The path resolution might not be successful in the end. Before calling
 * {@link #getInputStream()} make sure to call {@link #exists()} as specified in
 * the {@link Resource} interface contract.
 * <p>
 * Note: this implementation is not intended to be complete and correct
 * Implementation of the {@link Resource} interface.
 * @author Christian Dupuis
 * @since 2.0.3
 */
class EclipseClassPathResource extends AbstractResource implements IAdaptable {

	private final String path;

	private final IProject project;

	private Resource resource;

	/**
	 * Constructor taking a path which will be resolved in the given
	 * {@link IProject}.
	 */
	public EclipseClassPathResource(String path, IProject project) {
		Assert.notNull(path, "Path must not be null");
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		this.path = StringUtils.cleanPath(path);
		this.project = project;
		this.resource = ResourceUtils.getResource(path, project);
	}
	
	/**
	 * Creates a relative {@link Resource}.
	 */
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path,
				relativePath);
		return new EclipseClassPathResource(pathToUse, this.project);
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof EclipseClassPathResource) {
			EclipseClassPathResource otherRes = (EclipseClassPathResource) obj;
			return (this.path.equals(otherRes.path) && ObjectUtils
					.nullSafeEquals(this.project, otherRes.project));
		}
		return false;
	}
	
	/**
	 * Checks if the resource resolution was successful.
	 */
	@Override
	public boolean exists() {
		return this.resource != null && this.resource.exists();
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IResource.class)
				&& resource instanceof EclipseResource) {
			return ((EclipseResource) resource).getRawResource();
		}
		else if (adapter.equals(IResource.class)
				&& resource instanceof FileResource) {
			return ((FileResource) resource).getRawFile();
		}
		else if (adapter.equals(IFile.class)
				&& resource instanceof FileResource) {
			return ((FileResource) resource).getRawFile();
		}
		else if ((adapter.equals(ZipEntryStorage.class) || adapter
				.equals(IStorage.class))
				&& resource instanceof StorageResource) {
			return ((StorageResource) resource).getRawStorage();
		}
		return null;
	}

	public String getDescription() {
		if (this.resource != null) {
			return this.resource.getDescription();
		}
		else {
			return path;
		}
	}

	public String getFilename() {
		if (this.resource != null) {
			return this.resource.getFilename();
		}
		return path;
	}

	public InputStream getInputStream() throws IOException {
		if (this.resource != null) {
			return this.resource.getInputStream();
		}
		else {
			throw new FileNotFoundException("file not found");
		}
	}

	public final String getPath() {
		return this.path;
	}

	public int hashCode() {
		return this.path.hashCode();
	}

}
