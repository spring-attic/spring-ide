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

import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.util.ObjectUtils;

/**
 * Holds the data of an {@link IBean}'s constructor argument.
 * 
 * @author Torsten Juergeleit
 */
public class BeanConstructorArgument extends AbstractBeansValueHolder
		implements IBeanConstructorArgument {

	private int index;
	private String type;
	
	public BeanConstructorArgument(IBean bean, ValueHolder vHolder) {
		this(bean, -1, vHolder);
	}

	public BeanConstructorArgument(IBean bean, int index,
			ValueHolder vHolder) {
		super(bean, createName(index, vHolder), vHolder.getValue(), vHolder);
		this.index = index;
		this.type = vHolder.getType();
	}

	public int getElementType() {
		return IBeansModelElementTypes.CONSTRUCTOR_ARGUMENT_TYPE;
	}

	public int getIndex() {
		return index;
	}

	public String getType() {
		return type;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanConstructorArgument)) {
			return false;
		}
		BeanConstructorArgument that = (BeanConstructorArgument) other;
		if (!ObjectUtils.nullSafeEquals(this.index, that.index)) return false;
		if (!ObjectUtils.nullSafeEquals(this.type, that.type)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(index);
		hashCode = getElementType() * hashCode
				+ ObjectUtils.nullSafeHashCode(type);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(", index=");
		text.append(index);
		text.append(", type=");
		text.append(type);
		return text.toString();
	}

	protected static final String createName(int index, ValueHolder vHolder) {
		StringBuffer buf = new StringBuffer();
		if (index >= 0) {
			buf.append(index);
		}
		if (vHolder.getType() != null) {
			if (buf.length() > 0) {
				buf.append(" - ");
			}
			buf.append(vHolder.getType());
		}
		if (buf.length() == 0) {
			buf.append(BeansModelUtils.getValueName(vHolder.getValue()));
		}
		return buf.toString();
	}
}
