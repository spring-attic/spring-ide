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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;

/**
 * Representation of a Spring bean configuration.
 */
public class ConfigNode extends AbstractNode {

	private IBeansConfig config;
	private List beans;  // lazily initialized in getBeans() or getBean()
	private Map beansMap;  // lazily initialized in getBean()

	/**
	 * Creates a new Spring bean factory node with given name.
	 * 
	 * @param project the new node's parent
	 * @param name the new node's config file name (full path)
	 */
	public ConfigNode(ProjectNode project, String name) {
		super(project, name);
		setBeansConfig(project);
	}

	/**
	 * Creates a new Spring bean factory node with given name.
	 * 
	 * @param configSet the new node's parent
	 * @param name the new node's config file name (full path)
	 */
	public ConfigNode(ConfigSetNode configSet, String name) {
		super(configSet, name);
		setBeansConfig(configSet.getProjectNode());
	}

	public ProjectNode getProjectNode() {
		Object parent = getParent();
		if (parent instanceof ConfigSetNode) {
			return ((ConfigSetNode) parent).getProjectNode();
		}
		return (ProjectNode) parent;
	}

	public IFile getConfigFile() {
		return (config != null ? config.getConfigFile() : null);
	}

	public BeanNode getBean(String name) {
		if (beansMap == null) {
			if (beans == null) {
				// Lazily populated bean list
				createBeans();
				refreshViewer();
			}

			// Lazily initialize the bean map
			beansMap = new HashMap();
			Iterator iter = beans.iterator();
			while (iter.hasNext()) {
				BeanNode bean = (BeanNode) iter.next();
				beansMap.put(bean.getName(), bean);
			}
		}
		return (BeanNode) beansMap.get(name);
	}

	/**
	 * Returns the bean nodes of this bean factory.
	 * 
	 * @return bean nodes of this bean factory
	 */
	public BeanNode[] getBeans(boolean refreshViewer) {
		if (beans == null) {
			// Lazily populated bean list
			createBeans();
			if (refreshViewer) {
				refreshViewer();
			}
		}
		return (BeanNode[]) beans.toArray(new BeanNode[beans.size()]);
	}

	/**
	 * Adds the given bean to this bean factory.
	 * 
	 * @param bean the bean to add
	 */
	public void addBean(BeanNode bean) {
		beans.add(bean);
	}
	
	/**
	 * Sets the error message of this bean factory and creates a new child
	 * node with the error message and given line number.
	 *
	 * @param errorMessage the error message generated while parsing this
	 *						bean factory's config file
	 * @param lineNumber loction of parse error
	 */
	private void setErrorMessage(String errorMessage, int lineNumber) {
		BeanNode bean = new BeanNode(this, errorMessage);
		bean.setStartLine(lineNumber);
		bean.setFlags(INode.FLAG_HAS_ERRORS);
		addBean(bean);
	}
	
	/**
	 * Clear's this node's internally stored data
	 */
	public void clear() {
		beans.clear();
	}

	public void remove(INode node) {
		beans.remove(node);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(config);
		}
		return super.getAdapter(adapter);
	}

	public String toString() {
		StringBuffer text = new StringBuffer(getName());
		if (config != null) {
			text.append(": ");
			text.append(config.toString());
		}
		return text.toString();
	}

	private void setBeansConfig(ProjectNode project) {
		String configName = getName();
		if (configName.charAt(0) == '/') {
			int configNamePos = configName.indexOf('/', 1);
			String projectName = configName.substring(1, configNamePos);
			IBeansProject proj = BeansCorePlugin.getModel().getProject(
																   projectName);
			configName  = configName.substring(configNamePos + 1);
			if (proj != null) { 
				config = proj.getConfig(configName);
			}
			setFlags(INode.FLAG_IS_EXTERNAL);
		} else {
			config = project.getBeansProject().getConfig(configName);
		}
	}

	/**
	 * Populates the bean list contained in the corresponding
	 * <code>BeansConfig</code>.
	 * If an error occurs while parsing the file, the error state will be set
	 * and a bean error node will be added.
	 */
	private void createBeans() {
		beans = new ArrayList();
		if (config == null) {
			setErrorMessage("Undefined Spring config '" + getName() + "'", -1);
		} else {
			BeanDefinitionException exception = config.getException();
			if (exception != null	) {
				setErrorMessage(exception.getMessage(),
								exception.getLineNumber());
			} else {
				Iterator iter = config.getBeans().iterator();
				while (iter.hasNext()) {
					IBean bean = (IBean) iter.next();
					addBean(this, bean);
				}
			}
		}
	}

	private void addBean(ConfigNode config, IBean bean) {
		BeanNode beanNode = new BeanNode(config, bean.getElementName());
		initBeanNode(beanNode, bean);
		beans.add(beanNode);
	}

	private void initBeanNode(BeanNode beanNode, IBean bean) {
		beanNode.setBean(bean);

		// Add constructor arguments
		Iterator cargs = bean.getConstructorArguments().iterator();
		while (cargs.hasNext()) {
			IBeanConstructorArgument carg = (IBeanConstructorArgument)
																   cargs.next();
			beanNode.addConstructorArgument(new ConstructorArgumentNode(
																beanNode, carg));
		}

		// Add properties
		Iterator props = bean.getProperties().iterator();
		while (props.hasNext()) {
			IBeanProperty prop = (IBeanProperty) props.next();
			beanNode.addProperty(new PropertyNode(beanNode, prop));
		}

		// Add inner beans
		Iterator inner = bean.getInnerBeans().iterator();
		while (inner.hasNext()) {
			IBean innerBean = (IBean) inner.next();
			BeanNode innerBeanNode = new BeanNode(beanNode,
												  innerBean.getElementName());
			initBeanNode(innerBeanNode, innerBean);
			beanNode.addInnerBean(innerBeanNode);
		}
	}
}
