/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model.properties;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class ChildBeanProperties implements IPropertySource {

	// Property unique keys
	public static final String P_CATEGORY = "ChildBean";
	public static final String P_ID_NAME = "ChildBean.name";
	public static final String P_ID_CONFIG = "ChildBean.config";
	public static final String P_ID_PARENT = "ChildBean.parent";
	public static final String P_ID_SINGLETON = "ChildBean.singleton";
	public static final String P_ID_LAZY_INIT = "ChildBean.lazyinit";
	public static final String P_ID_ABSTRACT = "ChildBean.abstract";

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

		descriptor = new PropertyDescriptor(P_ID_CONFIG, BeansUIPlugin
				.getResourceString(P_ID_CONFIG));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_PARENT, BeansUIPlugin
				.getResourceString(P_ID_PARENT));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_SINGLETON, BeansUIPlugin
				.getResourceString(P_ID_SINGLETON));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_LAZY_INIT, BeansUIPlugin
				.getResourceString(P_ID_LAZY_INIT));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);

		descriptor = new PropertyDescriptor(P_ID_ABSTRACT, BeansUIPlugin
				.getResourceString(P_ID_ABSTRACT));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(BeansUIPlugin.getResourceString(P_CATEGORY));
		descriptors.add(descriptor);
	}

	private IBean bean;

	public ChildBeanProperties(IBean bean) {
		this.bean = bean;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors
				.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_NAME.equals(id)) {
			return bean.getElementName();
		}
		if (P_ID_CONFIG.equals(id)) {
			IBeansConfig config = BeansModelUtils.getConfig(bean);
			IFile file = (IFile) config.getElementResource();
			if (file != null) {
				return new ConfigFilePropertySource(file);
			}
			return config.getElementName();
		}
		if (P_ID_PARENT.equals(id)) {
			String parentName = bean.getParentName();
			IBean parentBean = BeansModelUtils.getConfig(bean).getBean(
					parentName);
			if (parentBean != null) {
				if (parentBean.isRootBean()) {
					return new RootBeanProperties(parentBean);
				} else if (parentBean.isChildBean()) {
					return new ChildBeanProperties(parentBean);
				} else {
					// FIXME add factory bean support
					// return new FactoryBeanProperties(parentBean);
				}
			}
			return parentName;
		}
		if (P_ID_SINGLETON.equals(id)) {
			return new Boolean(bean.isSingleton());
		}
		if (P_ID_LAZY_INIT.equals(id)) {
			return new Boolean(bean.isLazyInit());
		}
		if (P_ID_ABSTRACT.equals(id)) {
			return new Boolean(bean.isAbstract());
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

	@Override
	public String toString() {
		return bean.getElementName();
	}

	private class ConfigFilePropertySource extends FilePropertySource {
		private IFile file;

		public ConfigFilePropertySource(IFile file) {
			super(file);
			this.file = file;
		}

		@Override
		public String toString() {
			return file.getFullPath().toString();
		}
	}
}
