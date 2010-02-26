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
package org.springframework.ide.eclipse.beans.ui.model.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.beans.core.metadata.BeansMetadataPlugin;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * Helper methods to load external contributed
 * {@link IBeanMetadataContentProvider} and {@link IBeanMetadataLabelProvider}
 * for linking in third-party contributed bean meta data.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataUtils {

	private static final String LABEL_PROVIDER_ATTRIBUTE = "labelProvider";

	private static final String CONTENT_PROVIDER_ATTRIBUTE = "contentProvider";

	private static final String METADATA_PROVIDERS_ELEMENT = "metadataProviders";

	public static final String META_DATA_PROVIDERS_EXTENSION_POINT = BeansUIPlugin.PLUGIN_ID
			+ ".metadataproviders";

	public static IBeanMetadataContentProvider getContenProvider(IBeanMetadata metaDataReference) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				META_DATA_PROVIDERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (METADATA_PROVIDERS_ELEMENT.equals(config.getName())
							&& config.getAttribute(CONTENT_PROVIDER_ATTRIBUTE) != null) {
						try {
							Object handler = config
									.createExecutableExtension(CONTENT_PROVIDER_ATTRIBUTE);
							if (handler instanceof IBeanMetadataContentProvider) {
								IBeanMetadataContentProvider provider = (IBeanMetadataContentProvider) handler;
								if (provider.supports(metaDataReference)) {
									return provider;
								}
							}
						}
						catch (CoreException e) {
							BeansUIPlugin.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	public static IBeanMetadataLabelProvider getLabelProvider(
			BeanMetadataReference metaDataReference) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				META_DATA_PROVIDERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (METADATA_PROVIDERS_ELEMENT.equals(config.getName())
							&& config.getAttribute(LABEL_PROVIDER_ATTRIBUTE) != null) {
						try {
							Object handler = config
									.createExecutableExtension(LABEL_PROVIDER_ATTRIBUTE);
							if (handler instanceof IBeanMetadataLabelProvider) {
								IBeanMetadataLabelProvider provider = (IBeanMetadataLabelProvider) handler;
								if (provider.supports(metaDataReference)) {
									return provider;
								}
							}
						}
						catch (CoreException e) {
							BeansUIPlugin.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	public static IBeanMetadataLabelProvider getLabelProvider(IBeanMetadata metaData) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				META_DATA_PROVIDERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (METADATA_PROVIDERS_ELEMENT.equals(config.getName())
							&& config.getAttribute(LABEL_PROVIDER_ATTRIBUTE) != null) {
						try {
							Object handler = config
									.createExecutableExtension(LABEL_PROVIDER_ATTRIBUTE);
							if (handler instanceof IBeanMetadataLabelProvider) {
								IBeanMetadataLabelProvider provider = (IBeanMetadataLabelProvider) handler;
								if (provider.supports(metaData)) {
									return provider;
								}
							}
						}
						catch (CoreException e) {
							BeansUIPlugin.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	public static Collection<? extends Object> getProjectChildren(IBeansProject project) {
		Map<String, BeanMetadataReference> metaDataMapping = new HashMap<String, BeanMetadataReference>();
		// add meta data grouping
		for (IBeansConfig beansConfig : project.getConfigs()) {
			// add beans from config
			for (IBean bean : beansConfig.getBeans()) {
				addMetaDataForBean(project, metaDataMapping, bean);
			}

			// add nested component beans; required for component scan
			for (IBeansComponent beansComponent : beansConfig.getComponents()) {
				for (IBean bean : beansComponent.getBeans()) {
					addMetaDataForBean(project, metaDataMapping, bean);
				}
			}
		}
		return metaDataMapping.values();
	}

	private static void addMetaDataForBean(IBeansProject project,
			Map<String, BeanMetadataReference> metaDataMapping, IBean bean) {
		for (IBeanMetadata metaData : BeansMetadataPlugin.getMetadataModel().getBeanMetadata(bean)) {
			if (!metaDataMapping.containsKey(metaData.getKey())) {
				metaDataMapping.put(metaData.getKey(), getContenProvider(metaData)
						.getBeanMetadataReference(metaData, project));
			}
			metaDataMapping.get(metaData.getKey()).addChild(metaData);
		}
	}

}
