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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * This class defines a Spring beans config set (a list of beans config names).
 */
public class BeansConfigSet extends BeansModelElement implements IBeansConfigSet {

	private List configNames;
	private boolean allowBeanDefinitionOverriding;
	private Map beansMap;

	public BeansConfigSet(IBeansProject project, String name) {
		this(project, name, new ArrayList());
	}

	public BeansConfigSet(IBeansProject project, String name, List configNames) {
		super(project, name);
		this.allowBeanDefinitionOverriding = true;
		this.configNames = new ArrayList(configNames); 
	}

	public int getElementType() {
		return CONFIG_SET;
	}

	public IResource getElementResource() {
		return getElementParent().getElementResource();
	}

	public void setAllowBeanDefinitionOverriding(
										boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
		beansMap = null;
	}

	public boolean isAllowBeanDefinitionOverriding() {
		return allowBeanDefinitionOverriding;
	}

	public void addConfig(String configName) {
		if (configName.length() > 0 && !configNames.contains(configName)) {
			configNames.add(configName);
			beansMap = null;
		}
	}

	public boolean hasConfig(String configName) {
		return configNames.contains(configName);
	}

	public boolean hasConfig(IFile file) {
		return configNames.contains(file.getProjectRelativePath().toString());
	}

	public void removeConfig(String configName) {
		configNames.remove(configName);
	}

	public Collection getConfigs() {
		return configNames;
	}

	public boolean hasBean(String name) {
		if (beansMap == null) {

			// Lazily initialization of beans map
			beansMap = getBeansMap();
		}
		return beansMap.containsKey(name);
	}

	public IBean getBean(String name) {
		if (beansMap == null) {

			// Lazily initialization of beans map
			beansMap = getBeansMap();
		}
		return (IBean) beansMap.get(name);
	}

	public String toString() {
		return getElementName() + ": " + configNames.toString();
	}

	private Map getBeansMap() {
		Map beansMap = new HashMap();
		IBeansProject project = (IBeansProject) getElementParent();
		Iterator iter = configNames.iterator();
		while (iter.hasNext()) {
			String configName = (String) iter.next();
			IBeansConfig config = project.getConfig(configName);
			for (Iterator beans = config.getBeans().iterator();
															 beans.hasNext();) {
				IBean bean = (IBean) beans.next();
				if (allowBeanDefinitionOverriding ||
								 !beansMap.containsKey(bean.getElementName())) {
					beansMap.put(bean.getElementName(), bean);
				}
				
			}
		}
		return beansMap;
	}

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet#replaceConfig(org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile)
     */
    public void replaceConfig(String origFileName, String newFileName)
    {
        removeConfig(origFileName);
        addConfig(newFileName);
        
    }
}
