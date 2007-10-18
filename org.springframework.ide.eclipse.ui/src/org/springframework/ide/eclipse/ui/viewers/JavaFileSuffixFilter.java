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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Viewer filter for file selection dialogs. The filter is not case sensitive.
 * Java fragments and folders are only shown if, searched recursively, contain at
 * least one file name ends with one of specified file suffixes.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class JavaFileSuffixFilter extends FileSuffixFilter {

	/**
	 * Creates new instance of filter.
	 * 
	 * @param allowedFileExtensions list of file extension the filter has to
	 *			recognize or <code>null</code> if all files are allowed 
	 */
	public JavaFileSuffixFilter(String[] allowedFileExtensions) {
		super(allowedFileExtensions);
	}

	public JavaFileSuffixFilter(Collection<String> allowedFileExtensions) {
		super(allowedFileExtensions.toArray(new String[allowedFileExtensions
				.size()]));
	}

	public JavaFileSuffixFilter() {
		super();
	}

	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot root = (IPackageFragmentRoot) element;
			try {
				for (IJavaElement child : root.getChildren()) {
					// recursive! Only show containers that contain a configs
					if (select(viewer, parent, child)) {
						return true;
					}
				}
				for (Object nonJavaResource : root.getNonJavaResources()) {
					// recursive! Only show containers that contain a configs
					if (select(viewer, parent, nonJavaResource)) {
						return true;
					}
				}
			} catch (JavaModelException e) {
				SpringUIPlugin.log(e.getStatus());
			}
			return false;
		} else if (element instanceof IPackageFragment) {
			IPackageFragment fragment = (IPackageFragment) element;
			try {
				for (IJavaElement child : fragment.getChildren()) {
					// recursive! Only show containers that contain a configs
					if (select(viewer, parent, child)) {
						return true;
					}
				}
				for (Object nonJavaResource : fragment.getNonJavaResources()) {
					// recursive! Only show containers that contain a configs
					if (select(viewer, parent, nonJavaResource)) {
						return true;
					}
				}
			} catch (JavaModelException e) {
				SpringUIPlugin.log(e.getStatus());
			}
			return false;
		} else if (element instanceof IStorage && !(element instanceof IFile)) {
			return hasAllowedFileSuffix(((IStorage) element).getFullPath());
		}
		return super.select(viewer, parent, element);
	}
}
