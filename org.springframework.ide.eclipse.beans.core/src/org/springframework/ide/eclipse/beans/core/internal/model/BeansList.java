/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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

	public BeansList(IModelElement parent, ManagedList<?> list) {
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
		if (!ObjectUtils.nullSafeEquals(this.list, that.list))
			return false;
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
