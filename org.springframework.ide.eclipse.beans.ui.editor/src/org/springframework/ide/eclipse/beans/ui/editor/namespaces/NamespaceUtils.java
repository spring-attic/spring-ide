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

	public static final String EXTENSION_POINT = Activator.PLUGIN_ID
			+ ".namespaces";

	public static IClassNameProvider getClassNameProvider(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "classNameProvider",
				IClassNameProvider.class);
	}

	public static INamespaceContentAssistProcessor getContentAssistProcessor(
			String namespaceUri) {
		INamespaceContentAssistProcessor processor = getExecutableExtension(
				namespaceUri, "contentAssistProcessor",
				INamespaceContentAssistProcessor.class);
		processor.init();
		return processor;
	}

	public static IReferenceableElementsLocator getElementsLocator(
			String namespaceUri) {
		return getExecutableExtension(namespaceUri, "elementLocator",
				IReferenceableElementsLocator.class);
	}

	public static List<IReferenceableElementsLocator> getAllElementsLocators() {
		List<IReferenceableElementsLocator> locators = new ArrayList<IReferenceableElementsLocator>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					try {
						if (config.getAttribute("elementLocator") != null) {
							locators
									.add(((IReferenceableElementsLocator) config
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
	private static <T> T getExecutableExtension(String namespaceUri,
			String attributeName, Class<T> requiredType) {
		namespaceUri = checkNameSpaceUri(namespaceUri);
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension
						.getConfigurationElements()) {
					if (namespaceUri.equals(config.getAttribute("uri"))) {
						try {
							if (config.getAttribute(attributeName) != null) {
								return (T) config
										.createExecutableExtension(attributeName);
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
		IHyperlinkDetector detector = getExecutableExtension(namespaceUri,
				"hyperLinkDetector", IHyperlinkDetector.class);
		if (detector instanceof INamespaceHyperlinkDetector) {
			((INamespaceHyperlinkDetector) detector).init();
		}
		return detector;
	}

	public static ILabelProvider getLabelProvider(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "labelProvider",
				ILabelProvider.class);
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
