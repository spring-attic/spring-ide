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
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConstructorArgumentNode;

public class ConstructorArgumentProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "ConstructorArgument";
	public static final String P_ID_BEAN = "ConstructorArgument.bean";
	public static final String P_ID_INDEX = "ConstructorArgument.index";
	public static final String P_ID_TYPE = "ConstructorArgument.type";
	public static final String P_ID_VALUE = "ConstructorArgument.value";

	// Property descriptors
	private static List descriptors;
	static {
		descriptors = new ArrayList();
		PropertyDescriptor descriptor;

		descriptor = new PropertyDescriptor(P_ID_BEAN,
									BeansUIPlugin.getResourceString(P_ID_BEAN));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_INDEX,
									  BeansUIPlugin.getResourceString(P_ID_INDEX));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_TYPE,
									   BeansUIPlugin.getResourceString(P_ID_TYPE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_VALUE,
								   BeansUIPlugin.getResourceString(P_ID_VALUE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private ConstructorArgumentNode constructorArg;

	public ConstructorArgumentProperties(ConstructorArgumentNode constructorArg) {
		this.constructorArg = constructorArg;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[]) descriptors.toArray(
									new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_BEAN.equals(id)) {
			BeanNode bean = (BeanNode) constructorArg.getParent();
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else {
				return new ChildBeanProperties(bean);
			}
		} else if (P_ID_INDEX.equals(id)) {
			return new Integer(constructorArg.getIndex());
		} else if (P_ID_TYPE.equals(id)) {
			return constructorArg.getType();
		} else if (P_ID_VALUE.equals(id)) {
			return constructorArg.getValue();
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
