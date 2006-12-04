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

package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring beans config set (a list of
 * beans configs).
 * @author Torsten Juergeleit
 */
public interface IBeansConfigSet extends IResourceModelElement,
			IBeanClassAware {
	/** Name prefix of a config which belongs to a different project */
	char EXTERNAL_CONFIG_NAME_PREFIX = '/';

	boolean isAllowBeanDefinitionOverriding();

	boolean isIncomplete();

	boolean hasConfig(String configName);

	boolean hasConfig(IFile file);

	Set<IBeansConfig> getConfigs();

	Set<String> getConfigNames();

	public boolean hasAlias(String name);

	public IBeanAlias getAlias(String name);

	public Set<IBeanAlias> getAliases();

	Set<IBeansComponent> getComponents();

	boolean hasBean(String name);

	IBean getBean(String name);

	public Set<IBean> getBeans();
}
