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
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.SpringUIImages;
import org.springframework.ide.eclipse.ui.SpringUILabelProvider;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * This {@link ICommonLabelProvider} knows about the Spring projects.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringNavigatorLabelProvider extends SpringUILabelProvider
		implements ICommonLabelProvider {

	private String providerID;

	public SpringNavigatorLabelProvider() {
		super(true);
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}

	public String getDescription(Object element) {
		if (element instanceof ISpringProject) {
			if (SpringUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				return "Spring Elements" // TODO Externalize string
						+ " - "
						+ ((ISpringProject) element).getProject().getName();
			}
			else if (SpringUIPlugin.SPRING_EXPLORER_CONTENT_PROVIDER_ID
					.equals(providerID)) {
				return ((ISpringProject) element).getElementName();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return providerID;
	}

	@Override
	protected Image getImage(Object element, Object parentElement) {
		if (element instanceof ISpringProject
				&& SpringUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID
						.equals(providerID)) {
			return SpringUIImages.getImage(SpringUIImages.IMG_OBJS_SPRING);
		}
		return super.getImage(element, parentElement);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof ISpringProject
				&& SpringCoreUtils.getAdapter(parentElement, IProject.class) != null) {
			return "Spring Elements"; // TODO Externalize string
		}
		else if (element instanceof IWorkspaceRoot) {
			return "Spring Explorer";
		}
		return super.getText(element, parentElement);
	}
}
