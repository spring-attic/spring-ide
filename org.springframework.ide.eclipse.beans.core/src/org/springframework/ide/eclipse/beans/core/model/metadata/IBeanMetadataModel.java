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

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;

/**
 * The global {@link IBeanMetadata} model to retrieve {@link IBeanMetadata} for
 * {@link IBean}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
/**
 * TODO CD this interface needs methods to provide {@link ConstructorArgumentValues} as well.
 */
public interface IBeanMetadataModel {
	
	/**
	 * Returns the {@link IBeanMetadata}s for the given {@link IBean}.
	 */
	Set<IBeanMetadata> getBeanMetadata(IBean bean);
	
	/**
	 * Set {@link IBeanMetadata}s for a certain {@link IBean}. 
	 */
	void setBeanMetadata(IBean bean, Set<IBeanMetadata> beanMetadata,
			Set<IMethodMetadata> methodMetadata);
	/**
	 * Clears the meta data for a certain {@link IBean}. 
	 */
	void clearBeanMetadata(IBean bean);

	/**
	 * Returns the {@link IBeanProperty}s for the given {@link IBean}.
	 */
	Set<IBeanProperty> getBeanProperties(IBean bean);
	
	/**
	 * Set {@link IBeanProperty}s for a certain {@link IBean}. 
	 */
	void setBeanProperties(IBean bean, Set<IBeanProperty> beanProperties);
	
	/**
	 * Clears the meta data for a certain {@link IBean}. 
	 */
	void clearBeanProperties(IBean bean);
	
}
