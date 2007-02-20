/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.viewers.ILabelProvider;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.w3c.dom.Element;

public class NamespaceUtils {

	public static final String DEFAULT_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	public static final String EXTENSION_POINT = Activator.PLUGIN_ID + ".namespaces";

	public static IClassNameProvider getClassNameProvider(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "classNameProvider", IClassNameProvider.class);
	}

	public static INamespaceContentAssistProcessor getContentAssistProcessor(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "contentAssistProcessor", INamespaceContentAssistProcessor.class);
	}

	public static IReferenceableElementsLocator getElementsLocator(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "elementLocator", IReferenceableElementsLocator.class);
	}

	public static List<IReferenceableElementsLocator> getAllElementsLocators() {
		List<IReferenceableElementsLocator> locators = new ArrayList<IReferenceableElementsLocator>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					try {
						if (config.getAttribute("elementLocator") != null) {
							locators.add(((IReferenceableElementsLocator) config
									.createExecutableExtension("elementLocator")));
						}
					}
					catch (Exception e) {
						BeansUIPlugin.log(e);
					}
				}
			}
		}
		return locators;
	}

	@SuppressWarnings("unchecked")
	private static <T> T getExecutableExtension(String namespaceUri, String attributeName, Class<T> requiredType) {
		namespaceUri = checkNameSpaceUri(namespaceUri);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceUri.equals(config.getAttribute("uri"))) {
						try {
							if (config.getAttribute(attributeName) != null) {
								return (T) config.createExecutableExtension(attributeName);
							}
						}
						catch (Exception e) {
							// log classcast to log file
							Activator.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	public static IHyperlinkDetector getHyperlinkDetector(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "hyperLinkDetector", IHyperlinkDetector.class);
	}

	public static ILabelProvider getLabelProvider(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "labelProvider", ILabelProvider.class);
	}

	public static String getNameSpaceUri(Element element) {
		String namespaceURI = element.getNamespaceURI();
		if (namespaceURI == null) {
			namespaceURI = DEFAULT_NAMESPACE_URI;
		}
		return namespaceURI;
	}

	public static String checkNameSpaceUri(String namespaceUri) {
		if (namespaceUri == null) {
			namespaceUri = DEFAULT_NAMESPACE_URI;
		}
		return namespaceUri;
	}
}
