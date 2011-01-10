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
package org.springframework.ide.eclipse.blueprint.ui.namespaces;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.osgi.OsgiUIImages;

/**
 * {@link INamespaceLabelProvider} for the <code>blueprint</code> namespace.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class BlueprintNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		if (element instanceof IBean) {
			String nodeName = ModelUtils.getLocalName((IBean) element);
			if (nodeName.equals("blueprint")) {
				return OsgiUIImages.getImage(OsgiUIImages.IMG_BLUEPRINT_OBJS_OSGI);
			}
			if (nodeName.equals("type-converters")) {
				return OsgiUIImages.getImage(OsgiUIImages.IMG_BLUEPRINT_OBJS_OSGI);
			}
			else if (nodeName.equals("service")) {
				return OsgiUIImages.getImage(OsgiUIImages.IMG_SERVICE_OBJS_OSGI);
			}
			else if (nodeName.equals("reference")) {
				return OsgiUIImages.getImage(OsgiUIImages.IMG_REFERENCE_OBJS_OSGI);
			}
			else if (nodeName.equals("reference-list")) {
				return OsgiUIImages.getImage(OsgiUIImages.IMG_REFERENCE_OBJS_OSGI);
			}
			else if (nodeName.equals("bean")) {
				return OsgiUIImages.getImage(OsgiUIImages.IMG_COMPONENT_OBJS_OSGI);
			}
		}
		return super.getImage(element, context, isDecorating);
	}
}
