/*
 * Copyright 2002-2004 the original author or authors.
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
 * bean.
 */
public class BeanReference {

	public static final int STANDARD_BEAN_TYPE = 1;
	public static final int PARENT_BEAN_TYPE = 2;
	public static final int FACTORY_BEAN_TYPE = 3;
	public static final int DEPENDS_ON_BEAN_TYPE = 4;
	public static final int METHOD_OVERRIDE_BEAN_TYPE = 5;
	public static final int INTERCEPTOR_BEAN_TYPE = 6;

	private IModelElement source;
	private IBean target;
	private int type;

	public BeanReference(IModelElement source, IBean target) {
		this(STANDARD_BEAN_TYPE, source, target);
	}

	public BeanReference(int type, IModelElement source, IBean target) {
		this.type = type;
		this.source = source;
		this.target = target;
	}

	public final int getType() {
		return type;
	}

	public final IModelElement getSource() {
		return source;
	}

	public final IBean getTarget() {
		return target;
	}

	/**
	 * Returns the unique ID of this bean references. The ID is built from the
	 * type, the source's ID and the target's ID delimited by '|'.
	 */
	public final String getID() {
		StringBuffer id = new StringBuffer();
		id.append(id);
		id.append('|');
		if (source != null) {
			id.append(source);
		}
		id.append('|');
		if (target != null) {
			id.append(target);
		}
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
			return ((BeanReference) obj).getType() == getType() &&
				   ((BeanReference) obj).getSource().equals(source) &&
				   ((BeanReference) obj).getTarget().equals(target);
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
		StringBuffer text = new StringBuffer();
		text.append(type);
		text.append(": ");
		text.append(source);
		text.append(" -> ");
		text.append(target);
		return text.toString();
	}
}
