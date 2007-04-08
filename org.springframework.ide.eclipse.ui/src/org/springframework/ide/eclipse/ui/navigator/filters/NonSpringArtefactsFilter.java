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
package org.springframework.ide.eclipse.ui.navigator.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Filters non-Spring artefacts.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class NonSpringArtefactsFilter extends ViewerFilter {

	public static final String PROJECT_EXPLORER_EXTENSION_POINT =
			SpringUIPlugin.PLUGIN_ID + ".projectExplorer";

	private List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
	
	public NonSpringArtefactsFilter() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PROJECT_EXPLORER_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					try {
						Object filter = config
								.createExecutableExtension("class");
						if (filter instanceof ViewerFilter) {
							filters.add((ViewerFilter) filter);
						}
					} catch (CoreException e) {
						SpringUIPlugin.log(e);
					}
				}
			}
		}
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if ((element instanceof IFolder || element instanceof IFile)
				&& SpringCoreUtils.isSpringProject((IResource) element)) {
			for (ViewerFilter filter : filters) {
				if (filter.select(viewer, parentElement, element)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
}
