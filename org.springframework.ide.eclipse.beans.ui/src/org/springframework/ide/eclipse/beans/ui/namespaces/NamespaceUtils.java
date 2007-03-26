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
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * Some helper methods.
 * 
 * @author Torsten Juergeleit
 */
public class NamespaceUtils {

	public static final String NAMESPACES_EXTENSION_POINT = BeansUIPlugin
			.PLUGIN_ID + ".namespaces";

	public static final String DEFAULT_NAMESPACE_URI =
			"http://www.springframework.org/schema/beans"; 

	/**
	 * Returns the namespace URI for the given {@link ISourceModelElement} or
	 * <code>"http://www.springframework.org/schema/beans"</code> if no
	 * namespace URI found.
	 */
	public static String getNameSpaceURI(ISourceModelElement element) {
		String namespaceURI = ModelUtils.getNameSpaceURI(element);
		if (namespaceURI == null) {
			namespaceURI = DEFAULT_NAMESPACE_URI;
		}
		return namespaceURI;
	}

	/**
	 * Returns the {@link ILabelProvider} for the given
	 * {@link ISourceModelElement}'s namespace.
	 */
	public static ILabelProvider getLabelProvider(
			ISourceModelElement element) {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = getNameSpaceURI(element);
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {
						try {
							Object provider = config
								.createExecutableExtension("labelProvider");
							if (provider instanceof ILabelProvider) {
								return (ILabelProvider) provider;
							}
						} catch (CoreException e) {
							BeansUIPlugin.log(e);
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link ITreeContentProvider} for the given
	 * {@link ISourceModelElement}'s namespace.
	 */
	public static ITreeContentProvider getContentProvider(
			ISourceModelElement element) {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = getNameSpaceURI(element);
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {
						if (config.getAttribute("contentProvider") != null) {
							try {
								Object provider = config
										.createExecutableExtension(
												"contentProvider");
								if (provider instanceof IContentProvider) {
									return (ITreeContentProvider) provider;
								}
							} catch (CoreException e) {
								BeansUIPlugin.log(e);
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}
}
