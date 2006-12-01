/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Holder for information about a reference from a model element to to a Spring
 * bean within a certain context (<code>IBeansConfig</code> or
 * <code>IBeansConfigSet</code>).
 * @author Torsten Juergeleit
 */
public class BeanReference {

	public enum BeanType {
		STANDARD, PARENT, FACTORY, DEPENDS_ON, METHOD_OVERRIDE, INTERCEPTOR,
		INNER
	}
	private IModelElement source;
	private IBean target;
	private BeanType type;
	private IModelElement context;

	public BeanReference(BeanType type, IModelElement source, IBean target) {
		this(BeanType.STANDARD, source, target, target.getElementParent());
	}

	public BeanReference(BeanType type, IModelElement source, IBean target,
			IModelElement context) {
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

	/**
	 * Returns the unique ID of this bean references. The ID is built from the
	 * type, the source's ID and the target's ID delimited by '|'.
	 */
	public final String getID() {
		StringBuffer id = new StringBuffer();
		id.append(type);
		id.append('|');
		if (source != null) {
			id.append(source.getElementID());
		}
		id.append('|');
		if (target != null) {
			id.append(target.getElementID());
		}
		id.append('|');
		id.append(context.getElementID());
		return id.toString();
	}

	/**
	 * Checks for model element equality by comparing the types, the sources
	 * and the targets.
	 */
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof BeanReference) {
			return ((BeanReference) obj).getType() == getType()
					&& ((BeanReference) obj).getSource().equals(source)
					&& ((BeanReference) obj).getTarget().equals(target)
					&& ((BeanReference) obj).getContext().equals(context);
		}
		return false;
	}

	/**
	 * Returns the hash code of this bean references's ID.
	 */
	public final int hashCode() {
		return getID().hashCode();
	}

	public String toString() {
		StringBuffer text = new StringBuffer(type.toString());
		text.append(": ");
		text.append(source);
		text.append(" -> ");
		text.append(target);
		text.append(" @ ");
		text.append(context);
		return text.toString();
	}
}
