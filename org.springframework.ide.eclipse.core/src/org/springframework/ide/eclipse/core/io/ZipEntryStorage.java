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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.util.ObjectUtils;

/**
 * Wrapper for an entry in a ZIP file.
 * 
 * @author Torsten Juergeleit
 */
public class ZipEntryStorage implements IStorage, IAdaptable {

	/**
	 * This string (with the value of "!") is used to delimit the ZIP file name
	 * from the corresponding ZIP entry
	 */
	public static final String DELIMITER = "!"; 

	private String fullName;
	private IFile file;
	private String entryName;
	private IPath entryPath;

	/**
	 * Creates a <code>ZipEntryStorage</code> from a full-qualified name of a
	 * ZIP file entry (project-relative path to the ZIP file plus full path of
	 * the ZIP file entry delimited by <code>DELIMITER</code>) and the project
	 * which contains the ZIP file.
	 * @param project  the project which contains the ZIP file
	 * @param fullName  the full-qualified name of the ZIP file entry
	 *			(project-relative path to the ZIP file plus full path of the
	 *			ZIP file entry delimited by <code>DELIMITER</code>)
	 */
	public ZipEntryStorage(IProject project, String fullName) {
		int pos = fullName.indexOf(ZipEntryStorage.DELIMITER);
		if (pos == -1 || pos == (fullName.length() - DELIMITER.length())) {
			throw new IllegalArgumentException("Illegal JAR entry name '"
					+ fullName + "'");
		} else {
			IResource member = project.findMember(fullName.substring(0, pos));
			if (member == null || !(member instanceof IFile)) {
				throw new IllegalArgumentException(
						"Missing or wrong zip file: " + file);
			}
			this.fullName = fullName;
			this.file = (IFile) member;
			this.entryName = fullName.substring(pos + DELIMITER.length());
			this.entryPath = new Path(this.entryName);
		}
	}

	/**
	 * Creates a <code>ZipEntryStorage</code> from a full path of a
	 * ZIP file entry and the corresponding ZIP file.
	 * @param file  the ZIP file
	 * @param entryName  the full path of the ZIP file entry
	 */
	public ZipEntryStorage(IFile file, String entryName) {
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("Missing or wrong zip file: "
					+ file);
		}
		this.fullName = file.getProjectRelativePath() + DELIMITER + entryName;
		this.file = file;
		this.entryName = entryName;
		this.entryPath = new Path(entryName);
	}

	/**
	 * Creates a <code>ZipEntryStorage</code> from a given archived model
	 * element.
	 * 
	 * @param element  the archived model element
	 */
	public ZipEntryStorage(IResourceModelElement element) {
		if (element == null || !element.isElementArchived()
				|| !(element.getElementResource() instanceof IFile)) {
			throw new IllegalArgumentException(
					"Missing or wrong model element: " + element);
		}
		this.fullName = element.getElementName();
		this.file = (IFile) element.getElementResource();
		this.entryName = fullName.substring(fullName.indexOf(DELIMITER)
				+ DELIMITER.length());
		this.entryPath = new Path(entryName);
	}

	/**
	 * Returns the ZIP file resource of this ZIP file entry.
	 */
	public IFile getFile() {
		return file;
	}

	public InputStream getContents() throws CoreException {
		try {
			ZipFile file = new ZipFile(this.file.getLocation().toFile()); 
			ZipEntry entry = file.getEntry(this.entryName);
			if (entry == null) {
				throw new CoreException(SpringCore.createErrorStatus(
						"Invalid path '" + entryName + "'", null));
			}
			return file.getInputStream(entry);
		} catch (IOException e){
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
	 * Returns the full-qualified name of the ZIP file entry
	 * (project-relative path to the ZIP file plus full path of the
	 * ZIP file entry delimited by <code>DELIMITER</code>).
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the full-qualified name of the ZIP file entry
	 * (workspace-relative path to the ZIP file plus full path of the
	 * ZIP file entry delimited by <code>DELIMITER</code>).
	 */
	public String getAbsoluteName() {
		return file.getProject().getFullPath().append(fullName)
				.toString();
	}

	/**
	 * Returns <code>true</code> because the ZIP file entry is not modifiable. 
	 */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * Adapts to {@link IFile}, {@link File} or {@link ZipFile}.
	 */
	public Object getAdapter(Class adapter) {
		if (file != null) {
			if (adapter.equals(IFile.class)) {
				return file;
			} else if (adapter.equals(File.class)) {
				return file.getFullPath().toFile();
			} else if (adapter.equals(ZipFile.class)) {
				try {
					return new ZipFile(file.getFullPath().toFile());
				} catch (IOException e) {
					SpringCore.log(e);
				}
			}
		}
		return null;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ZipEntryStorage)) {
			return false;
		}
		ZipEntryStorage that = (ZipEntryStorage) other;
		if (!ObjectUtils.nullSafeEquals(this.fullName, that.fullName))
			return false;
		return ObjectUtils.nullSafeEquals(this.file, that.file);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(fullName);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(file);
	}

	public String toString() {
		return "ZipEntryStorage[" + file.toString() + " - " + entryPath
				+ "]";
	}
}
