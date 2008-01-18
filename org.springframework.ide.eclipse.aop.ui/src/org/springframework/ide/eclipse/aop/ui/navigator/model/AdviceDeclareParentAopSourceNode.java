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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAnnotationAopDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdviceDeclareParentAopSourceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private List<IAopReference> references;

	public AdviceDeclareParentAopSourceNode(List<IAopReference> reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		if (references.get(0).getDefinition() instanceof IAnnotationAopDefinition) {
			return new IReferenceNode[] { new AdviceDeclareParentAopSourceFieldNode(
					references) };

		}
		else {
			return new IReferenceNode[] { new AdviceDeclareParentAopReferenceNode(
					references) };
		}
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_INTRODUCTION);
	}

	public String getText() {
		String text = "";
		text += "declare parents:";
		text += " implements "
				+ ((IIntroductionDefinition) references.get(0).getDefinition())
						.getImplInterfaceName();
		text += " <";
		text += references.get(0).getDefinition().getAspectName();
		text += "> - ";
		text += ModelUtils.getFilePath(getResource());
		return text;
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		IResource resource = references.get(0).getDefinition().getResource();
		SpringUIUtils.openInEditor((IFile) resource, references.get(0)
				.getDefinition().getAspectStartLineNumber());
	}

	public IAopReference getReference() {
		return this.references.get(0);
	}

	public int getLineNumber() {
		return references.get(0).getDefinition().getAspectStartLineNumber();
	}

	public IResource getResource() {
		return references.get(0).getResource();
	}
	
	public Object getReferenceParticipant() {
		return references.get(0).getDefinition();
	}
}
