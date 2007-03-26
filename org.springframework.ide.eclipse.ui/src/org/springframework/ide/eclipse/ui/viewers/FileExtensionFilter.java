/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.viewers;

import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Viewer filter for file selection dialogs. The filter is not case sensitive.
 * Folders are only shown if, searched recursivly, contain at least one file
 * which has one of the specified file extensions.
 * 
 * @author Torsten Juergeleit
 */
public class FileExtensionFilter extends ViewerFilter {

	private String[] allowedFileExtensions;

	/**
	 * Creates new instance of filter.
	 * 
	 * @param allowedFileExtensions list of file extension the filter has to
	 *			recognize or <code>null</code> if all files are allowed 
	 */
	public FileExtensionFilter(String[] allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	public FileExtensionFilter(Collection<String> allowedFileExtensions) {
		this(allowedFileExtensions.toArray(new String[allowedFileExtensions
				.size()]));
	}

	public FileExtensionFilter() {
		allowedFileExtensions = null;
	}

	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IFile) {
			return hasAllowedFileExtension(((IFile) element).getFullPath());
		} else if (element instanceof IContainer) { // IProject, IFolder
			try {
				for (IResource resource : ((IContainer) element).members()) {
					// recursive! Only show containers that contain a configs
					if (select(viewer, parent, resource)) {
						return true;
					}
				}
			} catch (CoreException e) {
				SpringUIPlugin.log(e.getStatus());
			}
		}
		return false;
	}

	protected boolean hasAllowedFileExtension(IPath path) {
		if (allowedFileExtensions == null) {
			return true;
		}
		String extension = path.getFileExtension();
		if (extension != null) {
			for (String allowedExtension : allowedFileExtensions) {
				if (extension.equalsIgnoreCase(allowedExtension)) {
					return true;
				}
			}
		}
		return false;
	}
}
