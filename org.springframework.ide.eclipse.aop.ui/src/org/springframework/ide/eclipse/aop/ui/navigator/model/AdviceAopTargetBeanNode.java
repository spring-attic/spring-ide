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
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
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
		IBean bean = AopReferenceModelUtils
				.getBeanFromElementId(this.references.get(0).getTargetBeanId());
		if (bean != null) {
			return BeansUIPlugin.getLabelProvider().getImage(bean);
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ERROR);
		}
	}

	public String getText() {
		IBean bean = AopReferenceModelUtils
				.getBeanFromElementId(this.references.get(0).getTargetBeanId());
		if (bean != null) {
			return BeansUIPlugin.getLabelProvider().getText(bean) + " - "
					+ bean.getElementResource().getFullPath().toString();
		}
		else {
			return "<bean cannot be found>";
		}
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		IBean bean = AopReferenceModelUtils
				.getBeanFromElementId(this.references.get(0).getTargetBeanId());
		if (bean != null) {
			SpringUIUtils.openInEditor((IFile) bean.getElementResource(), bean
					.getElementEndLine());
		}
	}

	public IAopReference getReference() {
		return this.references.get(0);
	}

	public int getLineNumber() {
		IBean bean = AopReferenceModelUtils
				.getBeanFromElementId(this.references.get(0).getTargetBeanId());
		if (bean != null) {
			return bean.getElementStartLine();
		}
		else {
			return -1;
		}
	}

	public IBean getTargetBean() {
		return AopReferenceModelUtils.getBeanFromElementId(this.references.get(
				0).getTargetBeanId()); 
	}

	public IResource getResource() {
		return references.get(0).getResource();
	}

}
