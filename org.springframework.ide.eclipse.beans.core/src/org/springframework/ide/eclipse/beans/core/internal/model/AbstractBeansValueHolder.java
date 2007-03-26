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

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansValueHolder;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.ObjectUtils;

/**
 * Holds a resolvable value. This value is resolved on first access by using
 * {@link BeansModelUtils#resolveValueIfNecessary(ISourceModelElement, Object)}.
 * 
 * @author Torsten Juergeleit
 */
public abstract class AbstractBeansValueHolder extends
		AbstractBeansModelElement implements IBeansValueHolder {

	private boolean isResolved = false;
	private Object value;

	protected AbstractBeansValueHolder(IModelElement parent, String name,
			Object value, BeanMetadataElement metadata) {
		super(parent, name, metadata);
		this.value = value;
	}

	@Override
	public IModelElement[] getElementChildren() {
		Object val = getValue();
		if (val instanceof IModelElement) {
			return new IModelElement[] { (IModelElement) val };
		}
		return NO_CHILDREN;
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this holders's value
			if (value instanceof IModelElement) {
				((IModelElement) value).accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		if (!isResolved) {
			value = BeansModelUtils.resolveValueIfNecessary(this, value);
		}
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeansValueHolder)) {
			return false;
		}
		AbstractBeansValueHolder that = (AbstractBeansValueHolder) other;
		if (!ObjectUtils.nullSafeEquals(this.getValue(), that.getValue()))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getValue());
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": value=");
		text.append(getValue());
		return text.toString();
	}
}
