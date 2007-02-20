/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;

/**
 * Java element hyperlink.
 */
public class JavaElementHyperlink implements IHyperlink {

	private final IRegion region;

	private final IJavaElement[] elements;

	/**
	 * Creates a new Java element hyperlink.
	 */
	public JavaElementHyperlink(IRegion region, IJavaElement element) {
		this.region = region;
		this.elements = new IJavaElement[] { element };
	}

	/**
	 * Creates a new Java element hyperlink.
	 */
	public JavaElementHyperlink(IRegion region, IJavaElement[] element) {
		this.region = region;
		this.elements = element;
	}

	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	/**
	 * opens the standard Java Editor for the given IJavaElement
	 */
	public void open() {
		// TODO display selection dialog if element.length > 1
		if (elements != null && elements.length > 0) {
			if (elements[0] instanceof IJavaElement) {
				IJavaElement element = (IJavaElement) elements[0];
				try {
					JavaUI.revealInEditor(JavaUI.openInEditor(element), element);
				}
				catch (PartInitException e) {
				}
				catch (JavaModelException e) {
				}
			}
		}

	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return null;
	}
}
