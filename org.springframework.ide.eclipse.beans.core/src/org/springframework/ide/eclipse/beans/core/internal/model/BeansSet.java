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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;

/**
 * Holds a managed {@link Set}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansSet extends AbstractBeansModelElement implements IBeansSet {

	private LinkedHashSet<Object> set;

	public BeansSet(IModelElement parent, ManagedSet set) {
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

	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		for (Object value : set) {
			if (value instanceof IModelElement) {
				children.add((IModelElement) value);
			}
		}
		return children.toArray(new IModelElement[children.size()]);
	}

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

	public Set getSet() {
		return set;
	}
}
