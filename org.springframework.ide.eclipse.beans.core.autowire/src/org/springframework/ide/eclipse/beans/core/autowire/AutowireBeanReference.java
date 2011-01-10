/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanReference;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Extension to {@link BeanReference} that takes source locations in the form of {@link IJavaElement}.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class AutowireBeanReference extends BeanReference {

	private IJavaElement source;

	private ISourceModelElement parent;

	private int parameterIndex = -1;

	public AutowireBeanReference(ISourceModelElement parent,
			org.springframework.beans.factory.config.BeanReference beanRef) {
		super(parent, beanRef);
		this.parent = parent;
	}

	public void setSource(Field field) {
		setSource(field, -1);
	}

	public void setSource(Member member, int index) {
		IResource resource = BeansModelUtils.getParentOfClass(parent, IResourceModelElement.class).getElementResource();
		source = AutowireUtils.getJavaElement(resource, member, index);
		if (source != null) {
			try {
				this.parameterIndex = index;
				setElementSourceLocation(new JavaModelSourceLocation(source));
			}
			catch (JavaModelException e) {

			}
		}
	}

	public int getParameterIndex() {
		return parameterIndex;
	}

	public IJavaElement getSource() {
		return source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 31 ^ ObjectUtils.nullSafeHashCode(source) * parameterIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}
		return (source.equals(((AutowireBeanReference) other).source) && parameterIndex == ((AutowireBeanReference) other).parameterIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + " [" + source + "]";
	}
}
