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

package org.springframework.ide.eclipse.beans.ui.model.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

public class PropertyProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "Property";
	public static final String P_ID_NAME = "Property.name";
	public static final String P_ID_BEAN = "Property.bean";
	public static final String P_ID_CLASS = "Property.class";
	public static final String P_ID_VALUE = "Property.value";

	// Property descriptors
	private static List descriptors;
	static {
		descriptors = new ArrayList();
		PropertyDescriptor descriptor;

		descriptor = new PropertyDescriptor(P_ID_NAME,
									BeansUIPlugin.getResourceString(P_ID_NAME));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_BEAN,
									BeansUIPlugin.getResourceString(P_ID_BEAN));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_CLASS,
								   BeansUIPlugin.getResourceString(P_ID_CLASS));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_VALUE,
								   BeansUIPlugin.getResourceString(P_ID_VALUE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private IBeanProperty property;

	public PropertyProperties(IBeanProperty property) {
		this.property = property;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[]) descriptors.toArray(
								   new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_NAME.equals(id)) {
			return property.getElementName();
		} else if (P_ID_BEAN.equals(id)) {
			IBean bean = (IBean) property.getElementParent();
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else {
				return new ChildBeanProperties(bean);
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
