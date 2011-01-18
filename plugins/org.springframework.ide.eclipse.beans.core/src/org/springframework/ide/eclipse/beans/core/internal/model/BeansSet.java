/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * Holds a managed {@link Set}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansSet extends AbstractBeansModelElement implements IBeansSet {

	private LinkedHashSet<Object> set;

	public BeansSet(IModelElement parent, ManagedSet<?> set) {
		super(parent, "(set)", set);

		// Create new list with values from given list
		this.set = new LinkedHashSet<Object>();
		for (Object value : set) {
			this.set.add(BeansModelUtils.resolveValueIfNecessary(this, value));
		}
	}

	public int getElementType() {
		return IBeansModelElementTypes.SET_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		for (Object value : set) {
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

			// Now ask this set's entries
			for (Object value : set) {
				if (value instanceof IModelElement) {
					((IModelElement) value).accept(visitor, monitor);
					if (monitor.isCanceled()) {
						return;
					}
				}
			}
		}
	}

	public Set<Object> getSet() {
		return set;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansSet)) {
			return false;
		}
		BeansSet that = (BeansSet) other;
		if (!ObjectUtils.nullSafeEquals(this.set, that.set))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(set);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": set=");
		text.append(set);
		return text.toString();
	}
}
