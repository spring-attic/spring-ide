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

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.ObjectUtils;

/**
 * Holds a reference to an {@link IBean}'s by it's name.
 * 
 * @author Torsten Juergeleit
 */
public class BeanReference extends AbstractBeansModelElement implements
		IBeanReference {

	private String beanName;

	public BeanReference(ISourceModelElement parent,
			org.springframework.beans.factory.config.BeanReference beanRef) {
		super(parent, "(bean reference)", beanRef);
		beanName = beanRef.getBeanName();
	}

	public int getElementType() {
		return IBeansModelElementTypes.BEAN_REFERENCE_TYPE;
	}

	public String getBeanName() {
		return beanName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanReference)) {
			return false;
		}
		BeanReference that = (BeanReference) other;
		if (!ObjectUtils.nullSafeEquals(this.beanName, that.beanName)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(beanName);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": name=");
		text.append(beanName);
		return text.toString();
	}
}
