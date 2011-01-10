/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.util.ObjectUtils;

/**
 * Holder for information about the references from Spring beans to a bean
 * class.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeanClassReferences {

	private IType beanClass;
	private Set<IBean> beans;

	public BeanClassReferences(IType beanClass, Set<IBean> beans) {
		this.beanClass = beanClass;
		this.beans = beans;
	}

	public final IType getBeanClass() {
		return beanClass;
	}

	public final Set<IBean> getBeans() {
		return beans;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanClassReferences)) {
			return false;
		}
		BeanClassReferences that = (BeanClassReferences) other;
		if (!ObjectUtils.nullSafeEquals(this.beanClass, that.beanClass))
			return false;
		return ObjectUtils.nullSafeEquals(this.beans, that.beans);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(beanClass);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(beans);
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(": ");
		text.append(beanClass).append(" <- ").append(beans);
		return text.toString();
	}
}
