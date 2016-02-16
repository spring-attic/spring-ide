/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.process;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Post processing mechanism that provides similar semantics than Spring's
 * {@link BeanFactoryPostProcessor} and {@link BeanPostProcessor}.
 * <p>
 * A certain {@link IBeansConfigPostProcessor} can be contributed to Spring IDE by using the
 * <pre>
 * org.springframework.ide.eclipse.beans.core.postprocessors
 * </pre>
 * extension point.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IBeansConfigPostProcessor {

	/**
	 * Post process the given {@link IBeansConfig} that is wrapped in the
	 * {@link IBeansConfigPostProcessingContext}.
	 */
	void postProcess(IBeansConfigPostProcessingContext postProcessingContext);

}
