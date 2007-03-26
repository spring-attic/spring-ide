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
package org.springframework.ide.eclipse.beans.ui.model.properties;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * @author Torsten Juergeleit
 */
public class ConstructorArgumentProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "ConstructorArgument";
	public static final String P_ID_BEAN = "ConstructorArgument.bean";
	public static final String P_ID_INDEX = "ConstructorArgument.index";
	public static final String P_ID_TYPE = "ConstructorArgument.type";
	public static final String P_ID_VALUE = "ConstructorArgument.value";

	// Property descriptors
	private static Set<PropertyDescriptor> descriptors;
	static {
		descriptors = new LinkedHashSet<PropertyDescriptor>();
		PropertyDescriptor descriptor;

		descriptor = new PropertyDescriptor(P_ID_BEAN, BeansUIPlugin
				.getResourceString(P_ID_BEAN));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_INDEX, BeansUIPlugin
				.getResourceString(P_ID_INDEX));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_TYPE, BeansUIPlugin
				.getResourceString(P_ID_TYPE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_VALUE, BeansUIPlugin
				.getResourceString(P_ID_VALUE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private IBeanConstructorArgument constructorArg;

	public ConstructorArgumentProperties(IBeanConstructorArgument constructorArg) {
		this.constructorArg = constructorArg;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors
				.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_BEAN.equals(id)) {
			IBean bean = (IBean) constructorArg.getElementParent();
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else if (bean.isChildBean()) {
				return new ChildBeanProperties(bean);
			} else {
				// FIXME add factory bean support
				// return new FactoryBeanProperties(bean);
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
