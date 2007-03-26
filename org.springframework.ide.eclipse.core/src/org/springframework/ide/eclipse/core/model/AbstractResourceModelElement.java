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
package org.springframework.ide.eclipse.core.model;

import org.springframework.util.ObjectUtils;

/**
 * Default implementation of the common protocol for all model elements that map
 * to a resource in the Eclipse workspace.
 * 
 * @author Torsten Juergeleit
 */
public abstract class AbstractResourceModelElement extends AbstractModelElement
		implements IResourceModelElement {

	protected AbstractResourceModelElement(IModelElement parent, String name) {
		super(parent, name);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractResourceModelElement)) {
			return false;
		}
		AbstractResourceModelElement that = (AbstractResourceModelElement)
				other;
		if (!ObjectUtils.nullSafeEquals(this.getElementResource(), that
				.getElementResource())) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getElementResource());
		return getElementType() * hashCode + super.hashCode();
	}
}
