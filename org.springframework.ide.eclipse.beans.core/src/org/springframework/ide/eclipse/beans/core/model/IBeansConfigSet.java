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
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring beans config set (a list of
 * {@link IBeansConfig}s).
 * 
 * @author Torsten Juergeleit
 */
public interface IBeansConfigSet extends IBeansModelElement,
		IResourceModelElement, IBeanClassAware {

	/** Name prefix of a config which belongs to a different project */
	char EXTERNAL_CONFIG_NAME_PREFIX = '/';

	boolean isAllowAliasOverriding();

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
