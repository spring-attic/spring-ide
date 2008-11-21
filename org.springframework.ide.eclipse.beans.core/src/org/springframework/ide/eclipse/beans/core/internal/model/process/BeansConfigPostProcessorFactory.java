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
package org.springframework.ide.eclipse.beans.core.internal.model.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessingContext;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;

/**
 * Internal factory for creating instances of {@link IBeansConfigPostProcessor} and
 * {@link BeansConfigPostProcessingContext}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansConfigPostProcessorFactory {

	public static final String POSTPROCESSOR_EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID
			+ ".postprocessors";

	/**
	 * Returns an new instance of {@link IBeansConfigPostProcessor} that is configured to match the
	 * given <code>type</code>.
	 * @param type the Spring {@link BeanFactoryPostProcessor} or {@link BeanPostProcessor}
	 * implementation
	 * @return the beansConfigPostProcessor
	 */
	public static IBeansConfigPostProcessor[] createPostProcessor(String type) {
		Set<IBeansConfigPostProcessor> postProcessors = new HashSet<IBeansConfigPostProcessor>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				POSTPROCESSOR_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					String extensionType = config.getAttribute("type");
					if ((type == null && extensionType == null)
							|| (type != null && type.equals(extensionType))) {
						try {
							Object object = config.createExecutableExtension("class");
							if (object instanceof IBeansConfigPostProcessor) {
								IBeansConfigPostProcessor postProcessor = (IBeansConfigPostProcessor) object;
								postProcessors.add(postProcessor);
							}
						}
						catch (CoreException e) {
							BeansCorePlugin.log(e);
						}
					}
				}
			}
		}
		return postProcessors.toArray(new IBeansConfigPostProcessor[postProcessors.size()]);
	}

	/**
	 * Helper method to create a new {@link IBeansConfigPostProcessingContext}.
	 */
	public static IBeansConfigPostProcessingContext createPostProcessingContext(
			IBeansConfig beansConfig, Collection<IBean> beans,
			ReaderEventListener readerEventListener, ProblemReporter problemReporter,
			BeanNameGenerator beanNameGenerator) {
		List<IBean> beansClone = new ArrayList<IBean>();
		beansClone.addAll(beans);
		return new BeansConfigPostProcessingContext(beansConfig, beanNameGenerator,
				problemReporter, new BeansConfigRegistrationSupport(Collections
						.unmodifiableCollection(beansClone), readerEventListener));
	}
}
