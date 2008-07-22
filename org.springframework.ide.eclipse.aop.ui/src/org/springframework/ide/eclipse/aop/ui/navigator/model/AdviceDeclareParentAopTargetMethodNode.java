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

import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;

public class AdviceDeclareParentAopTargetMethodNode extends
		AbstractJavaElementReferenceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private IAopReference reference;

	public AdviceDeclareParentAopTargetMethodNode(IAopReference reference) {
		super(reference.getTarget());
		this.reference = reference;
	}

	public IReferenceNode[] getChildren() {
		return new IReferenceNode[0];
	}

	@Override
	public String getText() {
		if (reference.getAdviceType() == ADVICE_TYPE.DECLARE_PARENTS) {
			return AopReferenceModelUtils.getJavaElementLinkName(reference
					.getTarget())
					+ " - "
					+ AopReferenceModelUtils.getPackageLinkName(reference
							.getTarget());
		}
		else {
			return AopReferenceModelUtils.getJavaElementLinkName(reference
					.getTarget().getParent())
					+ "."
					+ AopReferenceModelUtils.getJavaElementLinkName(reference
							.getTarget())
					+ " - "
					+ AopReferenceModelUtils.getPackageLinkName(reference
							.getTarget());
		}
	}

	@Override
	public boolean hasChildren() {
		return false;
	}
}
