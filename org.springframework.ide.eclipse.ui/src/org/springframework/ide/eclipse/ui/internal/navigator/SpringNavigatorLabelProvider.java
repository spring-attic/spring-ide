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
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.SpringUIImages;
import org.springframework.ide.eclipse.ui.SpringUILabelProvider;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * This {@link ICommonLabelProvider} knows about the Spring projects.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringNavigatorLabelProvider extends SpringUILabelProvider implements
		ICommonLabelProvider {

	private static final String SPRING_EXPLORER_LABEL = "Spring Explorer";

	private static final String SPRING_ELEMENTS_LABEL = "Spring Elements";
	
	private String providerId;

	public SpringNavigatorLabelProvider() {
		super(true);
	}

	public void init(ICommonContentExtensionSite config) {
		providerId = config.getExtension().getId();
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}

	public String getDescription(Object element) {
		if (element instanceof ISpringProject) {
			if (SpringUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID.equals(providerId)) {
				return new StringBuilder().append(SPRING_ELEMENTS_LABEL).append(" - ").append(
						((ISpringProject) element).getProject().getName()).toString();
			}
			else if (SpringUIPlugin.SPRING_EXPLORER_CONTENT_PROVIDER_ID.equals(providerId)) {
				return ((ISpringProject) element).getElementName();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return providerId;
	}

	@Override
	protected Image getImage(Object element, Object parentElement) {
		if (element instanceof ISpringProject
				&& SpringUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID.equals(providerId)) {
			return SpringUIImages.getImage(SpringUIImages.IMG_OBJS_SPRING);
		}
		return super.getImage(element, parentElement);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof ISpringProject
				&& SpringUIPlugin.PROJECT_EXPLORER_CONTENT_PROVIDER_ID.equals(providerId)) {
			return SPRING_ELEMENTS_LABEL;
		}
		else if (element instanceof IWorkspaceRoot) {
			return SPRING_EXPLORER_LABEL;
		}
		return super.getText(element, parentElement);
	}
}
