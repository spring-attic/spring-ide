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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class AdviceAopTargetNode implements IReferenceNode,
		IRevealableReferenceNode {

	private List<IAopReference> references;

	public AdviceAopTargetNode(List<IAopReference> reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		Map<IAspectDefinition, List<IAopReference>> refs = new HashMap<IAspectDefinition, List<IAopReference>>();
		for (IAopReference r : this.references) {
			if (refs.containsKey(r.getDefinition())) {
				refs.get(r.getDefinition()).add(r);
			}
			else {
				List<IAopReference> ref = new ArrayList<IAopReference>();
				ref.add(r);
				refs.put(r.getDefinition(), ref);
			}
		}
		for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : refs
				.entrySet()) {
			nodes.add(new AdviceAopReferenceNode(entry.getValue()));
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
		// return new IReferenceNode[] {new AdviceAopReferenceNode(references)};
	}

	public Image getImage() {
		ADVICE_TYPES type = references.get(0).getAdviceType();
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
		ADVICE_TYPES type = references.get(0).getAdviceType();
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
		text += " <";
		text += references.get(0).getDefinition().getAspectName();
		text += "> -";
		text += references.get(0).getDefinition().getResource().getFullPath()
				.toString();
		return text;
	}

	public boolean hasChildren() {
		return true;
	}

	public void openAndReveal() {
		IResource resource = references.get(0).getDefinition().getResource();
		SpringUIUtils.openInEditor((IFile) resource, references.get(0)
				.getDefinition().getAspectLineNumber());
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

	public Object getReferenceParticipant() {
		return references.get(0).getDefinition();
	}
}
