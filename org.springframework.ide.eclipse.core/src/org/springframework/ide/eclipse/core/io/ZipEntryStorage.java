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
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * Wrapper for an entry in a ZIP file.
 * 
 * @author Torsten Juergeleit
 */
public class ZipEntryStorage extends PlatformObject implements IStorage {

	/**
	 * This string (with the value of "!") is used to delimit ZIP file name plus
	 * the corresponding ZIP entry (delimited by
	 * <code>ZipEntryStorage.NAME_DELIMITER</code>)
	 */
	public static final String DELIMITER = "!";

	private String fullName;

	private IResource zipResource;

	private String entryName;

	private IPath entryPath;

	/**
	 * Creates a <code>ZipEntryStorage</code> from a full-qualified name of a
	 * ZIP file entry (project-relative path to the ZIP file plus full path of
	 * the ZIP file entry delimited by <code>DELIMITER</code>) and the
	 * project which contains the ZIP file.
	 * 
	 * @param project
	 *            the project which contains the ZIP file
	 * @param fullName
	 *            the full-qualified name of the ZIP file entry
	 *            (project-relative path to the ZIP file plus full path of the
	 *            ZIP file entry delimited by <code>DELIMITER</code>)
	 */
	public ZipEntryStorage(IProject project, String fullName) {
		int pos = fullName.indexOf(ZipEntryStorage.DELIMITER);
		if (pos == -1 || pos == (fullName.length() - DELIMITER.length())) {
			throw new IllegalArgumentException("Illegal JAR entry name '"
					+ fullName + "'");
		} else {
			this.fullName = fullName;
			this.zipResource = project.findMember(fullName.substring(0, pos));
			this.entryName = fullName.substring(pos + DELIMITER.length());
			this.entryPath = new Path(this.entryName);
		}
	}

	/**
	 * Creates a <code>ZipEntryStorage</code> from a full path of a ZIP file
	 * entry and the corresponding ZIP file resource.
	 * 
	 * @param zipResource
	 *            the ZIP file resource
	 * @param entryName
	 *            the full path of the ZIP file entry
	 */
	public ZipEntryStorage(IResource zipResource, String entryName) {
		this.fullName = zipResource.getProjectRelativePath() + DELIMITER
				+ entryName;
		this.zipResource = zipResource;
		this.entryName = entryName;
		this.entryPath = new Path(entryName);
	}

	/**
	 * Creates a <code>ZipEntryStorage</code> from a given archived model
	 * element.
	 * 
	 * @param element
	 *            the archived model element
	 */
	public ZipEntryStorage(IResourceModelElement element) {
		if (element == null || !element.isElementArchived()) {
			throw new IllegalArgumentException();
		}
		this.fullName = element.getElementName();
		this.zipResource = element.getElementResource();
		this.entryName = fullName.substring(fullName.indexOf(DELIMITER)
				+ DELIMITER.length());
		this.entryPath = new Path(entryName);
	}

	/**
	 * Returns the ZIP file resource of this ZIP file entry.
	 */
	public IResource getZipResource() {
		return zipResource;
	}

	public InputStream getContents() throws CoreException {
		try {
			ZipFile zipFile = new ZipFile(zipResource.getLocation().toFile());
			ZipEntry zipEntry = zipFile.getEntry(this.entryName);
			if (zipEntry == null) {
				throw new CoreException(SpringCore.createErrorStatus(
						"Invalid path '" + entryName + "'", null));
			}
			return zipFile.getInputStream(zipEntry);
		} catch (IOException e) {
			throw new CoreException(SpringCore.createErrorStatus(
					e.getMessage(), e));
		}
	}

	/**
	 * Returns the full path of the ZIP file entry.
	 */
	public IPath getFullPath() {
		return entryPath;
	}

	public String getName() {
		return entryPath.lastSegment();
	}

	/**
	 * Returns the full-qualified name of the ZIP file entry (project-relative
	 * path to the ZIP file plus full path of the ZIP file entry delimited by
	 * <code>DELIMITER</code>).
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns <code>true</code> because the ZIP file entry is not modifiable.
	 */
	public boolean isReadOnly() {
		return true;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ZipEntryStorage)) {
			return false;
		}
		ZipEntryStorage other = (ZipEntryStorage) obj;
		return fullName.equals(other.fullName);
	}

	public int hashCode() {
		return fullName.hashCode();
	}

	/**
	 * Adapts to <code>org.eclipse.core.resources.IResource</code>,
	 * <code>java.io.File</code> or <code>java.util.zip.ZipFile</code>.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IResource.class)) {
			return zipResource;
		} else if (adapter.equals(File.class)) {
			return zipResource.getFullPath().toFile();
		} else if (adapter.equals(ZipFile.class)) {
			try {
				return new ZipFile(zipResource.getFullPath().toFile());
			} catch (IOException e) {
				SpringCore.log(e);
				return null;
			}
		}
		return super.getAdapter(adapter);
	}

	public String toString() {
		return "ZipEntryStorage[" + zipResource.toString() + " - " + entryPath
				+ "]";
	}
}
