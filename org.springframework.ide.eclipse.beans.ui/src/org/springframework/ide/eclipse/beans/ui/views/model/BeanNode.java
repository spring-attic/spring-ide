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

package org.springframework.ide.eclipse.beans.ui.views.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * Representation of a Spring bean.
 *
 * @author Torsten Juergeleit
 */
public class BeanNode extends AbstractNode {

	private ConfigNode config;
	private boolean isOverride;
	private Set<ConstructorArgumentNode> constructorArguments;
	private Set<PropertyNode> properties;
	private Map<String, PropertyNode> propertiesMap;	// lazily initialized in getProperty()
	private Set<BeanNode> innerBeans;

	/**
	 * Creates a new bean node with the given name within the specified config.
	 * 
	 * @param config the config this bean belongs to
	 * @param name the new bean's name
	 */
	public BeanNode(ConfigNode config, String name) {
		super(config, name);
		this.config = config; 
		constructorArguments = new LinkedHashSet<ConstructorArgumentNode>();
		properties = new LinkedHashSet<PropertyNode>();
		innerBeans = new LinkedHashSet<BeanNode>();
		isOverride = false;
	}

	/**
	 * Creates a new inner bean node with the given name within the specified
	 * bean.
	 * 
	 * @param bean the bean this inner bean belongs to
	 * @param name the new bean's name
	 */
	public BeanNode(BeanNode bean, String name) {
		super(bean, name);
		constructorArguments = new LinkedHashSet<ConstructorArgumentNode>();
		properties = new LinkedHashSet<PropertyNode>();
		innerBeans = new LinkedHashSet<BeanNode>();
		isOverride = false;
	}

	/**
	 * Creates a new bean node which is a clone from given bean node within the
	 * given config set.
	 *
	 * @param configSet the config this bean belongs to 
	 * @param bean the bean which is to clone
	 */
	public BeanNode(ConfigSetNode configSet, BeanNode bean) {
		super(configSet, bean.getName());
		config = bean.getConfigNode(); 
		setElement(bean.getElement());
		setFlags(bean.getFlags());

		// Clone contructor arguments
		constructorArguments = new LinkedHashSet<ConstructorArgumentNode>();
		for (ConstructorArgumentNode carg : bean.getConstructorArguments()) {
			constructorArguments.add(new ConstructorArgumentNode(this,
					carg.getConstructorArgument()));
		}

		// Clone properties
		properties = new LinkedHashSet<PropertyNode>();
		for (PropertyNode prop : bean.getProperties()) {
			properties.add(new PropertyNode(this, prop.getProperty()));
		}

		// Clone inner beans
		innerBeans = new LinkedHashSet<BeanNode>();
		for (BeanNode inner : bean.getInnerBeans()) {
			innerBeans.add(new BeanNode(this, inner));
		}
		
		this.isOverride = false;
	}

	/**
	 * Creates a new bean node which is a clone of the given inner bean node
	 * within the given bean.
	 * 
	 * @param innerBean the bean this inner bean belongs to
	 * @param innerbean the inner bean which has to be cloned
	 */
	public BeanNode(BeanNode bean, BeanNode innerBean) {
		super(bean, innerBean.getName());
		config = innerBean.getConfigNode(); 
		setElement(innerBean.getElement());
		setFlags(innerBean.getFlags());

		// Clone contructor arguments
		constructorArguments = new LinkedHashSet<ConstructorArgumentNode>();
		for (ConstructorArgumentNode carg :
				innerBean.getConstructorArguments()) {
			this.constructorArguments.add(new ConstructorArgumentNode(this,
					carg.getConstructorArgument()));
		}

		// Clone properties
		properties = new LinkedHashSet<PropertyNode>();
		for (PropertyNode prop : innerBean.getProperties()) {
			properties.add(new PropertyNode(this, prop.getProperty()));
		}

		// Clone inner beans
		innerBeans = new LinkedHashSet<BeanNode>();
		for (BeanNode inner : innerBean.getInnerBeans()) {
			innerBeans.add(new BeanNode(this, inner));
		}
		
		isOverride = false;
	}

	public void setBean(IBean bean) {
		setElement(bean);
	}

	public IBean getBean() {
		return (IBean) getElement();
	}

	public void setIsOverride(boolean isOverridden) {
		this.isOverride = isOverridden;
		setFlags(INode.FLAG_HAS_WARNINGS);
	}

	public boolean isOverride() {
		return isOverride;
	}

	public void addConstructorArgument(ConstructorArgumentNode carg) {
		constructorArguments.add(carg);
	}

	public Set<ConstructorArgumentNode> getConstructorArguments() {
		return new LinkedHashSet<ConstructorArgumentNode>(constructorArguments);
	}

	public boolean hasConstructorArguments() {
		return !constructorArguments.isEmpty();
	}

	public void addProperty(PropertyNode property) {
		properties.add(property);
	}

	public PropertyNode getProperty(String name) {
		if (propertiesMap == null) {

			// Lazily initialize the property map
			propertiesMap = new LinkedHashMap<String, PropertyNode>();
			Iterator iter = properties.iterator();
			while (iter.hasNext()) {
				PropertyNode property = (PropertyNode) iter.next();
				propertiesMap.put(property.getName(), property);
			}
		}
		return (PropertyNode) propertiesMap.get(name);
	}

	public boolean hasProperties() {
		return !properties.isEmpty();
	}

	public Set<PropertyNode> getProperties() {
		return new LinkedHashSet<PropertyNode>(properties);
	}

	public void addInnerBean(BeanNode bean) {
		innerBeans.add(bean);
	}

	public BeanNode[] getInnerBeans() {
		return (BeanNode[]) innerBeans.toArray(new BeanNode[innerBeans.size()]);
	}

	public String getClassName() {
		return (getBean() != null ? getBean().getClassName() : null);
	}

	public String getParentName() {
		return (getBean() != null ? getBean().getParentName() : null);
	}

	public boolean isRootBean() {
		return (getBean() != null ? getBean().isRootBean() : true);
	}

	public boolean isChildBean() {
		return (getBean() != null ? getBean().isChildBean() : true);
	}

	public boolean isSingleton() {
		return (getBean() != null ? getBean().isSingleton() : true);
	}

	public boolean isLazyInit() {
		return (getBean() != null ? getBean().isLazyInit() : false);
	}

	public boolean isAbstract() {
		return (getBean() != null ? getBean().isAbstract() : false);
	}
	
	/**
	 * Returns the <code>ConfigNode</code> containing this bean.
	 * 
	 * @return ConfigNode the project containing this bean
	 */
	public ConfigNode getConfigNode() {
		return config;
	}

	public void remove(INode node) {
		properties.remove(node);
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getName());
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
