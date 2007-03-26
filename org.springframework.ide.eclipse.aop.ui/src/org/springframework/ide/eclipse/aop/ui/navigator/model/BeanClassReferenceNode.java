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
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

public class BeanClassReferenceNode implements IReferenceNode {

	private IReferenceNode child;

	public BeanClassReferenceNode(IReferenceNode child) {
		this.child = child;
	}

	public IReferenceNode[] getChildren() {
		return new IReferenceNode[] { child };
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
	}

	public String getText() {
		return "class";
	}

	public boolean hasChildren() {
		return true;
	}

}
