/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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
		this(BeanType.STANDARD, source, target, (IModelElement) target
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

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(type);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(source);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(target);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(context);
	}

	public String toString() {
		StringBuffer text = new StringBuffer(type.toString());
		text.append(": ").append(source).append(" -> ").append(target);
		text.append(" @ ").append(context);
		return text.toString();
	}
}
