/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata.model;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Extension point to be implemented if third parties want to contribute {@link IBeanMetadata} or {@link IBeanProperty}s
 * to the Spring IDE core model.
 * <p>
 * Note: contributions made over this extension are <b>not</b> stored in the core model and will be terminated after
 * <b>10</b> seconds.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeanMetadataProvider {

	/**
	 * Contribute {@link IBeanMetadata} for a given {@link IBean} in the context of an {@link IBeansConfig}.
	 * <p>
	 * Note: implementations of this method are not allowed to return <code>null</code>.
	 * @param bean the current {@link IBean} to search for bean meta data
	 * @param beansConfig the current {@link IBeansConfig}
	 * @param progressMonitor a {@link IProgressMonitor} to report progress
	 * @return {@link Set} of {@link IBeanMetadata}
	 */
	Set<IBeanMetadata> provideBeanMetadata(IBean bean, IBeansConfig beansConfig, IProgressMonitor progressMonitor);

	/**
	 * Contribute {@link IBeanProperty} for a given {@link IBean} in the context of an {@link IBeansConfig}.
	 * <p>
	 * Note: implementations of this method are not allowed to return <code>null</code>.
	 * @param bean the current {@link IBean} to search for bean meta data
	 * @param beansConfig the current {@link IBeansConfig}
	 * @param progressMonitor a {@link IProgressMonitor} to report progress
	 * @return {@link Set} of {@link IBeanProperty}
	 */
	Set<IBeanProperty> provideBeanProperties(IBean bean, IBeansConfig beansConfig, IProgressMonitor progressMonitor);

}
