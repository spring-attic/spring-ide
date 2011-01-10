/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata.model;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Empty implementation of the {@link IBeanMetadataProvider} interface allowing subclasses to just
 * override on of both interface methods if desired.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataProviderAdapter implements IBeanMetadataProvider {

	/**
	 * {@inheritDoc}
	 */
	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor) {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IBeanProperty> provideBeanProperties(IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor) {
		return Collections.emptySet();
	}

}
