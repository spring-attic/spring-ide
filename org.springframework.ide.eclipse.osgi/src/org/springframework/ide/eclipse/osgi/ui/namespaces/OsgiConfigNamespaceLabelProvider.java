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
package org.springframework.ide.eclipse.osgi.ui.namespaces;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.osgi.OsgiUIImages;

/**
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiConfigNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		if (element instanceof IBean
				&& (context instanceof IBeansConfig
						|| context instanceof IBeansConfigSet)) {
			return OsgiUIImages.getImage(OsgiUIImages.IMG_OBJS_OSGI);
		}
		return super.getImage(element, context, isDecorating);
	}
}
