/*
 * Copyright 2002-2007 the original author or authors.
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
 * Java fragments and folders are only shown if, searched recursivly, contain at
 * least one file which has one of the specified file extensions.
 * 
 * @author Torsten Juergeleit
 */
public class JavaFileExtensionFilter extends FileExtensionFilter {

	/**
	 * Creates new instance of filter.
	 * 
	 * @param allowedFileExtensions list of file extension the filter has to
	 *			recognize or <code>null</code> if all files are allowed 
	 */
	public JavaFileExtensionFilter(String[] allowedFileExtensions) {
		super(allowedFileExtensions);
	}

	public JavaFileExtensionFilter(Collection<String> allowedFileExtensions) {
		super(allowedFileExtensions.toArray(new String[allowedFileExtensions
				.size()]));
	}

	public JavaFileExtensionFilter() {
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
			return hasAllowedFileExtension(((IStorage) element).getFullPath());
		}
		return super.select(viewer, parent, element);
	}
}
