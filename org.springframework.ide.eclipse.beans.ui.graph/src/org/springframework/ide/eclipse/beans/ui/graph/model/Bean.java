/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.util.ClassUtils;

/**
 * This is a representation of a Spring bean.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class Bean extends Node implements IAdaptable {

	private IBean bean;

	private Bean[] innerBeans;

	public int preferredHeight;

	public Bean() {
		super("empty");
	}

	public Bean(IBean bean) {
		super(bean.getElementName());
		this.bean = bean;
	}

	public IBean getBean() {
		return bean;
	}

	public String getName() {
		if (bean != null && bean.isGeneratedElementName() && bean.getClassName() != null) {
			return "<anonymous> " + ClassUtils.getShortName(bean.getClassName());
		}
		else if (bean != null && bean.isGeneratedElementName() && bean.getClassName() == null) {
			return "<anonymous>";
		}
		return (bean != null ? bean.getElementName() : "empty");
	}

	public String getClassName() {
		return bean.getClassName();
	}

	public String getParentName() {
		return bean.getParentName();
	}

	public IFile getConfigFile() {
		return (IFile) BeansModelUtils.getConfig(bean).getElementResource();
	}

	public int getStartLine() {
		return bean.getElementStartLine();
	}

	public boolean hasConstructorArguments() {
		return bean.getConstructorArguments().size() > 0;
	}

	public ConstructorArgument[] getConstructorArguments() {
		ArrayList<ConstructorArgument> list = new ArrayList<ConstructorArgument>();
		Iterator cargs = bean.getConstructorArguments().iterator();
		while (cargs.hasNext()) {
			IBeanConstructorArgument carg = (IBeanConstructorArgument) cargs.next();
			list.add(new ConstructorArgument(this, carg));
		}
		return list.toArray(new ConstructorArgument[list.size()]);
	}

	public boolean hasProperties() {
		return getProperties().length > 0;
	}

	public Property[] getProperties() {
		ArrayList<Property> list = new ArrayList<Property>();
		Iterator props = bean.getProperties().iterator();
		while (props.hasNext()) {
			IBeanProperty prop = (IBeanProperty) props.next();
			list.add(new Property(this, prop));
		}
		props = BeansCorePlugin.getMetadataModel().getBeanProperties(bean).iterator();
		while (props.hasNext()) {
			IBeanProperty prop = (IBeanProperty) props.next();
			list.add(new Property(this, prop));
		}
		return list.toArray(new Property[list.size()]);
	}

	public Bean[] getInnerBeans() {
		if (innerBeans == null) {
			Set<Bean> innerBeans = new HashSet<Bean>();
			if (BeansUIPlugin.getDefault().getPluginPreferences().getBoolean(
					BeansUIPlugin.SHOULD_SHOW_INNER_BEANS_PREFERENCE_ID)) {
				for (IBean b : BeansModelUtils.getInnerBeans(bean, false)) {
					
					if (shouldAddBean(b)) {				
						innerBeans.add(new Bean(b));
					}
				}
			}
			this.innerBeans = innerBeans.toArray(new Bean[innerBeans.size()]);
		}
		return this.innerBeans;
	}
	
	private boolean shouldAddBean(IBean bean) {
		return !bean.isInfrastructure()
				|| (bean.isInfrastructure() && BeansUIPlugin.getDefault().getPluginPreferences()
						.getBoolean(BeansUIPlugin.SHOULD_SHOW_INFRASTRUCTURE_BEANS_PREFERENCE_ID));
	}

	public boolean isRootBean() {
		return bean.isRootBean();
	}

	public boolean isChildBean() {
		return bean.isChildBean();
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(bean);
		}
		return null;
	}

	@Override
	public String toString() {
		return "Bean '" + getName() + "': x=" + x + ", y=" + y + ", width=" + width + ", height="
				+ height;
	}
}
