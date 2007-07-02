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
package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractValidationAction;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * Extension of {@link AbstractValidationAction} that determines the
 * {@link IWebflowModel} specific elements for a selected object.
 * @author Christian Dupuis
 * @since 2.0.1
 * @see #getResourcesFromSelectedObject(Object)
 */
public class WebflowValidationAction extends AbstractValidationAction {

	public WebflowValidationAction(ICommonActionExtensionSite site) {
		super(site);
	}

	@Override
	protected Set<IResource> getResourcesFromSelectedObject(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof IWebflowProject) {
			for (IWebflowConfig bc : ((IWebflowProject) object).getConfigs()) {
				resources.add(bc.getElementResource());
			}
		}
		return resources;
	}
}
