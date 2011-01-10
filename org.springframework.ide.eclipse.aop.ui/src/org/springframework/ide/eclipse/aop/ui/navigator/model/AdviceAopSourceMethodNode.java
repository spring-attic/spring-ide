/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
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

import org.springframework.ide.eclipse.aop.core.model.IAopReference;

/**
 * @author Christian Dupuis
 */
public class AdviceAopSourceMethodNode extends AbstractJavaElementReferenceNode
		implements IReferenceNode, IRevealableReferenceNode {

	private List<IAopReference> reference;

	public AdviceAopSourceMethodNode(List<IAopReference> reference) {
		super(reference.get(0).getSource());
		this.reference = reference;
	}

	public IReferenceNode[] getChildren() {
		return new IReferenceNode[] { new AdviceAopReferenceNode(this.reference) };
	}
}
