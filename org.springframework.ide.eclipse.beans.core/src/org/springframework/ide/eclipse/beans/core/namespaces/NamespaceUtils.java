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

package org.springframework.ide.eclipse.beans.core.namespaces;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;

/**
 * Some helper methods.
 * 
 * @author Torsten Juergeleit
 */
public class NamespaceUtils {

	public static final String NAMESPACES_EXTENSION_POINT = BeansCorePlugin
			.PLUGIN_ID + ".namespaces";

	public static final String DEFAULT_NAMESPACE_URI =
			"http://www.springframework.org/schema/beans"; 

	/**
	 * Returns the namespace URI for the given {@link BeanMetadataElement} or
	 * <code>"http://www.springframework.org/schema/beans"</code> if no
	 * namespace URI found.
	 */
	public static String getNameSpaceURI(BeanMetadataElement element) {
		IModelSourceLocation location = ModelUtils.getSourceLocation(element);
		if (location instanceof XmlSourceLocation) {
			String namespaceURI = ((XmlSourceLocation) location)
					.getNamespaceURI();
			if (namespaceURI != null) {
				return namespaceURI;
			}
		}
		return DEFAULT_NAMESPACE_URI;
	}

	/**
	 * Returns a {@link Map} with all registered {@link NamespaceHandler}s.
	 */
	public static Map<String, NamespaceHandler> getNamespaceHandlers() {
		Map<String, NamespaceHandler> handlers =
				new HashMap<String, NamespaceHandler>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					String uri = config.getAttribute("uri");
					if (uri != null
							&& config.getAttribute("namespaceHandler") != null) {
						try {
							Object handler = config.createExecutableExtension(
									"namespaceHandler");
							if (handler instanceof NamespaceHandler) {
								NamespaceHandler namespaceHandler = 
									(NamespaceHandler) handler;
								namespaceHandler.init();
								handlers.put(uri, namespaceHandler);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		return handlers;
	}

	/**
	 * Returns a {@link Map} with all registered {@link IModelElementProvider}s.
	 */
	public static Map<String, IModelElementProvider> getElementProviders() {
		Map<String, IModelElementProvider> providers =
				new HashMap<String, IModelElementProvider>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					String uri = config.getAttribute("uri");
					if (uri != null
							&& config.getAttribute("elementProvider") != null) {
						try {
							Object provider = config.createExecutableExtension(
									"elementProvider");
							if (provider instanceof IModelElementProvider) {
								providers.put(uri,
										(IModelElementProvider) provider);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		return providers;
	}
}
