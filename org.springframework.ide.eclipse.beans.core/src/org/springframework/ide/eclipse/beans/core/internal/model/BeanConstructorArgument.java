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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;

public class BeanConstructorArgument extends BeansModelElement
											   implements IBeanConstructorArgument {
	private int index;
	private String type;
	private Object value;
	private int startLine;

	public BeanConstructorArgument(IBean bean, int index, String type,
							   Object value) {
		super(bean, buildName(index, type,value));
		this.index = index;
		this.type = type;
		this.value = value;
	}

	public int getElementType() {
		return CONSTRUCTOR_ARGUMENT;
	}

	public IResource getElementResource() {
		return getElementParent().getElementResource();
	}

	public int getIndex() {
		return index;
	}

	public String getType() {
		return type;
	}
	
	public Object getValue() {
		return value;
	}

	/**
	 * Returns a collection of all <code>Bean</code>s which are referenced from
	 * within this constructor argument's value.
	 */
	public Collection getReferencedBeans() {
		Map beans = new HashMap();
		BeansModelUtil.addReferencedBeansForValue(this, value, beans);
		return beans.values();
	}

	private static String buildName(int index, String type, Object value) {
		StringBuffer name = new StringBuffer();
		if (index != -1) {
			name.append(index);
			name.append(':');
		}
		if (type != null) {
			name.append(type);
			name.append(':');
		}
		name.append(value.toString());
		return name.toString();
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getElementName());
		text.append(" (");
		text.append(getElementStartLine());
		text.append("): index=");
		text.append(index);
		text.append(", type=");
		text.append(type);
		text.append(", value=");
		text.append(value);
		return text.toString();
	}
}
