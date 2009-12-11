/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.project.IProjectContributor;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;

/**
 * State object that is registered in the {@link IProjectContributorState} and therefore for other
 * {@link IProjectContributor}s. The aim of this particular state is to capture calculations of bean
 * classes and their implements and extends hierarchy.
 * <p>
 * This dependencies are required to figure out what XML configuration needs re-building based on a
 * change to a java source file.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class BeansTypeHierachyState {

	/** All {@link IBean} instances that are affected by a particular java source file */
	private Map<IResource, Set<IBean>> beansByType = new ConcurrentHashMap<IResource, Set<IBean>>();

	/** All {@link IBeansConfig} instances that are affected by a particular java source file */
	private Map<IResource, Set<IBeansConfig>> configsByType = new ConcurrentHashMap<IResource, Set<IBeansConfig>>();

	/**
	 * Returns all {@link IBean} that need re-processing on change to the given {@link IResource}
	 * representing a java source file.
	 * <p>
	 * This implementation delegates to {@link BeansModelUtils#getBeansByContainingTypes(IResource)}
	 * and caches the result for the execution of a {@link IProjectContributor} execution.
	 * @param resource the java source file which has potentially been changed.
	 * @return a set of {@link IBean} affected by a change to the given java source file
	 */
	public Set<IBean> getBeansByContainingTypes(IResource resource) {
		// First check the internal cache for faster access
		if (!beansByType.containsKey(resource)) {
			// Only if not in cache do the calculation
			beansByType.put(resource, BeansModelUtils.getBeansByContainingTypes(resource));
		}
		return beansByType.get(resource);
	}

	/**
	 * Returns all {@link IBeansConfig} that need re-processing on change to the given
	 * {@link IResource} representing a java source file.
	 * <p>
	 * This implementation delegates to
	 * {@link BeansModelUtils#getConfigsByContainingTypes(IResource)} and caches the result for the
	 * execution of a {@link IProjectContributor} execution.
	 * @param resource the java source file which has potentially been changed.
	 * @return a set of {@link IBeansConfig} affected by a change to the given java source file
	 */
	public Set<IBeansConfig> getConfigsByContainingTypes(IResource resource) {
		// First check the internal cache for faster access
		if (!configsByType.containsKey(resource)) {
			// Only if not in cache do the calculation
			configsByType.put(resource, BeansModelUtils.getConfigsByContainingTypes(resource));
		}
		return configsByType.get(resource);
	}

}
