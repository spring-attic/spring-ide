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

import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Extension interface to provide elements to the Spring and Project Explorer
 * based on {@link IBeanMetadata}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeanMetadataContentProvider {

	/**
	 * Returns <code>true</code> if the <code>object</code> is supported by
	 * this implementation.
	 */
	boolean supports(Object object);

	/**
	 * Returns <code>true</code> if the <code>element</code> has children to
	 * be displayed.
	 */
	boolean hasChildren(Object element);
	
	/**
	 * Returns  the children of the <code>element</code>.
	 */
	Object[] getChildren(Object element);
	
	/**
	 * Returns the root node of the bean meta data extension. 
	 */
	BeanMetadataReference getBeanMetadataReference(IBeanMetadata metadata, IBeansProject project);
	
}
