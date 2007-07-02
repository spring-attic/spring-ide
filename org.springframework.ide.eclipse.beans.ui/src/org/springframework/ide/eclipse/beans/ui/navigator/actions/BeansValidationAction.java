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
package org.springframework.ide.eclipse.beans.ui.navigator.actions;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractValidationAction;

/**
 * {@link AbstractNavigatorAction} extension that determines the
 * {@link IResource} instances to validate from a selected object.
 * @author Christian Dupuis
 * @since 2.0.1
 * @see #getResourcesFromSelectedObject(Object)
 */
public class BeansValidationAction extends AbstractValidationAction {

	public BeansValidationAction(ICommonActionExtensionSite site) {
		super(site);
	}

	@Override
	protected Set<IResource> getResourcesFromSelectedObject(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof IBeansConfigSet) {
			for (IBeansConfig bc : ((IBeansConfigSet) object).getConfigs()) {
				resources.add(bc.getElementResource());
			}
		}
		else if (object instanceof IBeansProject) {
			for (IBeansConfig bc : ((IBeansProject) object).getConfigs()) {
				resources.add(bc.getElementResource());
			}
		}
		else if (object instanceof IResourceModelElement) {
			resources.add(((IResourceModelElement) object).getElementResource());
		}
		return resources;
	}
}
