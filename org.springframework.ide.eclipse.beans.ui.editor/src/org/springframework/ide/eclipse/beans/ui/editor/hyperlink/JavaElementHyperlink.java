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
				IJavaElement element = elements[0];
				try {
					JavaUI
							.revealInEditor(JavaUI.openInEditor(element),
									element);
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
