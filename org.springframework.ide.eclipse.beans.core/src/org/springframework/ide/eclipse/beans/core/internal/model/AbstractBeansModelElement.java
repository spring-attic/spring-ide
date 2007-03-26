/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * Default implementation of the common protocol for all {@link IModelElement}s
 * related to source code retrievable from Spring's {@link BeanMetadataElement}
 * interface.
 * 
 * @author Torsten Juergeleit
 */
public abstract class AbstractBeansModelElement extends
		AbstractSourceModelElement {

	protected AbstractBeansModelElement(IModelElement parent, String name,
			BeanMetadataElement metadata) {
		super(parent, name, ModelUtils.getSourceLocation(metadata));
	}

	protected AbstractBeansModelElement(IModelElement parent, String name,
			IModelSourceLocation location) {
		super(parent, name, location);
	}
}
