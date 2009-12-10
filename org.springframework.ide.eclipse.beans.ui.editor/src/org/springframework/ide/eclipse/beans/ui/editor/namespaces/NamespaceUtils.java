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

import java.lang.reflect.Array;
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
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IAnnotationBasedContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IAnnotationBasedHyperlinkDetector;
import org.w3c.dom.Element;

/**
 * Utility class that loads contributions to the
 * <code>org.springframework.ide.eclipse.beans.ui.editor.namespaces</code> extension point.
 * @author Christian Dupuis
 */
public class NamespaceUtils {

	public static final String DEFAULT_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	public static final String EXTENSION_POINT = Activator.PLUGIN_ID + ".namespaces";

	public static IClassNameProvider[] getClassNameProvider(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "classNameProvider", IClassNameProvider.class);
	}

	public static INamespaceContentAssistProcessor[] getContentAssistProcessor(String namespaceUri) {
		INamespaceContentAssistProcessor[] processors = getExecutableExtension(namespaceUri,
				"contentAssistProcessor", INamespaceContentAssistProcessor.class);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.init();
		}
		return processors;
	}

	/**
	 * Returns the registered {@link IAnnotationBasedContentAssistProcessor} for the given namespace
	 * uri.
	 * @since 2.0.3
	 */
	public static IAnnotationBasedContentAssistProcessor[] getAnnotationBasedContentAssistProcessor(
			String namespaceUri) {
		IAnnotationBasedContentAssistProcessor[] processors = getExecutableExtension(namespaceUri,
				"contentAssistProcessor", IAnnotationBasedContentAssistProcessor.class);
		for (IAnnotationBasedContentAssistProcessor processor : processors) {
			processor.init();
		}
		return processors;
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
	private static <T> T[] getExecutableExtension(String namespaceUri, String attributeName,
			Class<T> requiredType) {
		namespaceUri = checkNamespaceUri(namespaceUri);
		List extensions = new ArrayList();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceUri.equals(config.getAttribute("uri"))) {
						try {
							if (config.getAttribute(attributeName) != null) {
								extensions.add((T) config.createExecutableExtension(attributeName));
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
		return (T[]) extensions.toArray((T[]) Array.newInstance(requiredType, extensions.size()));
	}

	public static IHyperlinkDetector[] getHyperlinkDetector(String namespaceUri) {
		IHyperlinkDetector[] detectors = getExecutableExtension(namespaceUri, "hyperLinkDetector",
				IHyperlinkDetector.class);
		for (IHyperlinkDetector detector : detectors) {
			if (detector instanceof INamespaceHyperlinkDetector) {
				((INamespaceHyperlinkDetector) detector).init();
			}
		}
		return detectors;
	}

	public static IAnnotationBasedHyperlinkDetector[] getAnnotationBasedHyperlinkDetector(
			String namespaceUri) {
		IAnnotationBasedHyperlinkDetector[] detectors = getExecutableExtension(namespaceUri,
				"hyperLinkDetector", IAnnotationBasedHyperlinkDetector.class);
		for (IAnnotationBasedHyperlinkDetector detector : detectors) {
			if (detector instanceof INamespaceHyperlinkDetector) {
				((INamespaceHyperlinkDetector) detector).init();
			}
		}
		return detectors;
	}

	public static ILabelProvider[] getLabelProvider(String namespaceUri) {
		return getExecutableExtension(namespaceUri, "labelProvider", ILabelProvider.class);
	}

	public static String getNamespaceUri(Element element) {
		String namespaceURI = element.getNamespaceURI();
		if (namespaceURI == null) {
			namespaceURI = DEFAULT_NAMESPACE_URI;
		}
		return namespaceURI;
	}

	public static String checkNamespaceUri(String namespaceUri) {
		if (namespaceUri == null) {
			namespaceUri = DEFAULT_NAMESPACE_URI;
		}
		return namespaceUri;
	}
}
