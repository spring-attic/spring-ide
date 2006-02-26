/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This class holds the data for a Spring bean.
 *
 * @author Torsten Juergeleit
 */
public class Bean extends AbstractSourceModelElement implements IBean {

	private BeanDefinitionHolder beanDefinitionHolder;
	private List constructorArguments;
	private List properties;
	private Map propertiesMap;
	private List innerBeans;

	public Bean(IBeansConfig config) {
		super(config, null);   // the name we get from the BeanDefinitionHolder
		this.constructorArguments = new ArrayList();
		this.properties = new ArrayList();
		this.propertiesMap = new HashMap();
		this.innerBeans = new ArrayList();
	}

	public int getElementType() {
		return IBeansModelElementTypes.BEAN_TYPE;
	}

	public IModelElement[] getElementChildren() {
		ArrayList children = new ArrayList(getConstructorArguments());
		children.addAll(getProperties());
		children.addAll(getInnerBeans());
		return (IModelElement[]) children.toArray(
										   new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		if (getElementParent() instanceof IResourceModelElement) {
			return ((IResourceModelElement)
									  getElementParent()).getElementResource();
		}
		return null;
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this beans's constructor arguments
			Iterator iter = constructorArguments.iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// The ask this beans's properties
			iter = properties.iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this bean's inner beans
			iter = innerBeans.iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public void setBeanDefinitionHolder(
									BeanDefinitionHolder beanDefinitionHolder) {
		this.beanDefinitionHolder = beanDefinitionHolder;
		setElementName(beanDefinitionHolder.getBeanName());
	}

	public BeanDefinitionHolder getBeanDefinitionHolder() {
		return beanDefinitionHolder;
	}

	public String[] getAliases() {
		return beanDefinitionHolder.getAliases();
	}

	public void addConstructorArgument(IBeanConstructorArgument carg) {
		constructorArguments.add(carg);
	}

	public Collection getConstructorArguments() {
		return constructorArguments;
	}

	public void addProperty(IBeanProperty property) {
		properties.add(property);
		propertiesMap.put(property.getElementName(), property);
	}

	public IBeanProperty getProperty(String name) {
		if (name != null) {
			return (IBeanProperty) propertiesMap.get(name);
		}
		return null;
	}

	public Collection getProperties() {
		return properties;
	}

	public void addInnerBean(Bean bean) {
		innerBeans.add(bean);
	}

	public Collection getInnerBeans() {
		return innerBeans;
	}

	public String getClassName() {
		BeanDefinition beanDef = beanDefinitionHolder.getBeanDefinition();
		if (beanDef instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) beanDef).getBeanClassName();
		}
		return null;
	}

	public String getParentName() {
		BeanDefinition beanDef = beanDefinitionHolder.getBeanDefinition();
		if (beanDef instanceof ChildBeanDefinition) {
			return ((ChildBeanDefinition) beanDef).getParentName();
		}
		return null;
	}

	public boolean isRootBean() {
		return (beanDefinitionHolder.getBeanDefinition() instanceof
														   RootBeanDefinition);
	}

	public boolean isChildBean() {
		return (beanDefinitionHolder.getBeanDefinition() instanceof
														  ChildBeanDefinition);
	}

	public boolean isSingleton() {
		BeanDefinition beanDef = beanDefinitionHolder.getBeanDefinition();
		if (beanDef instanceof RootBeanDefinition) {
			return ((RootBeanDefinition) beanDef).isSingleton();
		} else if (beanDef instanceof ChildBeanDefinition){
			return ((ChildBeanDefinition) beanDef).isSingleton();
		}
		return true;
	}

	public boolean isAbstract() {
		return beanDefinitionHolder.getBeanDefinition().isAbstract();
	}

	public boolean isLazyInit() {
		return beanDefinitionHolder.getBeanDefinition().isLazyInit();
	}

	public boolean isInnerBean() {
		return (getElementParent() != null);
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getElementName());
		text.append(" (");
		text.append(getElementStartLine());
		text.append(')');
		if (getClassName() != null) {
			text.append(" [");
			text.append(getClassName());
			text.append(']');
		} else if (getParentName() != null) {
			text.append(" <");
			text.append(getParentName());
			text.append('>');
		}
		return text.toString();
	}
}
