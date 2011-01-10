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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

/**
 * @author Christian Dupuis
 */
public class JavaElementReferenceNode implements IRevealableReferenceNode {

	private IJavaElement method;

	private boolean isRoot = false;

	public JavaElementReferenceNode(IJavaElement elem, boolean isRoot) {
		this.method = elem;
		this.isRoot = isRoot;
	}

	public IJavaElement getJavaElement() {
		return method;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void openAndReveal() {
		IEditorPart p;
		try {
			p = JavaUI.openInEditor(method);
			JavaUI.revealInEditor(p, method);
		}
		catch (Exception e) {
		}
	}

	public int getLineNumber() {
		if (method instanceof IMember) {
			return AopReferenceModelNavigatorUtils
					.getLineNumber((IMember) method);
		}
		else {
			return -1;
		}
	}

	public IResource getResource() {
		return method.getResource();
	}
	
	public Object getReferenceParticipant() {
		return method;
	}
}
