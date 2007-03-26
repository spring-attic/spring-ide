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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.ObjectUtils;

/**
 * Holder for a connection betweena {@link IModelElement} and an {@link IBean}
 * within a certain context ({@link IBeansConfig} or ({@link IBeansConfigSet}).
 * 
 * @author Torsten Juergeleit
 */
public class BeansConnection {

	public enum BeanType {
		STANDARD, PARENT, FACTORY, DEPENDS_ON, METHOD_OVERRIDE, INTERCEPTOR,
		INNER
	}
	private BeanType type;
	private IModelElement source;
	private IBean target;
	private IModelElement context;

	public BeansConnection(BeanType type, IModelElement source,
			IBean target) {
		this(BeanType.STANDARD, source, target, target
				.getElementParent());
	}

	public BeansConnection(BeanType type, IModelElement source,
			IBean target, IModelElement context) {
		this.type = type;
		this.source = source;
		this.target = target;
		this.context = context;
	}

	public final BeanType getType() {
		return type;
	}

	public final IModelElement getSource() {
		return source;
	}

	public final IBean getTarget() {
		return target;
	}

	public IModelElement getContext() {
		return context;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansConnection)) {
			return false;
		}
		BeansConnection that = (BeansConnection) other;
		if (!ObjectUtils.nullSafeEquals(this.type, that.type))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.source, that.source))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.target, that.target))
			return false;
		return ObjectUtils.nullSafeEquals(this.context, that.context);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(type);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(source);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(target);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(context);
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(type.toString());
		text.append(": ").append(source).append(" -> ").append(target);
		text.append(" @ ").append(context);
		return text.toString();
	}
}
