/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.MethodOverride;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanMethodOverride;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.util.ObjectUtils;

/**
 * Holds the data of an {@link IBean}'s method override.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class AbstractBeanMethodOverride extends
		AbstractBeansValueHolder implements IBeanMethodOverride {

	private final String methodName;

	private final String beanName;
	
	private final MethodOverride methodOverride;

	public AbstractBeanMethodOverride(IBean bean, String beanName,
			MethodOverride methodOverride) {
		super(bean, createName(methodOverride), new RuntimeBeanReference(
				beanName), methodOverride);
		this.methodName = methodOverride.getMethodName();
		this.beanName = beanName;
		this.methodOverride = methodOverride;
	}

	public int getElementType() {
		return IBeansModelElementTypes.METHOD_OVERRIDE_TYPE;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeanMethodOverride)) {
			return false;
		}
		AbstractBeanMethodOverride that = (AbstractBeanMethodOverride) other;
		if (!ObjectUtils.nullSafeEquals(this.methodName, that.methodName))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.getBeanName(), that.getBeanName()))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(methodName);
		hashCode = getElementType() * hashCode
				+ ObjectUtils.nullSafeHashCode(getBeanName());
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(", methodName=");
		text.append(methodName);
		text.append(", bean=");
		text.append(getBeanName());
		return text.toString();
	}

	protected static final String createName(MethodOverride methodOverride) {
		StringBuffer buf = new StringBuffer();
		if (methodOverride.getMethodName() != null) {
			buf.append(methodOverride.getMethodName());
		}
		return buf.toString();
	}

	public String getMethodName() {
		return methodName;
	}

	public String getBeanName() {
		return beanName;
	}

	public MethodOverride getMethodOverride() {
		return methodOverride;
	}
}
