/*
 * Copyright 2002-2007 the original author or authors.
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
import org.eclipse.jdt.core.IType;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * This class holds the data for a Spring bean.
 * 
 * @author Torsten Juergeleit
 */
public class Bean extends AbstractBeansModelElement implements IBean {

	private BeanDefinition definition;
	private String[] aliases;
	private Set<IBeanConstructorArgument> constructorArguments;
	private Map<String, IBeanProperty> properties;

	public Bean(IModelElement parent, BeanDefinitionHolder bdHolder) {
		this(parent, bdHolder.getBeanName(), bdHolder.getAliases(), bdHolder
				.getBeanDefinition());
	}

	public Bean(IModelElement parent, String name, String[] aliases,
			BeanDefinition definition) {
		super(parent, name, definition);
		this.definition = definition;
		this.aliases = aliases;
	}

	public int getElementType() {
		return IBeansModelElementTypes.BEAN_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getConstructorArguments());
		children.addAll(getProperties());
		return children.toArray(new IModelElement[children.size()]);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this beans's constructor arguments
			for (IBeanConstructorArgument carg : getConstructorArguments()) {
				carg.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this beans's properties
			for (IBeanProperty property : getProperties()) {
				property.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public BeanDefinition getBeanDefinition() {
		return definition;
	}

	public String[] getAliases() {
		return aliases;
	}

	public Set<IBeanConstructorArgument> getConstructorArguments() {
		if (constructorArguments == null) {
			initBean();
		}
		return constructorArguments;
	}

	public IBeanProperty getProperty(String name) {
		if (name != null) {
			if (properties == null) {
				initBean();
			}
			return properties.get(name);
		}
		return null;
	}

	public Set<IBeanProperty> getProperties() {
		if (properties == null) {
			initBean();
		}
		return new LinkedHashSet<IBeanProperty>(properties.values());
	}

	public String getClassName() {
		if (definition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) definition).getBeanClassName();
		}
		return null;
	}

	public String getParentName() {
		if (definition instanceof ChildBeanDefinition) {
			return ((ChildBeanDefinition) definition).getParentName();
		}
		return null;
	}

	public boolean isRootBean() {
		return (definition instanceof RootBeanDefinition);
	}

	public boolean isChildBean() {
		return (definition instanceof ChildBeanDefinition);
	}

	public boolean isInnerBean() {
		IModelElement parent = getElementParent();
		return !(parent instanceof IBeansConfig
				|| parent instanceof IBeansComponent);
	}

	public boolean isSingleton() {
		if (definition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) definition).isSingleton();
		}
		return true;
	}

	public boolean isAbstract() {
		if (definition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) definition).isAbstract();
		}
		return false;
	}

	public boolean isLazyInit() {
		if (definition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) definition).isLazyInit();
		}
		return true;
	}

	public boolean isFactory() {
		if (definition instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition bd = (AbstractBeanDefinition) definition;
			if (bd.getFactoryBeanName() != null) {
				return true;
			}
			if (isRootBean() && bd.getFactoryMethodName() != null) {
				return true;
			}
			IType type = BeansModelUtils.getBeanType(this, null);
			if (type != null) {
				return Introspector.doesImplement(type,
						"org.springframework.beans.factory.FactoryBean");
			}
		}
		return false;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Bean)) {
			return false;
		}
		Bean that = (Bean) other;
		if (!ObjectUtils.nullSafeEquals(this.definition, that.definition))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.aliases, that.aliases))
			return false;
		return super.equals(other);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(definition);
		hashCode = getElementType() * hashCode
				+ ObjectUtils.nullSafeHashCode(aliases);
		return getElementType() * hashCode + super.hashCode();
	}

	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
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
	
	/**
	 * Lazily initialize this bean's data (constructor arguments, properties
	 * and inner beans).
	 */
	private void initBean() {

		// Retrieve this bean's constructor arguments
		constructorArguments = new LinkedHashSet<IBeanConstructorArgument>();
		ConstructorArgumentValues cargValues = definition
				.getConstructorArgumentValues();
		for (Object cargValue : cargValues.getGenericArgumentValues()) {
			IBeanConstructorArgument carg = new BeanConstructorArgument(this,
					(ValueHolder) cargValue);
			constructorArguments.add(carg);
		}
		Map indexedCargValues = cargValues.getIndexedArgumentValues();
		for (Object key : indexedCargValues.keySet()) {
			ValueHolder vHolder = (ValueHolder) indexedCargValues.get(key);
			IBeanConstructorArgument carg = new BeanConstructorArgument(this,
					((Integer) key).intValue(), vHolder);
			constructorArguments.add(carg);
		}

		// Retrieve this bean's properties
		properties = new LinkedHashMap<String, IBeanProperty>();
		for (PropertyValue propValue : definition.getPropertyValues()
				.getPropertyValues()) {
			IBeanProperty property = new BeanProperty(this, propValue);
			properties.put(property.getElementName(), property);
		}
	}
}
