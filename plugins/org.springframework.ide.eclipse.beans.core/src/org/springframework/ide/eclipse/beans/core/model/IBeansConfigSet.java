/*******************************************************************************
 * Copyright (c) 2004, 2011 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring beans config set (a list of
 * {@link IBeansConfig}s).
 * @author Torsten Juergeleit
 * @author Dave Watkins
 * @author Christian Dupuis
 */
public interface IBeansConfigSet extends IBeansModelElement,
		IResourceModelElement, IBeanClassAware {

	enum Type {
		MANUAL, AUTO_DETECTED
	}
	
	/** Name prefix of a config which belongs to a different project */
	char EXTERNAL_CONFIG_NAME_PREFIX = '/';

	boolean isAllowAliasOverriding();

	boolean isAllowBeanDefinitionOverriding();

	boolean isIncomplete();

	
	boolean hasConfig(BeansConfigId configId);
	
//	boolean hasConfig(String configName);
//
//	boolean hasConfig(IFile file);

	Set<IBeansConfig> getConfigs();

	Set<BeansConfigId> getConfigIds();

	public boolean hasAlias(String name);

	public IBeanAlias getAlias(String name);

	public Set<IBeanAlias> getAliases();

	Set<IBeansComponent> getComponents();

	boolean hasBean(String name);

	IBean getBean(String name);

	public Set<IBean> getBeans();
	
	/**
	 * Type of this configuration file. Could either be manual or automatic configured
	 * @since 2.0.5
	 */
	Type getType();
	
	/**
	 * Returns the list of configured profiles.
	 * @since 2.8.0 
	 */
	Set<String> getProfiles();
	
	/**
	 * Checks if this config set has configured profiles.
	 * @since 2.8.0 
	 */
	boolean hasProfiles();
}
