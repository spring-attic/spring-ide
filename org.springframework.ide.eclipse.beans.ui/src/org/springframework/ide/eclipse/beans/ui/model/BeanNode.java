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

package org.springframework.ide.eclipse.beans.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Representation of a Spring bean.
 */
public class BeanNode extends AbstractNode {

	private ConfigNode config;
	private IBean bean;
	private boolean isOverride;
	private List constructorArguments;
	private List properties;
	private Map propertiesMap;	// lazily initialized in getProperty()
	private List innerBeans;

	/**
	 * Creates a new bean node with the given name within the specified config.
	 * 
	 * @param config the config this bean belongs to
	 * @param name the new bean's name
	 */
	public BeanNode(ConfigNode config, String name) {
		super(config, name);
		this.config = config; 
		this.constructorArguments = new ArrayList();
		this.properties = new ArrayList();
		this.innerBeans = new ArrayList();
		this.isOverride = false;

		// copy external flag from config node
		if (config != null &&
							(config.getFlags() & INode.FLAG_IS_EXTERNAL) != 0) {
			setFlags(INode.FLAG_IS_EXTERNAL);
		}
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
		this.constructorArguments = new ArrayList();
		this.properties = new ArrayList();
		this.innerBeans = new ArrayList();
		this.isOverride = false;

		// Copy external flag from config node
		if (bean != null && (bean.getFlags() & INode.FLAG_IS_EXTERNAL) != 0) {
			setFlags(INode.FLAG_IS_EXTERNAL);
		}
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
		this.config = bean.getConfigNode(); 
		setFlags(bean.getFlags());
		setBean(bean.getBean());

		// Clone contructor arguments
		this.constructorArguments = new ArrayList();
		ConstructorArgumentNode[] cargs = bean.getConstructorArguments();
		for (int i = 0; i < cargs.length; i++) {
			this.constructorArguments.add(new ConstructorArgumentNode(this,
										cargs[i].getBeanConstructorArgument()));
		}

		// Clone properties
		this.properties = new ArrayList();
		PropertyNode[] props = bean.getProperties();
		for (int i = 0; i < props.length; i++) {
			this.properties.add(new PropertyNode(this,
												 props[i].getBeanProperty()));
		}

		// Clone inner beans
		this.innerBeans = new ArrayList();
		BeanNode[] inner = bean.getInnerBeans();
		for (int i = 0; i < inner.length; i++) {
			this.innerBeans.add(new BeanNode(this, inner[i]));
		}
		
		this.isOverride = false;

		// copy external flag from config set node
		if (this.config != null &&
					   (this.config.getFlags() & INode.FLAG_IS_EXTERNAL) != 0) {
			setFlags(INode.FLAG_IS_EXTERNAL);
		}
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
		this.config = innerBean.getConfigNode(); 
		setFlags(innerBean.getFlags());
		setBean(innerBean.getBean());

		// Clone contructor arguments
		this.constructorArguments = new ArrayList();
		ConstructorArgumentNode[] cargs = innerBean.getConstructorArguments();
		for (int i = 0; i < cargs.length; i++) {
			this.constructorArguments.add(new ConstructorArgumentNode(this,
										cargs[i].getBeanConstructorArgument()));
		}

		// Clone properties
		this.properties = new ArrayList();
		PropertyNode[] props = innerBean.getProperties();
		for (int i = 0; i < props.length; i++) {
			this.properties.add(new PropertyNode(this,
												 props[i].getBeanProperty()));
		}

		// Clone inner beans
		this.innerBeans = new ArrayList();
		BeanNode[] inner = innerBean.getInnerBeans();
		for (int i = 0; i < inner.length; i++) {
			this.innerBeans.add(new BeanNode(this, inner[i]));
		}
		
		this.isOverride = false;

		// copy external flag from config set node
		if (this.config != null &&
					   (this.config.getFlags() & INode.FLAG_IS_EXTERNAL) != 0) {
			setFlags(INode.FLAG_IS_EXTERNAL);
		}
	}

