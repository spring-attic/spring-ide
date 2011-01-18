/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.workingsets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Utility class that reads out the contentContribution extension point.
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class WorkingSetUtils {

	public static final String EXTENSION_POINT = SpringUIPlugin.PLUGIN_ID
			+ ".contentContribution";

	@SuppressWarnings("unchecked")
	private static <T> Set<T> getExecutableExtension(String attributeName,
			Class<T> requiredType) {
		Set<T> extensions = new HashSet<T>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					try {
						if (config.getAttribute(attributeName) != null) {
							extensions.add((T) config
									.createExecutableExtension(attributeName));
						}
					}
					catch (Exception e) {
						// log classcast to log file
						SpringUIPlugin.log(e);
					}
				}
			}
		}
		return extensions;
	}

	public static Set<ITreeContentProvider> getContentProvider() {
		return getExecutableExtension("contentProvider",
				ITreeContentProvider.class);
	}

	public static Set<IElementSpecificLabelProvider> getLabelProvider() {
		return getExecutableExtension("labelProvider",
				IElementSpecificLabelProvider.class);
	}

	public static Set<IWorkingSetFilter> getViewerFilter() {
		return getExecutableExtension("viewerFilter", IWorkingSetFilter.class);
	}
}
