/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.model.builder;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;

/**
 * An {@link IAspectDefinitionBuilder} is used to create instances of {@link IAspectDefinition} from the given
 * <code>file</code>.
 * <p>
 * Implementations can leverage the given {@link IProjectClassLoaderSupport} to do code in the context of the current
 * weaving class loader, being a class loader that has access to the Eclipse project's class path.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IAspectDefinitionBuilder {

	/**
	 * Build {@link IAspectDefinition}s from the given <code>file</code>.
	 */
	void buildAspectDefinitions(List<IAspectDefinition> aspectInfos, IFile file,
			IProjectClassLoaderSupport classLoaderSupport, IDocumentFactory documentFactory);
}
