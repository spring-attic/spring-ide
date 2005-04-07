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
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.AbstractLocatableModelElement;
import org.springframework.ide.eclipse.core.model.ILocatableModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;

public class BeanConstructorArgument extends AbstractLocatableModelElement
										  implements IBeanConstructorArgument {
	private int index;
	private String type;
	private Object value;

	public BeanConstructorArgument(IBean bean) {
		super(bean, null);
	}

	public int getElementType() {
		return IBeansModelElementTypes.CONSTRUCTOR_ARGUMENT;
	}

	public IResource getElementResource() {
		if (getElementParent() instanceof ILocatableModelElement) {
			return ((ILocatableModelElement)
									  getElementParent()).getElementResource();
		}
		return null;
	}

	public void accept(IModelElementVisitor visitor) {
		visitor.visit(this);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * Returns a collection with the names of all beans which are referenced
	 * by this constructor argument's value.
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
		text.append("): index=");
		text.append(index);
		text.append(", type=");
		text.append(type);
		text.append(", value=");
		text.append(value);
		return text.toString();
	}
}
