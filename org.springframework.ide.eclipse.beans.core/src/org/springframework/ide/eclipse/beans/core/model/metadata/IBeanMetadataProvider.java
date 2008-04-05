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

import org.eclipse.core.runtime.IProgressMonitor;
import org.objectweb.asm.ClassReader;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * Extension point to be implemented if third parties want to contribute
 * {@link IBeanMetadata} to the Spring IDE core model.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeanMetadataProvider {

	/**
	 * Contribute {@link IBeanMetadata} for a given {@link IBean} in the context
	 * of an {@link IBeansConfig}.
	 * <p>
	 * Note: implementations of this method are not allowed to return
	 * <code>null</code>.
	 * @param bean the current {@link IBean} to search for bean meta data
	 * @param beansConfig the current {@link IBeansConfig}
	 * @param progressMonitor a {@link IProgressMonitor} to report progress
	 * @param classReaderFactory the ASM {@link ClassReaderFactory} to use for
	 * loading {@link ClassReader}.
	 * @return {@link Set} of {@link IBeanMetadata}
	 */
	Set<IBeanMetadata> provideBeanMetadata(IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor, ClassReaderFactory classReaderFactory);

}
