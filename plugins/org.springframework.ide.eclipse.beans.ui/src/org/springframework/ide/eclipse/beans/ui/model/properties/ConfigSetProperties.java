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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class ConfigSetProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "ConfigSet";
	public static final String P_ID_NAME = "ConfigSet.name";
	public static final String P_ID_OVERRIDE = "ConfigSet.override";
	public static final String P_ID_INCOMPLETE = "ConfigSet.incomplete";

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

		descriptor = new PropertyDescriptor(P_ID_OVERRIDE, BeansUIPlugin
				.getResourceString(P_ID_OVERRIDE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_INCOMPLETE, BeansUIPlugin
				.getResourceString(P_ID_INCOMPLETE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private IBeansConfigSet configSet;

	public ConfigSetProperties(IBeansConfigSet configSet) {
		this.configSet = configSet;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors
				.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_NAME.equals(id)) {
			return configSet.getElementName();
		} else if (P_ID_OVERRIDE.equals(id)) {
			return new Boolean(configSet.isAllowBeanDefinitionOverriding());
		} else if (P_ID_INCOMPLETE.equals(id)) {
			return new Boolean(configSet.isIncomplete());
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
