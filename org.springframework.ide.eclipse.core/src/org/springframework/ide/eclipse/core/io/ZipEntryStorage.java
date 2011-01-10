/*******************************************************************************
 * Copyright (c) 2006, 2010 Spring IDE Developers
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
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class ZipEntryStorage implements IStorage, IAdaptable {

	/**
	 * This string (with the value of "!") is used to delimit the ZIP file name from the corresponding ZIP entry
	 */
	public static final String DELIMITER = "!";

	private String fullName;

	private IFile file;

	private String entryName;

	private IPath entryPath;

	private IResourceModelElement parentModelElement;

	/**
	 * Creates a <code>ZipEntryStorage</code> from a full-qualified name of a ZIP file entry (project-relative path to
	 * the ZIP file plus full path of the ZIP file entry delimited by <code>DELIMITER</code>) and the project which
	 * contains the ZIP file.
	 * @param project the project which contains the ZIP file
	 * @param fullName the full-qualified name of the ZIP file entry (project-relative path to the ZIP file plus full
	 * path of the ZIP file entry delimited by <code>DELIMITER</code>)
	 */
	public ZipEntryStorage(IProject project, String fullName) {
		int pos = fullName.indexOf(ZipEntryStorage.DELIMITER);
		if (pos == -1 || pos == (fullName.length() - DELIMITER.length())) {
			throw new IllegalArgumentException("Illegal JAR entry name '" + fullName + "'");
		}
		else {
			IResource member = project.findMember(fullName.substring(0, pos));
			if (member == null || !(member instanceof IFile)) {
				throw new IllegalArgumentException("Missing or wrong zip file: " + file);
			}
			this.fullName = fullName;
			this.file = (IFile) member;
			this.entryName = fullName.substring(pos + DELIMITER.length());
			this.entryPath = new Path(this.entryName);
		}
	}

	/**
	 * Creates a <code>ZipEntryStorage</code> from a full path of a ZIP file entry and the corresponding ZIP file.
	 * @param file the ZIP file
	 * @param entryName the full path of the ZIP file entry
	 */
	public ZipEntryStorage(IFile file, String entryName) {
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("Missing or wrong zip file: " + file);
		}
		this.fullName = file.getProjectRelativePath() + DELIMITER + entryName;
		this.file = file;
		this.entryName = entryName;
		this.entryPath = new Path(entryName);
	}

	/**
	 * Creates a <code>ZipEntryStorage</code> from a given archived model element.
	 * @param element the archived model element
	 */
	public ZipEntryStorage(IResourceModelElement element) {
		if (element == null || !element.isElementArchived() || !(element.getElementResource() instanceof IFile)) {
			throw new IllegalArgumentException("Missing or wrong model element: " + element);
		}
		this.fullName = element.getElementName();
		this.file = (IFile) element.getElementResource();
		this.entryName = fullName.substring(fullName.indexOf(DELIMITER) + DELIMITER.length());
		this.entryPath = new Path(entryName);
		this.parentModelElement = element;
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
			String cleanedEntryName = entryName;
			if (cleanedEntryName.length() > 1 && cleanedEntryName.charAt(0) == '/') {
				cleanedEntryName = cleanedEntryName.substring(1);
			}
			ZipEntry entry = file.getEntry(cleanedEntryName);
			if (entry == null) {
				throw new CoreException(SpringCore.createErrorStatus("Invalid path '" + cleanedEntryName + "'", null));
			}

			return InputStreamUtils.getWrappedInputStream(file, entry);
		}
		catch (IOException e) {
			throw new CoreException(SpringCore.createErrorStatus(e.getMessage(), e));
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
	 * Returns the full-qualified name of the ZIP file entry (project-relative path to the ZIP file plus full path of
	 * the ZIP file entry delimited by <code>DELIMITER</code>).
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Returns the name of the entry within the Zip file
	 * @since 2.0.3
	 */
	public String getEntryName() {
		return entryName;
	}

	/**
	 * Returns the full-qualified name of the ZIP file entry (workspace-relative path to the ZIP file plus full path of
	 * the ZIP file entry delimited by <code>DELIMITER</code>).
	 */
	public String getAbsoluteName() {
		return file.getProject().getFullPath().append(fullName).toString();
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
			}
			else if (adapter.equals(File.class)) {
				return file.getFullPath().toFile();
			}
			else if (adapter.equals(ZipFile.class)) {
				try {
					return new ZipFile(file.getFullPath().toFile());
				}
				catch (IOException e) {
					SpringCore.log(e);
				}
			}
		}
		if (IResourceModelElement.class.equals(adapter)) {
			return this.parentModelElement;
		}
		return null;
	}

	@Override
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

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(fullName);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(file);
	}

	@Override
	public String toString() {
		return "ZipEntryStorage[" + file.toString() + " - " + entryPath + "]";
	}
}
