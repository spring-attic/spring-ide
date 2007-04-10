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
package org.springframework.ide.eclipse.ui;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.viewers.DecoratingWorkbenchTreePathLabelProvider;

/**
 * This {@link DecoratingWorkbenchTreePathLabelProvider} knows about Spring
 * projects.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringUILabelProvider extends
		DecoratingWorkbenchTreePathLabelProvider {

	public SpringUILabelProvider(boolean isDecorating) {
		super(isDecorating);
	}

	@Override
	protected int getSeverity(Object element, Object parentElement) {
		if (element instanceof ISpringProject) {
			return MarkerUtils.getHighestSeverityFromMarkersInRange(
					((ISpringProject) element).getProject(), -1, -1);
		}
		return super.getSeverity(element, parentElement);
	}

	@Override
	protected Image getImage(Object element, Object parentElement,
			int severity) {
		if (element instanceof ISpringProject) {
			return SpringUIUtils.getDecoratedImage(SpringUIImages
					.getImage(SpringUIImages.IMG_OBJS_PROJECT), severity);
		}
		return super.getImage(element, parentElement, severity);
	}

	@Override
	protected String getText(Object element, Object parentElement,
			int severity) {
		if (element instanceof ISpringProject) {
			return ((ISpringProject) element).getProject().getName();
		}
		return super.getText(element, parentElement, severity);
	}
}
