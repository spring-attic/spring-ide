/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.metadata.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataProvider;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;

/**
 * {@link IBeanMetadataProvider} that simply delegates the processing to the contributed
 * {@link IAnnotationBeanMetadataProvider}s.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class DelegatingAnnotationReadingMetadataProvider extends
		AbstractAnnotationReadingMetadataProvider {

	/** The class attribute in the extension point contribution */
	private static final String CLASS_ATTRIBUTE = "class";
	
	/** The metadataProvider element in the extension point contribution */
	private static final String ANNOTATION_METADATA_PROVIDER_ELEMENT = "annotationMetadataProvider";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processFoundAnnotations(IBean bean, Set<IBeanMetadata> beanMetaDataSet,
			IType type, IAnnotationMetadata metadata, IProgressMonitor progressMonitor) {
		for (IAnnotationBeanMetadataProvider provider : getMetadataProviders()) {
			if (progressMonitor.isCanceled()) {
				return;
			}
			beanMetaDataSet.addAll(provider.provideBeanMetadata(bean, type, metadata));
		}
	}

	/**
	 * Returns the contributed {@link IAnnotationBeanMetadataProvider} from the Eclipse extension
	 * registry.
	 */
	protected IAnnotationBeanMetadataProvider[] getMetadataProviders() {
		List<IAnnotationBeanMetadataProvider> providers = new ArrayList<IAnnotationBeanMetadataProvider>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				BeanMetadataBuilderJob.META_DATA_PROVIDERS_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (ANNOTATION_METADATA_PROVIDER_ELEMENT.equals(config.getName())
							&& config.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							Object handler = config.createExecutableExtension(CLASS_ATTRIBUTE);
							if (handler instanceof IAnnotationBeanMetadataProvider) {
								IAnnotationBeanMetadataProvider entityResolver = (IAnnotationBeanMetadataProvider) handler;
								providers.add(entityResolver);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		return providers.toArray(new IAnnotationBeanMetadataProvider[providers.size()]);
	}

}
