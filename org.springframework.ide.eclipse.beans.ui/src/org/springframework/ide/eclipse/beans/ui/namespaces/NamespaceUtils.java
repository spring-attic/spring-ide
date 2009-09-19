/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.util.StringUtils;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class NamespaceUtils {

	public static final String NAMESPACES_EXTENSION_POINT = BeansUIPlugin.PLUGIN_ID + ".namespaces";

	public static final String DEFAULT_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	public static final String TOOLS_NAMESPACE_URI = "http://www.springframework.org/schema/tool";

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

	public static List<INamespaceDefinition> getNamespaceDefinitions() {
		List<INamespaceDefinition> namespaceDefinitions = new ArrayList<INamespaceDefinition>();
		INamespaceDefinitionResolver definitionResolver = BeansCorePlugin.getNamespaceDefinitionResolver();
		Set<org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition> detectedNamespaceDefinitions = definitionResolver
				.getNamespaceDefinitions();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					String uri = config.getAttribute("uri");
					org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition = definitionResolver
							.resolveNamespaceDefinition(uri);
					detectedNamespaceDefinitions.remove(namespaceDefinition);

					String prefix = config.getAttribute("prefix");
					if (!StringUtils.hasText(prefix) && namespaceDefinition != null) {
						prefix = namespaceDefinition.getPrefix();
					}
					String schemaLocation = config.getAttribute("defaultSchemaLocation");
					if (!StringUtils.hasText(schemaLocation) && namespaceDefinition != null) {
						schemaLocation = namespaceDefinition.getDefaultSchemaLocation();
					}
					Image image = null;
					if (config.getAttribute("icon") != null) {
						String ns = config.getDeclaringExtension().getNamespaceIdentifier();
						String icon = config.getAttribute("icon");
						image = getImage(ns, icon);
					}
					else if (namespaceDefinition != null) {
						String ns = namespaceDefinition.getBundle().getSymbolicName();
						String icon = namespaceDefinition.getIconPath();
						image = getImage(ns, icon);
					}

					DefaultNamespaceDefinition def = null;
					if (namespaceDefinition != null) {
						def = new DefaultNamespaceDefinition(prefix, uri, schemaLocation, namespaceDefinition
								.getUriMapping(), image);
					}
					else {
						def = new DefaultNamespaceDefinition(prefix, uri, schemaLocation, new Properties(), image);
					}

					// get schema locations from nested child elements
					IConfigurationElement[] schemaLocationConfigElements = config.getChildren("schemaLocation");
					for (IConfigurationElement schemaLocationConfigElement : schemaLocationConfigElements) {
						def.addSchemaLocation(schemaLocationConfigElement.getAttribute("url"));
					}
					if (def.getSchemaLocations().size() == 0 && namespaceDefinition != null) {
						def.getSchemaLocations().addAll(namespaceDefinition.getSchemaLocations());
					}

					namespaceDefinitions.add(def);
				}
			}
		}

		for (org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition : detectedNamespaceDefinitions) {
			String ns = namespaceDefinition.getBundle().getSymbolicName();
			String icon = namespaceDefinition.getIconPath();
			Image image = null;
			if (icon != null) {
				image = getImage(ns, icon);
			}
			DefaultNamespaceDefinition def = new DefaultNamespaceDefinition(namespaceDefinition.getPrefix(),
					namespaceDefinition.getNamespaceUri(), namespaceDefinition.getDefaultSchemaLocation(),
					namespaceDefinition.getUriMapping(), image);
			def.getSchemaLocations().addAll(namespaceDefinition.getSchemaLocations());
			namespaceDefinitions.add(def);
		}

		Collections.sort(namespaceDefinitions, new Comparator<INamespaceDefinition>() {
			public int compare(INamespaceDefinition o1, INamespaceDefinition o2) {
				if (o1 != null && o1.getNamespacePrefix() != null && o2 != null && o2.getNamespacePrefix() != null) {
					return o1.getNamespacePrefix().compareTo(o2.getNamespacePrefix());
				}
				return 0;
			}
		});

		// remove the tool namespace as we don't want to surface on the UI
		for (INamespaceDefinition definition : new ArrayList<INamespaceDefinition>(namespaceDefinitions)) {
			if (TOOLS_NAMESPACE_URI.equals(definition.getNamespaceURI())) {
				namespaceDefinitions.remove(definition);
			}
		}

		return namespaceDefinitions;
	}

	public static INamespaceDefinition getDefaultNamespaceDefinition() {
		INamespaceDefinitionResolver definitionResolver = BeansCorePlugin.getNamespaceDefinitionResolver();
		org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition = definitionResolver
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
						Image image = null;
						if (config.getAttribute("icon") != null) {
							String ns = config.getDeclaringExtension().getNamespaceIdentifier();
							String icon = config.getAttribute("icon");
							image = getImage(ns, icon);
						}
						else if (namespaceDefinition != null) {
							String ns = namespaceDefinition.getBundle().getSymbolicName();
							String icon = namespaceDefinition.getIconPath();
							image = getImage(ns, icon);
						}
						return new DefaultNamespaceDefinition(prefix, uri, schemaLocation, namespaceDefinition
								.getUriMapping(), image);
					}
				}
			}
		}

		if (namespaceDefinition != null) {
			String ns = namespaceDefinition.getBundle().getSymbolicName();
			String icon = namespaceDefinition.getIconPath();
			Image image = getImage(ns, icon);
			return new DefaultNamespaceDefinition(namespaceDefinition.getPrefix(), namespaceDefinition
					.getNamespaceUri(), namespaceDefinition.getDefaultSchemaLocation(), namespaceDefinition
					.getUriMapping(), image);
		}
		return null;
	}

	/**
	 * Returns an {@link Image} instance which is located at the indicated icon path.
	 */
	public static Image getImage(String ns, String icon) {
		if (StringUtils.hasText(icon)) {
			Image image = BeansUIPlugin.getDefault().getImageRegistry().get(icon);
			if (image == null) {
				ImageDescriptor imageDescriptor = BeansUIPlugin.imageDescriptorFromPlugin(ns, icon);
				BeansUIPlugin.getDefault().getImageRegistry().put(icon, imageDescriptor);
				image = BeansUIPlugin.getDefault().getImageRegistry().get(icon);
			}
			return image;
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}
	}

}
