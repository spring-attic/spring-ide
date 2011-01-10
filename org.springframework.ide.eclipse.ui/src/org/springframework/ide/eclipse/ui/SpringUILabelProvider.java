/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.ui.viewers.DecoratingWorkbenchTreePathLabelProvider;

/**
 * This {@link DecoratingWorkbenchTreePathLabelProvider} knows about Spring
 * projects.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringUILabelProvider extends
		DecoratingWorkbenchTreePathLabelProvider {

	public SpringUILabelProvider(boolean isDecorating) {
		super(isDecorating);
	}

	@Override
	protected Image getImage(Object element, Object parentElement) {
		if (element instanceof ISpringProject) {
			Image image = SpringUIImages
					.getImage(SpringUIImages.IMG_OBJS_PROJECT);
			return image;
		}
		return super.getImage(element, parentElement);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof ISpringProject) {
			return ((ISpringProject) element).getProject().getName();
		}
		return super.getText(element, parentElement);
	}
}
