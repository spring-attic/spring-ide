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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.ResourcePropertySource;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtil;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;

public class RootBeanProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "RootBean";
	public static final String P_ID_NAME = "RootBean.name";
	public static final String P_ID_CONFIG = "RootBean.config";
	public static final String P_ID_CLASS = "RootBean.class";
	public static final String P_ID_SINGLETON = "RootBean.singleton";
	public static final String P_ID_OVERRIDE = "RootBean.override";

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

		descriptor = new PropertyDescriptor(P_ID_CONFIG,
								  BeansUIPlugin.getResourceString(P_ID_CONFIG));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_CLASS,
								   BeansUIPlugin.getResourceString(P_ID_CLASS));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_SINGLETON,
							   BeansUIPlugin.getResourceString(P_ID_SINGLETON));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_OVERRIDE,
								BeansUIPlugin.getResourceString(P_ID_OVERRIDE));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private BeanNode bean;

	public RootBeanProperties(BeanNode bean) {
		this.bean = bean;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[]) descriptors.toArray(
								   new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_NAME.equals(id)) {
			return bean.getName();
		}
		if (P_ID_CONFIG.equals(id)) {
			ConfigNode config = bean.getConfigNode();
			IFile file = config.getConfigFile();
			if (file != null) {
				return new ConfigFilePropertySource(file);
			}
			return config.getName();
		}
		if (P_ID_CLASS.equals(id)) {
			IProject project = bean.getConfigNode().getProjectNode().getProject();
			String className = bean.getClassName();
			IType type = BeansModelUtil.getJavaType(project, className);

			// Use type only it's not a BinaryType (resource == null)
			if (type != null && type.getResource() != null) {
				return new TypeResourcePropertySource(type);
			} else {
				return className;
			}
		}
		if (P_ID_SINGLETON.equals(id)) {
			return new Boolean(bean.isSingleton());
		}
		if (P_ID_OVERRIDE.equals(id)) {
			return new Boolean(bean.isOverride());
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

	public String toString() {
		return bean.getName();
	}

	private class ConfigFilePropertySource extends FilePropertySource {
		private IFile file;

		public ConfigFilePropertySource(IFile file) {
			super(file);
			this.file = file;
		}

		public String toString() {
			return file.getFullPath().toString();
		}
	}

	private class TypeResourcePropertySource extends ResourcePropertySource {
		private IType type;

		public TypeResourcePropertySource(IType type) {
			super(type.getResource());
			this.type = type;
		}

		public String toString() {
			return type.getFullyQualifiedName();
		}
	}
}
