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
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdvisedAopSourceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private IAopReference references;

	public AdvisedAopSourceNode(IAopReference reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		return new IReferenceNode[] { new AdvisedAopSourceMethodNode(references) };
	}

	public Image getImage() {
		ADVICE_TYPE type = references.getAdviceType();
		if (type == ADVICE_TYPE.AFTER || type == ADVICE_TYPE.AFTER_RETURNING
				|| type == ADVICE_TYPE.AFTER_THROWING) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AFTER_ADVICE);
		}
		else if (type == ADVICE_TYPE.BEFORE) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEFORE_ADVICE);
		}
		else if (type == ADVICE_TYPE.AROUND) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_AROUND_ADVICE);
		}
		else if (type == ADVICE_TYPE.DECLARE_PARENTS) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_INTRODUCTION);
		}
		return null;
	}

	public String getText() {
		ADVICE_TYPE type = references.getAdviceType();
		String text = "";
		if (type == ADVICE_TYPE.AFTER) {
			text += "after()";
		}
		else if (type == ADVICE_TYPE.AFTER_RETURNING) {
			text += "after-returning()";
		}
		else if (type == ADVICE_TYPE.AFTER_THROWING) {
			text += "after-throwing()";
		}
		else if (type == ADVICE_TYPE.BEFORE) {
			text += "before()";
		}
		else if (type == ADVICE_TYPE.AROUND) {
			text += "around()";
		}
		else if (type == ADVICE_TYPE.DECLARE_PARENTS) {
			text += "declare parents: implements "
					+ ((IIntroductionDefinition) references.getDefinition())
							.getImplInterfaceName();
		}
		text += " <";
		text += references.getDefinition().getAspectName();
		text += "> - ";
		text += ModelUtils.getFilePath(getResource());
		return text;
	}

	public boolean hasChildren() {
		return references.getSource() != null;
	}

	public void openAndReveal() {
		IResource resource = references.getDefinition().getResource();
		SpringUIUtils.openInEditor((IFile) resource, references.getDefinition()
				.getAspectStartLineNumber());
	}

	public IAopReference getReference() {
		return this.references;
	}

	public int getLineNumber() {
		return references.getDefinition().getAspectStartLineNumber();
	}

	public IResource getResource() {
		return references.getResource();
	}

	public Object getReferenceParticipant() {
		return references.getDefinition();
	}
}
