/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;

/**
 * Bordered Integration edit part that has an icon image combined from main
 * image and a little aux decoration icon
 *
 * @author Alex Boyko
 *
 */
public class BadgedIntegrationPart extends BorderedIntegrationPart {

	private final ImageDescriptor mainImageDescriptor;

	private final ImageDescriptor auxImageDescriptor;

	public BadgedIntegrationPart(Activity activity, ImageDescriptor mainImageDescriptor,
			ImageDescriptor auxImageDescriptor) {
		super(activity);
		this.mainImageDescriptor = mainImageDescriptor;
		this.auxImageDescriptor = auxImageDescriptor;
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(IntegrationImages.getImageWithBadge(mainImageDescriptor, auxImageDescriptor));
		return l;
	}

	@Override
	public Activity getModelElement() {
		return (Activity) getModel();
	}

}
