/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class AdvisedAopSourceMethodNode extends
		AbstractJavaElementReferenceNode implements IReferenceNode,
		IRevealableReferenceNode {

	private IAopReference reference;

	public AdvisedAopSourceMethodNode(IAopReference reference) {
		super(reference.getSource());
		this.reference = reference;
	}

	public IReferenceNode[] getChildren() {
		return new IReferenceNode[0];
	}

	@Override
	public String getText() {
		if (reference.getSource() != null) {
			if (reference.getAdviceType() == ADVICE_TYPE.DECLARE_PARENTS) {
				return AopReferenceModelUtils.getJavaElementLinkName(reference
						.getSource())
						+ " - "
						+ AopReferenceModelUtils.getPackageLinkName(reference
								.getSource());
			}
			else {
				return AopReferenceModelUtils.getJavaElementLinkName(reference
						.getSource().getParent())
						+ "."
						+ AopReferenceModelNavigatorUtils.JAVA_LABEL_PROVIDER
								.getText(reference.getSource())
						+ " - "
						+ AopReferenceModelUtils.getPackageLinkName(reference
								.getSource());
			}
		}
		else {
			return "";
		}
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	public IAopReference getReference() {
		return this.reference;
	}
}
