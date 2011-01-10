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
package org.springframework.ide.eclipse.osgi.ui.editor.outline.blueprint;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.editor.outline.bean.BeansOutlineLabelProvider;
import org.springframework.ide.eclipse.osgi.OsgiUIImages;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class BlueprintOutlineLabelProvider extends BeansOutlineLabelProvider {

	@Override
	public Image getImage(Object object) {
		// Create Spring beans label image
		Node node = (Node) object;
		String nodeName = node.getLocalName();
		
		if (nodeName.equals("blueprint")) {
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
		else if (nodeName.equals("type-converters")) {
			return OsgiUIImages.getImage(OsgiUIImages.IMG_BLUEPRINT_OBJS_OSGI);
		}
		return OsgiUIImages.getImage(OsgiUIImages.IMG_BLUEPRINT_OBJS_OSGI);
	}
	
}
