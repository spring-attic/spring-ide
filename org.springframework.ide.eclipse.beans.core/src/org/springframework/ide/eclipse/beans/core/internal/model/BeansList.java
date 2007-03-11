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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * Holds a managed {@link List}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansList extends AbstractBeansModelElement implements IBeansList {

	private List<Object> list;

	public BeansList(IModelElement parent, ManagedList list) {
		super(parent, "(list)", list);

		// Create new list with values from given list
		this.list = new ArrayList<Object>();
		for (Object value : list) {
			this.list.add(BeansModelUtils.resolveValueIfNecessary(this, value));
		}
	}

	public int getElementType() {
		return IBeansModelElementTypes.LIST_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		for (Object value : list) {
			if (value instanceof IModelElement) {
				children.add((IModelElement) value);
			}
		}
		return children.toArray(new IModelElement[children.size()]);
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this list's entries
			for (Object value : list) {
				if (value instanceof IModelElement) {
					((IModelElement) value).accept(visitor, monitor);
					if (monitor.isCanceled()) {
						return;
					}
				}
			}
		}
	}

	public List<Object> getList() {
		return list;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansList)) {
			return false;
		}
		BeansList that = (BeansList) other;
		if (!ObjectUtils.nullSafeEquals(this.list, that.list)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(list);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": list=");
		text.append(list);
		return text.toString();
	}
}
