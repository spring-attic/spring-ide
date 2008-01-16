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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanMethodOverride;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * This class holds the data for a Spring bean.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class Bean extends AbstractBeansModelElement implements IBean {

	private BeanDefinition definition;

	private String[] aliases;

	private Set<IBeanConstructorArgument> constructorArguments;

	private Set<IBeanMethodOverride> methodOverrides;

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

	/**
	 * For inner beans we have to omit the element name because it consists of
	 * volatile stuff, like object ids.
	 */
	@Override
	protected String getUniqueElementName() {
		if (isInnerBean()) {
			return "" + ID_SEPARATOR + getElementStartLine();
		}
		return super.getUniqueElementName();
	}

	@Override
	public IResource getElementResource() {
		// We need to make sure that the beans resource comes back
 		if (getElementSourceLocation() != null
				&& getElementSourceLocation().getResource() instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) getElementSourceLocation()
					.getResource()).getAdapter(IResource.class);
			if (resource != null) {
				return resource;
			}
		}
		return super.getElementResource();
	}

	@Override
	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getConstructorArguments());
		children.addAll(getMethodOverrides());
		children.addAll(getProperties());
		return children.toArray(new IModelElement[children.size()]);
	}

	@Override
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
			
			// Now ask this bean's method overrides
			for (IBeanMethodOverride mo : getMethodOverrides()) {
				mo.accept(visitor, monitor);
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

	public Set<IBeanMethodOverride> getMethodOverrides() {
		if (methodOverrides == null) {
			initBean();
		}
		return methodOverrides;
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
		return definition.getParentName();
	}

	public boolean isRootBean() {
		return !isChildBean();
		// (definition instanceof RootBeanDefinition || definition instanceof GenericBeanDefinition);
	}

	public boolean isChildBean() {
		return definition.getParentName() != null;
	}

	public boolean isInnerBean() {
		IModelElement parent = getElementParent();
		return !(parent instanceof IBeansConfig || parent instanceof IBeansComponent);
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

	public boolean isInfrastructure() {
		if (definition instanceof AbstractBeanDefinition) {
			return ((AbstractBeanDefinition) definition).getRole() == BeanDefinition.ROLE_INFRASTRUCTURE;
		}
		return false;
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

	@Override
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

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(definition);
		hashCode = getElementType() * hashCode
				+ ObjectUtils.nullSafeHashCode(aliases);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		if (getClassName() != null) {
			text.append(" [");
			text.append(getClassName());
			text.append(']');
		}
		else if (getParentName() != null) {
			text.append(" <");
			text.append(getParentName());
			text.append('>');
		}
		return text.toString();
	}

	/**
	 * Lazily initialize this bean's data (constructor arguments, properties and
	 * inner beans).
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
		Map<?, ?> indexedCargValues = cargValues.getIndexedArgumentValues();
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

		// Retrieve this bean's method overrides
		if (definition instanceof AbstractBeanDefinition) {
			methodOverrides = new LinkedHashSet<IBeanMethodOverride>();
			MethodOverrides mos = ((AbstractBeanDefinition) definition)
					.getMethodOverrides();
			if (mos != null) {
				for (Object mo : mos.getOverrides()) {
					if (mo instanceof LookupOverride) {
						methodOverrides.add(new BeanLookupMethodOverride(this,
								(LookupOverride) mo));
					}
					else if (mo instanceof ReplaceOverride) {
						methodOverrides.add(new BeanReplaceMethodOverride(this,
								(ReplaceOverride) mo));
					}
				}
			}
		}
	}
}
