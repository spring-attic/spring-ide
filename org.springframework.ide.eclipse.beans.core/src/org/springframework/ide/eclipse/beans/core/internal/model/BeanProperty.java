/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.PropertyValue;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;

/**
 * Holds the data of an {@link IBean}'s property.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeanProperty extends AbstractBeansValueHolder implements
		IBeanProperty {

	public BeanProperty(IBean bean, PropertyValue propValue) {
		super(bean, propValue.getName(), propValue.getValue(), propValue);
	}

	public int getElementType() {
		return IBeansModelElementTypes.PROPERTY_TYPE;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanProperty)) {
			return false;
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return getElementType() + super.hashCode();
	}
}
