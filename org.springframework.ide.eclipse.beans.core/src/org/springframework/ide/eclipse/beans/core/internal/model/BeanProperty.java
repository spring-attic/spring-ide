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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;

public class BeanProperty extends BeansModelElement implements IBeanProperty {

	private Object value;

	public BeanProperty(IBean bean) {
		super(bean, null);
	}

	public int getElementType() {
		return PROPERTY;
	}

	public IResource getElementResource() {
		return getElementParent().getElementResource();
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return this.value;
	}

	/**
	 * Returns a collection with the names of all beans which are referenced
	 * by this property's value.
	 */
	public Collection getReferencedBeans() {
		List beanNames = new ArrayList();
		BeansModelUtils.addReferencedBeanNamesForValue(this, value, beanNames);
		return beanNames;
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getElementName());
		text.append(" (");
		text.append(getElementStartLine());
		text.append("): value=");
		text.append(value);
		return text.toString();
	}
}
