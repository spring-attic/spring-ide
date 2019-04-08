/*******************************************************************************
 * Copyright (c) 2006, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.xml.namespaces.ui.DefaultNamespaceDefinition;
import org.springframework.ide.eclipse.xml.namespaces.ui.DisplayThreadImageAccessor;
import org.springframework.ide.eclipse.xml.namespaces.ui.IImageAccessor;
import org.springframework.ide.eclipse.xml.namespaces.ui.INamespaceDefinition;
import org.springframework.ide.eclipse.xml.namespaces.ui.XmlUiNamespaceUtils.DefaultImageAccessor;
import org.springframework.util.StringUtils;

import static org.springframework.ide.eclipse.xml.namespaces.ui.XmlUiNamespaceUtils.*;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class UiNamespaceUtils {

	public static final String NAMESPACES_EXTENSION_POINT = BeansUIPlugin.PLUGIN_ID + ".namespaces";

	/**
	 * Returns the namespace URI for the given {@link ISourceModelElement} or
	 * <code>"http://www.springframework.org/schema/beans"</code> if no namespace URI found.
	 */
	public static String getNameSpaceURI(ISourceModelElement element) {
		String namespaceURI = ModelUtils.getNameSpaceURI(element);
		if (namespaceURI == null) {
			namespaceURI = DEFAULT_NAMESPACE_URI;
		}
		return namespaceURI;
	}

	/**
	 * Returns the {@link INamespaceLabelProvider} for the given {@link ISourceModelElement}'s namespace.
	 */
	public static INamespaceLabelProvider getLabelProvider(ISourceModelElement element) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = getNameSpaceURI(element);
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {
						if (config.getAttribute("labelProvider") != null) {
							try {
								Object provider = config.createExecutableExtension("labelProvider");
								if (provider instanceof INamespaceLabelProvider) {
									return (INamespaceLabelProvider) provider;
								}
							}
							catch (CoreException e) {
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

	/**
	 * Returns the {@link ITreeContentProvider} for the given {@link ISourceModelElement}'s namespace.
	 */
	public static ITreeContentProvider getContentProvider(ISourceModelElement element) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = getNameSpaceURI(element);
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {
						if (config.getAttribute("contentProvider") != null) {
							try {
								Object provider = config.createExecutableExtension("contentProvider");
								if (provider instanceof IContentProvider) {
									return (ITreeContentProvider) provider;
								}
							}
							catch (CoreException e) {
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

	public static INamespaceDefinition getDefaultNamespaceDefinition() {
		INamespaceDefinitionResolver definitionResolver = SpringXmlNamespacesPlugin.getNamespaceDefinitionResolver(null);
		org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinition namespaceDefinition = definitionResolver
				.resolveNamespaceDefinition(DEFAULT_NAMESPACE_URI);

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = DEFAULT_NAMESPACE_URI;
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {

						String prefix = config.getAttribute("prefix");
						if (!StringUtils.hasText(prefix) && namespaceDefinition != null) {
							prefix = namespaceDefinition.getPrefix();
						}
						String schemaLocation = config.getAttribute("defaultSchemaLocation");
						if (!StringUtils.hasText(schemaLocation) && namespaceDefinition != null) {
							schemaLocation = namespaceDefinition.getDefaultSchemaLocation();
						}
						String uri = config.getAttribute("uri");
						IImageAccessor image = null;
						if (config.getAttribute("icon") != null) {
							String ns = config.getDeclaringExtension().getNamespaceIdentifier();
							String icon = config.getAttribute("icon");
							image = new DefaultImageAccessor(ns, icon);
						}
						else if (namespaceDefinition != null) {
							image = new NamespaceDefinitionImageAccessor(namespaceDefinition);
						}
						return new DefaultNamespaceDefinition(prefix, uri, schemaLocation,
								namespaceDefinition.getUriMapping(), image);
					}
				}
			}
		}

		if (namespaceDefinition != null) {
			return new DefaultNamespaceDefinition(namespaceDefinition.getPrefix(),
					namespaceDefinition.getNamespaceUri(), namespaceDefinition.getDefaultSchemaLocation(),
					namespaceDefinition.getUriMapping(), new NamespaceDefinitionImageAccessor(namespaceDefinition));
		}
		return null;
	}

}
