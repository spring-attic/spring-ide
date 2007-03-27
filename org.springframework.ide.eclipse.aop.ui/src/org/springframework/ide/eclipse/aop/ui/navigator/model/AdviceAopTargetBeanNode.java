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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AdviceAopTargetBeanNode implements IReferenceNode,
		IRevealableReferenceNode {

	private List<IAopReference> references;

	public AdviceAopTargetBeanNode(List<IAopReference> reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		for (IAopReference r : this.references) {
			nodes.add(new AdviceAopTargetMethodNode(r));
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public Image getImage() {
		return BeansUIPlugin.getLabelProvider().getImage(
				this.references.get(0).getTargetBean());
	}

	public String getText() {
		return BeansUIPlugin.getLabelProvider().getText(
				this.references.get(0).getTargetBean())
				+ " - "
				+ this.references.get(0).getTargetBean().getElementResource()
						.getFullPath().toString();
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		SpringUIUtils.openInEditor((IFile) references.get(0).getTargetBean()
				.getElementResource(), references.get(0).getTargetBean()
				.getElementEndLine());
	}

	public IAopReference getReference() {
		return this.references.get(0);
	}

	public int getLineNumber() {
		return references.get(0).getDefinition().getAspectLineNumber();
	}

	public IResource getResource() {
		return references.get(0).getResource();
	}

}
