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
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.model.properties.PropertyProperties;

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
			IBean modelBean = bean.getBean();
			String  propertyName  = getName();
			IBean innerBean = null;
			Iterator innerBeans = modelBean.getInnerBeans().iterator();
			while (innerBeans.hasNext()) {
				IBean iBean = (IBean) innerBeans.next();
				IBeansModelElement parent = iBean.getElementParent();
				if (parent instanceof IBeanProperty &&
								 parent.getElementName().equals(propertyName)) {
					innerBean = iBean;
					break;
				}
				
			}
			if (innerBean != null) {
				Iterator beanNames = innerBean.getReferencedBeans().iterator();
				while (beanNames.hasNext()) {
					String beanName = (String) beanNames.next();
					references.add(new RuntimeBeanReference(beanName));
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
		if (adapter == IPropertySource.class && property != null) {
			return new PropertyProperties(property);
		}
		return null;
	}
}
