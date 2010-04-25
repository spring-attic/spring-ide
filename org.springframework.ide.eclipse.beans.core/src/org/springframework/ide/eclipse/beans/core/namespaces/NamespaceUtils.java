/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.namespaces;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.xml.sax.EntityResolver;

/**
 * Some helper methods that deal with loading extension point contributions.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class NamespaceUtils {

	public static final String NAMESPACES_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID + ".namespaces";

	public static final String RESOLVERS_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID + ".resolvers";

	public static final String DEFAULT_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	private static Set<NamespaceHandlerResolver> namespaceHandlerResolvers;

	private static Map<String, IModelElementProvider> modelElementProviders;

	private static Set<EntityResolver> entityResolvers;

	private static Map<NamespaceHandlerDescriptor, NamespaceHandler> namespaceHandlers;

	/**
	 * Returns the namespace URI for the given {@link BeanMetadataElement} or
	 * <code>"http://www.springframework.org/schema/beans"</code> if no namespace URI found.
	 */
	public static String getNameSpaceURI(BeanMetadataElement element) {
		IModelSourceLocation location = ModelUtils.getSourceLocation(element);
		if (location instanceof XmlSourceLocation) {
			String namespaceURI = ((XmlSourceLocation) location).getNamespaceURI();
			if (namespaceURI != null) {
				return namespaceURI;
			}
		}
		return DEFAULT_NAMESPACE_URI;
	}

	/**
	 * Returns a {@link Map} with all registered {@link NamespaceHandler}s.
	 */
	public synchronized static Map<NamespaceHandlerDescriptor, NamespaceHandler> getNamespaceHandlers() {
		if (namespaceHandlers == null) {
			Map<NamespaceHandlerDescriptor, NamespaceHandler> handlers = new HashMap<NamespaceHandlerDescriptor, NamespaceHandler>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						String uri = config.getAttribute("uri");
						if (uri != null && config.getAttribute("namespaceHandler") != null) {
							try {
								String handlerClassName = config.getAttribute("class");
								String provider = config.getAttribute("provider");
								if (!StringUtils.hasLength(provider)) {
									provider = extension.getContributor().getName();
								}
								Object handler = loadHandler(provider, handlerClassName);
								if (handler instanceof NamespaceHandler) {
									NamespaceHandler namespaceHandler = (NamespaceHandler) handler;
									namespaceHandler.init();
									handlers.put(
											NamespaceHandlerDescriptor.createNamespaceHandlerDescriptor(uri, null),
											namespaceHandler);
								}
							}
							catch (InvalidRegistryObjectException e) {
								BeansCorePlugin.log(e);
							}
						}
						for (IConfigurationElement namespaceHandlerConfig : config.getChildren("namespaceHandler")) {
							if (uri != null && namespaceHandlerConfig.getAttribute("class") != null) {
								try {
									String schemaLocation = namespaceHandlerConfig.getAttribute("schemaLocation");
									String handlerClassName = namespaceHandlerConfig.getAttribute("class");
									String provider = namespaceHandlerConfig.getAttribute("provider");
									if (!StringUtils.hasLength(provider)) {
										provider = extension.getContributor().getName();
									}
									Object handler = loadHandler(provider, handlerClassName);
									if (handler instanceof NamespaceHandler) {
										NamespaceHandler namespaceHandler = (NamespaceHandler) handler;
										namespaceHandler.init();
										handlers.put(NamespaceHandlerDescriptor.createNamespaceHandlerDescriptor(uri,
												schemaLocation), namespaceHandler);
									}
								}
								catch (InvalidRegistryObjectException e) {
									BeansCorePlugin.log(e);
								}
							}
						}
					}
				}
			}
			namespaceHandlers = handlers;
		}
		return namespaceHandlers;
	}

	/**
	 * Returns a {@link Map} with all registered {@link IModelElementProvider}s.
	 */
	public synchronized static Map<String, IModelElementProvider> getElementProviders() {
		if (modelElementProviders == null) {
			Map<String, IModelElementProvider> providers = new HashMap<String, IModelElementProvider>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						String uri = config.getAttribute("uri");
						if (uri != null && config.getAttribute("elementProvider") != null) {
							try {
								Object provider = config.createExecutableExtension("elementProvider");
								if (provider instanceof IModelElementProvider) {
									providers.put(uri, (IModelElementProvider) provider);
								}
							}
							catch (CoreException e) {
								BeansCorePlugin.log(e);
							}
						}
					}
				}
			}
			modelElementProviders = providers;
		}
		return modelElementProviders;
	}

	/**
	 * Returns a {@link Set} with all registered {@link NamespaceHandlerResolver}s.
	 * @since 2.0.1
	 */
	public synchronized static Set<NamespaceHandlerResolver> getNamespaceHandlerResolvers() {
		if (namespaceHandlerResolvers == null) {
			Set<NamespaceHandlerResolver> handlers = new HashSet<NamespaceHandlerResolver>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RESOLVERS_EXTENSION_POINT);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						if ("namespaceHandlerResolver".equals(config.getName()) && config.getAttribute("class") != null) {
							try {
								String handlerClassName = config.getAttribute("class");
								Object handler = loadHandler(handlerClassName, extension.getContributor().getName());
								if (handler instanceof NamespaceHandlerResolver) {
									NamespaceHandlerResolver namespaceHandlerResolver = (NamespaceHandlerResolver) handler;
									handlers.add(namespaceHandlerResolver);
								}
							}
							catch (InvalidRegistryObjectException e) {
								BeansCorePlugin.log(e);
							}
						}
					}
				}
			}
			// Add the OSGi-based namespace handler resolver
			handlers.add(BeansCorePlugin.getNamespaceHandlerResolver());
			namespaceHandlerResolvers = handlers;
		}
		return namespaceHandlerResolvers;
	}

	/**
	 * Returns a {@link Set} with all registered {@link EntityResolver}s.
	 * @since 2.0.1
	 */
	public synchronized static Set<EntityResolver> getEntityResolvers() {
		if (entityResolvers == null) {
			Set<EntityResolver> handlers = new HashSet<EntityResolver>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RESOLVERS_EXTENSION_POINT);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						if ("entityResolver".equals(config.getName()) && config.getAttribute("class") != null) {
							try {
								Object handler = config.createExecutableExtension("class");
								if (handler instanceof EntityResolver) {
									EntityResolver entityResolver = (EntityResolver) handler;
									handlers.add(entityResolver);
								}
							}
							catch (CoreException e) {
								BeansCorePlugin.log(e);
							}
						}
					}
				}
			}
			entityResolvers = handlers;
		}
		return entityResolvers;
	}

	/**
	 * Checks if a project should load namespace handlers from the classpath or use the global catalog.
	 */
	@SuppressWarnings("deprecation")
	public static boolean useNamespacesFromClasspath(IProject project) {
		if (project == null) {
			return false;
		}

		if (SpringCorePreferences.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID).getBoolean(
				BeansCorePlugin.PROJECT_PROPERTY_ID, false)) {
			return SpringCorePreferences.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID).getBoolean(
					BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, false);
		}
		return BeansCorePlugin.getDefault().getPluginPreferences().getBoolean(
				BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID);
	}

	private static Object loadHandler(String providerBundle, String handlerClassName) {
		Bundle bundle = Platform.getBundle(providerBundle);
		if (bundle != null) {
			try {
				Class<?> handlerClass = bundle.loadClass(handlerClassName);
				return BeanUtils.instantiate(handlerClass);
			}
			catch (ClassNotFoundException e) {
				BeansCorePlugin.log(new Status(IStatus.WARNING, BeansCorePlugin.PLUGIN_ID, 1, String.format(
						"Problem loading handler with class '%s' from bundle '%s'", handlerClassName, providerBundle),
						e));
			}
		}
		else {
			BeansCorePlugin.log(new Status(IStatus.WARNING, BeansCorePlugin.PLUGIN_ID, String.format(
					"Problem loading handler with class '%s' from bundle '%s'", handlerClassName, providerBundle)));
		}
		return null;
	}

	public static class NamespaceHandlerDescriptor {

		private final String namespaceUri;

		private final String schemaLocation;

		NamespaceHandlerDescriptor(String namespaceUri, String schemaLocation) {
			this.namespaceUri = namespaceUri;
			this.schemaLocation = schemaLocation;
		}

		public static NamespaceHandlerDescriptor createNamespaceHandlerDescriptor(String namespaceUri,
				String schemaLocation) {
			if (StringUtils.hasText(schemaLocation)) {
				return new NamespaceHandlerDescriptor(namespaceUri, schemaLocation);
			}
			return new NamespaceHandlerDescriptor(namespaceUri, "*");
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof NamespaceHandlerDescriptor)) {
				return false;
			}
			NamespaceHandlerDescriptor other = (NamespaceHandlerDescriptor) obj;
			return namespaceUri.equals(other.namespaceUri) && schemaLocation.equals(other.schemaLocation);
		}

		@Override
		public int hashCode() {
			return 31 ^ ObjectUtils.nullSafeHashCode(namespaceUri) * ObjectUtils.nullSafeHashCode(schemaLocation);
		}
	}
}
