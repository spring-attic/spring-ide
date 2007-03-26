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
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

public class Property extends Node implements IAdaptable {

	private Bean bean;
	private IBeanProperty property;

	public Property(Bean bean, IBeanProperty property) {
		super(property.getElementName());
		this.bean = bean;
		this.property = property;
	}

	public Bean getBean() {
		return bean;
	}

	public IBeanProperty getBeanProperty() {
		return property;
	}

	public String getName() {
		return property.getElementName();
	}

	/**
	 * Returns a list of all references to other beans
	 * (RuntimeBeanReferences) of this property.
	 */
	public List getBeanReferences() {
		List references = new ArrayList();
		addReferencesForValue(property.getValue(), references);
		return references;
	}

	/**
	 * Given a PropertyValue, adds any references to other beans
	 * (RuntimeBeanReference). The value could be:
	 * <li>A BeanDefinitionHolder, a RuntimeBeanReference for the inner bean
	 * will be added.
	 * <li>A RuntimeBeanReference, which will be added.
	 * <li>A List. This is a collection that may contain RuntimeBeanReferences
	 * which will be added.
	 * <li>A Set. May also contain RuntimeBeanReferences that will be added.
	 * <li>A Map. In this case the value may be a RuntimeBeanReference that will
	 * be added.
	 * <li>An ordinary object or null, in which case it's ignored.
	 */
	private void addReferencesForValue(Object value, List references) {
		if (value instanceof BeanDefinitionHolder) {
			String beanName = ((BeanDefinitionHolder) value).getBeanName();
			IBean modelBean = bean.getBean();
			String  propertyName  = getName();
			IBean innerBean = null;
			for (IBean iBean : BeansModelUtils.getInnerBeans(modelBean)) {
				if (iBean.getElementName().equals(beanName)) {
					IModelElement parent = iBean.getElementParent();
					if (parent instanceof IBeanProperty
							&& parent.getElementName().equals(propertyName)) {
						innerBean = iBean;
						break;
					}
				}
			}
			if (innerBean != null) {
				Iterator refBeans = BeansModelUtils.getBeanReferences(
							innerBean, BeansModelUtils.getConfig(innerBean),
							false).iterator();
				while (refBeans.hasNext()) {
					String refBeanName = ((IBean)
											 refBeans.next()).getElementName();
					references.add(new RuntimeBeanReference(refBeanName));
				}
			}
		} else if (value instanceof RuntimeBeanReference) {
			references.add(value);
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				addReferencesForValue(list.get(i), references);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				addReferencesForValue(iter.next(), references);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				addReferencesForValue(map.get(iter.next()), references);
			}
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(property);
		}
		return null;
	}
}
