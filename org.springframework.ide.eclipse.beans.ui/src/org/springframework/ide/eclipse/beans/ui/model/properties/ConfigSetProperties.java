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
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;

public class ConfigSetProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "ConfigSet";
	public static final String P_ID_NAME = "ConfigSet.name";
	public static final String P_ID_OVERRIDE = "ConfigSet.override";

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

		descriptor = new PropertyDescriptor(P_ID_OVERRIDE,
								BeansUIPlugin.getResourceString(P_ID_OVERRIDE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private ConfigSetNode configSet;

	public ConfigSetProperties(ConfigSetNode configSet) {
		this.configSet = configSet;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[]) descriptors.toArray(
								   new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_NAME.equals(id)) {
			return configSet.getName();
		} else if (P_ID_OVERRIDE.equals(id)) {
			return new Boolean(configSet.isOverrideEnabled());
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
