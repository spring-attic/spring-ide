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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

public class AbstractJavaElementReferenceNode {

	protected IMember element;

	public AbstractJavaElementReferenceNode(IMember element) {
		this.element = element;
	}

	public Image getImage() {
		if (element != null) {
			return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
				.getImage(this.element);
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ERROR);
		}
	}

	public String getText() {
		return AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
				.getText(this.element)
				+ " - "
				+ AopReferenceModelUtils.getPackageLinkName(this.element);
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		IEditorPart p;
		try {
			p = JavaUI.openInEditor(this.element);
			JavaUI.revealInEditor(p, (IJavaElement) this.element);
		}
		catch (Exception e) {
		}
	}

	public int getLineNumber() {
		return AopReferenceModelNavigatorUtils.getLineNumber(this.element);
	}

	public IResource getResource() {
		return this.element.getResource();

	}

	public IMember getElement() {
		return element;
	}
	
	public Object getReferenceParticipant() {
		return element;
	}

}
