/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model.properties;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class PropertyProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "Property";
	public static final String P_ID_NAME = "Property.name";
	public static final String P_ID_BEAN = "Property.bean";
	public static final String P_ID_CLASS = "Property.class";
	public static final String P_ID_VALUE = "Property.value";

	// Property descriptors
	private static Set<PropertyDescriptor> descriptors;
	static {
		descriptors = new LinkedHashSet<PropertyDescriptor>();
		PropertyDescriptor descriptor;

		descriptor = new PropertyDescriptor(P_ID_NAME, BeansUIPlugin
				.getResourceString(P_ID_NAME));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_BEAN, BeansUIPlugin
				.getResourceString(P_ID_BEAN));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_CLASS, BeansUIPlugin
				.getResourceString(P_ID_CLASS));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_VALUE, BeansUIPlugin
				.getResourceString(P_ID_VALUE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private IBeanProperty property;

	public PropertyProperties(IBeanProperty property) {
		this.property = property;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors
				.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_NAME.equals(id)) {
			return property.getElementName();
		} else if (P_ID_BEAN.equals(id)) {
			IBean bean = (IBean) property.getElementParent();
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else if (bean.isChildBean()) {
				return new ChildBeanProperties(bean);
			} else {
				// FIXME add factory bean support
				// return new FactoryBeanProperties(bean);
			}
		} else if (P_ID_CLASS.equals(id)) {
			Object value = property.getValue();
			Class valueClass = value.getClass();
			if (valueClass == RuntimeBeanReference.class) {
				return valueClass.getName();
			}
			return valueClass.getName();
		} else if (P_ID_VALUE.equals(id)) {
			return property.getValue();
		}
		return null;
	}

	public Object getEditableValue() {
		return this;
	}

	public boolean isPropertySet(Object id) {
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

	public void setPropertyValue(Object id, Object value) {
	}
}
