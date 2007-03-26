/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;

/**
 * This is a representation of a Spring bean's constructor argument.
 * 
 * @author Torsten Juergeleit
 */
public class ConstructorArgument extends Node implements IAdaptable {

	private Bean bean;
	private IBeanConstructorArgument carg;

	public ConstructorArgument(Bean bean, IBeanConstructorArgument carg) {
		super(carg.getElementName());
		this.bean = bean;
		this.carg = carg;
	}

	public Bean getBean() {
		return bean;
	}

	public IBeanConstructorArgument getBeanConstructorArgument() {
		return carg;
	}

	public String getName() {
		return carg.getElementName();
	}

	/**
	 * Returns a list of all references to other beans of this
	 * ConstructorArgumentValue.
	 */
	public List<RuntimeBeanReference> getBeanReferences() {
		List<RuntimeBeanReference> references = new ArrayList<RuntimeBeanReference>();
		addReferencesForValue(carg.getValue(), references);
		return references;
	}

	/**
	 * Given a ConstructorArgumentValue, adds any references to other beans
	 * (RuntimeBeanReference). The value could be:
	 * <li>A RuntimeBeanReference, which will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences which
	 * will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will be
	 * added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 */
	private void addReferencesForValue(Object value,
			List<RuntimeBeanReference> references) {
		if (value instanceof RuntimeBeanReference) {
			references.add((RuntimeBeanReference) value);
		}
		else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				addReferencesForValue(list.get(i), references);
			}
		}
		else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext();) {
				addReferencesForValue(iter.next(), references);
			}
		}
		else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
				addReferencesForValue(map.get(iter.next()), references);
			}
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(carg);
		}
		return null;
	}
}
