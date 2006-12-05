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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;

/**
 * This class holds the data for a Spring bean.
 * @author Torsten Juergeleit
 */
public class Bean extends AbstractSourceModelElement implements IBean {

	private BeanDefinition beanDefinition;
	private String[] aliases;
	private Set<IBeanConstructorArgument> constructorArguments;
	private Map<String, IBeanProperty> properties;
	private Set<IBean> innerBeans;

	public Bean(IModelElement parent, BeanDefinitionHolder bdHolder) {
		this(parent, bdHolder.getBeanName(), bdHolder.getAliases(),
				bdHolder.getBeanDefinition());
	}

	public Bean(IModelElement parent, String name, String[] aliases,
			BeanDefinition beanDefinition) {
		super(parent, name);
		setSourceRange(beanDefinition);
		this.beanDefinition = beanDefinition;
		this.aliases = aliases;

		constructorArguments = retrieveConstructorArguments(beanDefinition);
		properties = retrieveProperties(beanDefinition);
		innerBeans = retrieveInnerBeans();
	}

	public int getElementType() {
		return IBeansModelElementTypes.BEAN_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getConstructorArguments());
		children.addAll(getProperties());
		children.addAll(getInnerBeans());
		return children.toArray(new IModelElement[children.size()]);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this beans's constructor arguments
			for (IBeanConstructorArgument carg : constructorArguments) {
				carg.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Then ask this beans's properties
			for (IBeanProperty property : properties.values()) {
				property.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this bean's inner beans
			for (IBean bean : innerBeans) {
				bean.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public String[] getAliases() {
		return aliases;
	}

	public Set<IBeanConstructorArgument> getConstructorArguments() {
		return constructorArguments;
	}

	public IBeanProperty getProperty(String name) {
		if (name != null) {
			return properties.get(name);
		}
		return null;
	}

	public Set<IBeanProperty> getProperties() {
		return new LinkedHashSet<IBeanProperty>(properties.values());
	}

	public Set<IBean> getInnerBeans() {
		return innerBeans;
	}

	public String getClassName() {
		if (beanDefinition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) beanDefinition)
				.getBeanClassName();
		}
		return null;
	}

	public String getParentName() {
		if (beanDefinition instanceof ChildBeanDefinition) {
			return ((ChildBeanDefinition) beanDefinition).getParentName();
		}
		return null;
	}

	public boolean isRootBean() {
		return (beanDefinition instanceof RootBeanDefinition);
	}

	public boolean isChildBean() {
		return (beanDefinition instanceof ChildBeanDefinition);
	}

	public boolean isSingleton() {
		if (beanDefinition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) beanDefinition).isSingleton();
		}
		return true;
	}

	public boolean isAbstract() {
		if (beanDefinition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) beanDefinition).isAbstract();
		}
		return false;
	}

	public boolean isLazyInit() {
		if (beanDefinition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) beanDefinition).isLazyInit();
		}
		return true;
	}

	private Set<IBeanConstructorArgument> retrieveConstructorArguments(
			BeanDefinition beanDefinition) {
		Set<IBeanConstructorArgument> cargs = new LinkedHashSet
				<IBeanConstructorArgument>();
		ConstructorArgumentValues cargValues = beanDefinition
				.getConstructorArgumentValues();
		for (Object cargValue : cargValues.getGenericArgumentValues()) {
			IBeanConstructorArgument carg = new BeanConstructorArgument(this,
					(ValueHolder) cargValue);
			cargs.add(carg);
		}
		Map indexedCargValues = cargValues.getIndexedArgumentValues();
		for (Object key : indexedCargValues.keySet()) {
			ValueHolder vHolder = (ValueHolder) indexedCargValues.get(key);
			IBeanConstructorArgument carg = new BeanConstructorArgument(this,
					((Integer) key).intValue(), vHolder);
			cargs.add(carg);
		}
		return cargs;
	}

	private Map<String, IBeanProperty> retrieveProperties(
			BeanDefinition definition) {
		Map<String, IBeanProperty> properties = new LinkedHashMap
				<String, IBeanProperty>();
		for (PropertyValue propValue : definition.getPropertyValues()
				.getPropertyValues()) {
			IBeanProperty property = new BeanProperty(this, propValue);
			properties.put(property.getElementName(), property);
		}
		return properties;
	}

	private Set<IBean> retrieveInnerBeans() {
		Set<IBean> innerBeans = new LinkedHashSet<IBean>();
		for (IBeanConstructorArgument carg : constructorArguments) {
			addInnerBeans(carg, carg.getValue(), innerBeans);
		}
		for (IBeanProperty prop : properties.values()) {
			addInnerBeans(prop, prop.getValue(), innerBeans);
		}
		return innerBeans;
	}

	private void addInnerBeans(IModelElement parent, Object value,
			Set<IBean> innerBeans) {
		if (value instanceof BeanDefinitionHolder) {
			IBean bean = new Bean(parent, (BeanDefinitionHolder) value);
			innerBeans.add(bean);
			innerBeans.addAll(bean.getInnerBeans());
		} else if (value instanceof ManagedList) {
			for (Object element : (ManagedList) value) {
				addInnerBeans(parent, element, innerBeans);
			}
		} else if (value instanceof ManagedSet) {
			for (Object element : (ManagedSet) value) {
				addInnerBeans(parent, element, innerBeans);
			}
		} else if (value instanceof ManagedMap) {
			ManagedMap map = (ManagedMap) value;
			for (Object key : map.keySet()) {
				addInnerBeans(parent, map.get(key), innerBeans);
			}
		} else if (value instanceof ManagedProperties) {
			ManagedProperties props = (ManagedProperties) value;
			for (Object key : props.keySet()) {
				addInnerBeans(parent, props.get(key), innerBeans);
			}
		}
	}

	public String toString() {
		StringBuffer text = new StringBuffer(getElementName());
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
