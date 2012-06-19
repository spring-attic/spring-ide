/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.refresh;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansConfigValidator;
import org.springframework.ide.eclipse.core.project.IProjectContributor;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerAdapter;

/**
 * @author Christian Dupuis
 * @since 2.1.1
 */
public class RefreshProjectContributionEventListener extends ProjectContributionEventListenerAdapter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishContributor(IProjectContributor contributor, Set<IResource> affectedResources) {
		if (contributor instanceof BeansConfigValidator && affectedResources != null && affectedResources.size() > 0) {
			RefreshUtils.refreshEditors(affectedResources);
		}
	}
}