	public void setBean(IBean bean) {
		this.bean = bean;
		if (bean != null) {
			if (!bean.isSingleton()) {
				setFlags(INode.FLAG_IS_PROTOTYPE);
			}
			if (bean.isLazyInit()) {
				setFlags(INode.FLAG_IS_LAZY_INIT);
			}
			if (bean.isAbstract()) {
				setFlags(INode.FLAG_IS_ABSTRACT);
			}
			if (bean.isRootBean() && bean.getClassName() == null &&
												 bean.getParentName() == null) {
				setFlags(INode.FLAG_IS_ROOT_BEAN_WITHOUT_CLASS);
			}
			setStartLine(bean.getElementStartLine()); 
		}
	}

	public IBean getBean() {
		return bean;
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

	public ConstructorArgumentNode[] getConstructorArguments() {
		return (ConstructorArgumentNode[]) constructorArguments.toArray(
					  new ConstructorArgumentNode[constructorArguments.size()]);
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
			propertiesMap = new HashMap();
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

	public PropertyNode[] getProperties() {
		return (PropertyNode[]) properties.toArray(
										   new PropertyNode[properties.size()]);
	}

	public void addInnerBean(BeanNode bean) {
		innerBeans.add(bean);
	}

	public BeanNode[] getInnerBeans() {
		return (BeanNode[]) innerBeans.toArray(new BeanNode[innerBeans.size()]);
	}

	public String getClassName() {
		return (bean != null ? bean.getClassName() : null);
	}

	public String getParentName() {
		return (bean != null ? bean.getParentName() : null);
	}

	public boolean isRootBean() {
		return (bean != null ? bean.isRootBean() : true);
	}

	public boolean isSingleton() {
		return (bean != null ? bean.isSingleton() : true);
	}

	public boolean isLazyInit() {
		return (bean != null ? bean.isLazyInit() : false);
	}

	public boolean isAbstract() {
		return (bean != null ? bean.isAbstract() : false);
	}
	
	/**
	 * Returns the <code>ConfigNode</code> containing this bean.
	 * 
	 * @return ConfigNode the project containing this bean
	 */
	public ConfigNode getConfigNode() {
		return config;
	}

	/**
	 * Returns list of beans which are referenced from within this bean's parent
	 * bean (if this bean is a child bean), constructor arguments or properties.
	 */
	public Collection getReferencedBeans() {
		List refBeans = new ArrayList();

		// Add parent bean (if available)
		if (!isRootBean()) {
			String beanName = getParentName();
			BeanNode parentBean = ModelUtil.getBean(getParent(), beanName);
			if (parentBean != null) {
				refBeans.add(parentBean);
				ModelUtil.addReferencedBeansForBean(getParent(), beanName,
													refBeans);
			}
		}

		// Add referenced beans from constructor arguments
		Iterator cargs = constructorArguments.iterator();
		while (cargs.hasNext()) {
			ConstructorArgumentNode carg = (ConstructorArgumentNode)
																   cargs.next();
			Iterator beans = carg.getReferencedBeans().iterator();
			while (beans.hasNext()) {
				BeanNode bean = (BeanNode) beans.next();
				if (!refBeans.contains(bean)) {
					refBeans.add(bean);
				}
			}
		}

		// Add referenced beans from properties
		Iterator props = properties.iterator();
		while (props.hasNext()) {
			PropertyNode property = (PropertyNode) props.next();
			Iterator beans = property.getReferencedBeans().iterator();
			while (beans.hasNext()) {
				BeanNode bean = (BeanNode) beans.next();
				if (!refBeans.contains(bean)) {
					refBeans.add(bean);
				}
			}
		}

		// Add referenced beans from inner beans
		Iterator inner = innerBeans.iterator();
		while (inner.hasNext()) {
			BeanNode innerBean = (BeanNode) inner.next();
			Iterator beans = innerBean.getReferencedBeans().iterator();
			while (beans.hasNext()) {
				BeanNode bean = (BeanNode) beans.next();
				if (!refBeans.contains(bean)) {
					refBeans.add(bean);
				}
			}
		}
		return refBeans;
	}

	public void remove(INode node) {
		properties.remove(node);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(bean);
		} else if (adapter == IModelElement.class) {
			return bean;
		}
		return super.getAdapter(adapter);
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
