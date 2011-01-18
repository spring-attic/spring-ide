/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.integration.ui.namespaces;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.integration.IntegrationUiImages;

/**
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class FlexNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		return IntegrationUiImages.getImage(IntegrationUiImages.IMG_OBJS_FLEX);
	}
	
}
