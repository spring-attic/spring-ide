/*
 * Copyright 2002-2006 the original author or authors.
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
					}
				}
			}
		}
		return null;
	}

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
						try {
							Object provider = config
								.createExecutableExtension("contentProvider");
							if (provider instanceof IContentProvider) {
								return (ITreeContentProvider) provider;
							}
						} catch (CoreException e) {
							BeansUIPlugin.log(e);
						}
					}
				}
			}
		}
		return null;
	}
}
