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
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdviceRootAopReferenceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private List<IAopReference> reference;

	public AdviceRootAopReferenceNode(List<IAopReference> reference) {
		this(reference, false);
	}

	public AdviceRootAopReferenceNode(List<IAopReference> reference,
			boolean isBeanConfig) {
		this.reference = reference;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		for (IAopReference r : reference) {
			nodes.add(new AdviceDeclareParentAopTargetMethodNode(r));
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public Image getImage() {
		ADVICE_TYPES type = reference.get(0).getAdviceType();
		if (type == ADVICE_TYPES.AFTER || type == ADVICE_TYPES.AFTER_RETURNING
				|| type == ADVICE_TYPES.AFTER_THROWING) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		else if (type == ADVICE_TYPES.BEFORE) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEFORE_ADVICE);
		}
		else if (type == ADVICE_TYPES.AROUND) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AROUND_ADVICE);
		}
		else if (type == ADVICE_TYPES.DECLARE_PARENTS) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_INTRODUCTION);
		}
		return null;
	}

	public String getText() {
		ADVICE_TYPES type = reference.get(0).getAdviceType();
		String text = "";
		if (type == ADVICE_TYPES.AFTER) {
			text += "after()";
		}
		else if (type == ADVICE_TYPES.AFTER_RETURNING) {
			text += "after-returning()";
		}
		else if (type == ADVICE_TYPES.AFTER_THROWING) {
			text += "after-throwing()";
		}
		else if (type == ADVICE_TYPES.BEFORE) {
			text += "before()";
		}
		else if (type == ADVICE_TYPES.AROUND) {
			text += "around()";
		}
		else if (type == ADVICE_TYPES.DECLARE_PARENTS) {
			text += "declare parents: implements "
					+ ((IIntroductionDefinition) reference.get(0)
							.getDefinition()).getImplInterfaceName();
		}
		text += " <";
		text += reference.get(0).getDefinition().getAspectName();
		text += "> - ";
		text += reference.get(0).getDefinition().getResource().getFullPath()
				.toString();
		return text;
	}

	public List<IAopReference> getReference() {
		return reference;
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		IResource resource = reference.get(0).getDefinition().getResource();
		SpringUIUtils.openInEditor((IFile) resource, reference.get(0)
				.getDefinition().getAspectStartLineNumber());
	}

	public int getLineNumber() {
		return reference.get(0).getDefinition().getAspectStartLineNumber();
	}

	public IResource getResource() {
		return reference.get(0).getResource();
	}
	
	public Object getReferenceParticipant() {
		return reference.get(0).getDefinition();
	}
}
