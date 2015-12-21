/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.security.ui.editor.outline.security;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.security.SecurityUIImages;
import org.w3c.dom.Node;

/**
 * Basic {@link JFaceNodeLabelProvider} label provider for the security namespace.
 * @author Christian Dupuis
 * @since 2.0.5
 */
@SuppressWarnings("restriction")
public class SecurityOutlineLabelProvider extends JFaceNodeLabelProvider {
	
	@Override
	public Image getImage(Object object) {
		return SecurityUIImages.getImage(SecurityUIImages.IMG_OBJS_SECURITY);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;
			return node.getLocalName();
		}
		return super.getText(element);
	}
	
}
