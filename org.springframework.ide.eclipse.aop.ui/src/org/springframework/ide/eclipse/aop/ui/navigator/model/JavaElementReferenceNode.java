/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.aop.ui.navigator.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

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
}