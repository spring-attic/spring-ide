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
package org.springframework.ide.eclipse.beans.core.model.metadata;

import java.util.Set;

import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * The global {@link IBeanMetadata} model to retrieve {@link IBeanMetadata} for
 * {@link IBean}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeanMetadataModel {
	
	/**
	 * Returns the {@link IBeanMetadata}s for the given {@link IBean}.
	 */
	Set<IBeanMetadata> getBeanMetaData(IBean bean);
	
	/**
	 * Set {@link IBeanMetadata}s for a certain {@link IBean}. 
	 */
	void setBeanMetaData(IBean bean, Set<IBeanMetadata> beanMetaData,
			Set<IMethodMetadata> methodMetaData);
	/**
	 * Clears the meta data for a certian {@link IBean}. 
	 */
	void clearBeanMetaData(IBean bean);
	
}
