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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.ide.eclipse.beans.core.internal.parser.EventBeanDefinitionReader;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.resources.FileResource;

/**
 * This class defines a Spring beans configuration.
 */
public class BeansConfig extends BeansModelElement implements IBeansConfig {

	private IFile file;
	private List beans;
	private Map beansMap;
	private List innerBeans;
	private Map beanClassesMap;
	private BeanDefinitionException exception;

	public BeansConfig(IBeansProject project, String name) {
		super(project, name);
		this.file = getFile(name);
	}

	public int getElementType() {
		return CONFIG;
	}

	public IResource getElementResource() {
		return file;
	}

	/**
	 * Sets internal list of <code>IBean</code>s to <code>null</code>.
	 * Any further access to the data of this instance of
	 * <code>IBeansConfig</code> leads to reloading of this beans config file.
	 */
	public void reset() {
		this.beans = null;
		this.beansMap = null;
		this.innerBeans = null;
		this.beanClassesMap = null;
		this.exception = null;
	}

	public IFile getConfigFile() {
		return file;
	}

	public String getConfigPath() {
		return (file != null ? file.getFullPath().toString() : null);
	}

	public boolean hasBean(String name) {
		return getBeansMap().containsKey(name);
	}

	public IBean getBean(String name) {
		return (IBean) getBeansMap().get(name);
	}

	public Collection getBeans() {
		if (beans == null) {

			// Lazily initialization of beans list
			readConfig();
		}
		return beans;
	}

	public Collection getBeans(String className) {
		if (isBeanClass(className)) {
			return (Collection) getBeanClassesMap().get(className);
		}
		return Collections.EMPTY_LIST;
	}

	public Collection getInnerBeans() {
		if (innerBeans == null) {

			// Lazily initialization of inner beans list
			readConfig();
		}
		return innerBeans;
	}

	public boolean isBeanClass(String className) {
		return getBeanClassesMap().containsKey(className);
	}

	public Collection getBeanClasses() {
		return getBeanClassesMap().keySet();
	}

	public BeanDefinitionException getException() {
		if (beans == null) {

			// Lazily initialization of beans list
			readConfig();
		}
		return exception;
	}

	public String toString() {
		return getElementName() + ": " + getBeans().toString();
	}

	/**
	 * Returns the file for given name. If the given name defines an external
	 * resource (leading '/' -> not part of the project this config belongs to)
	 * get the file from the workspace else from the project.
	 * @return the file for given name
	 */
	private IFile getFile(String name) {
		IContainer container;
		if (name.charAt(0) == '/') {
			container = ResourcesPlugin.getWorkspace().getRoot();
		} else {
			container = (IProject) getElementParent().getElementResource();
		}
		return (IFile) container.findMember(name);
	}

	private void readConfig() {
		BeansConfigHandler handler = new BeansConfigHandler(this);
		EventBeanDefinitionReader reader = new EventBeanDefinitionReader(
																	   handler);
		try {
			reader.loadBeanDefinitions(new FileResource(file));
		} catch (BeanDefinitionException e) {
			exception = e;
		}
		beans = handler.getBeans();
		innerBeans = handler.getInnerBeans();
	}

	/**
	 * Returns lazily initialized map with all beans defined in this config.
	 */
	private Map getBeansMap() {
		if (beansMap == null) {
			Map beansMap = new HashMap();
			Iterator iter = getBeans().iterator();
			while (iter.hasNext()) {
				IBean bean = (IBean) iter.next();
				beansMap.put(bean.getElementName(), bean);
			}
		}
		return beansMap;
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config.
	 */
	private Map getBeanClassesMap() {
		if (beanClassesMap == null) {
			beanClassesMap = new HashMap();
			Iterator beans = getBeans().iterator();
			while (beans.hasNext()) {
				IBean bean = (IBean) beans.next();
				if (bean.isRootBean()) {
					String className = bean.getClassName();
					List beanClassBeans = (List) beanClassesMap.get(className);
					if (beanClassBeans == null) {
						beanClassBeans = new ArrayList();
						beanClassesMap.put(className, beanClassBeans);
					}
					beanClassBeans.add(bean);
				}
			}
		}
		return beanClassesMap;
	}
}
