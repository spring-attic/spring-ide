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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AdviceDeclareParentAopTargetNode implements IReferenceNode,
		IRevealableReferenceNode {

	private IAopReference reference;

	public AdviceDeclareParentAopTargetNode(IAopReference reference) {
		this.reference = reference;
	}

	public IReferenceNode[] getChildren() {
		return new IReferenceNode[] { new AdviceDeclareParentAopTargetMethodNode(
				reference) };
	}

	public Image getImage() {
		return BeansUIPlugin.getLabelProvider().getImage(
				this.reference.getTargetBean());
	}

	public String getText() {
		return BeansUIPlugin.getLabelProvider().getText(
				this.reference.getTargetBean())
				+ " - "
				+ this.reference.getTargetBean().getElementResource()
						.getFullPath().toString();
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		IResource resource = this.reference.getTargetBean()
				.getElementResource();
		SpringUIUtils.openInEditor((IFile) resource, this.reference
				.getTargetBean().getElementStartLine());
	}

	public int getLineNumber() {
		return AopReferenceModelNavigatorUtils.getLineNumber(reference
				.getTarget());
	}

	public IResource getResource() {
		return reference.getTarget().getResource();
	}

}
