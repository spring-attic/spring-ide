/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} implementation that can be used to link to {@link IMethod}.
 * <p>
 * Subclasses need to implement the {@link #calculateType} to return the parent type of the method to link to.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class MethodHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		IType type = calculateType(name, target, node, parentNode, document);
		try {
			IMethod method = Introspector.findMethod(type, target, -1, Public.DONT_CARE, Static.DONT_CARE);
			if (method != null) {
				return new JavaElementHyperlink(hyperlinkRegion, method);
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	/**
	 * Return the {@link IType} instances that should be used to query for the {@link IMethod} identified by the given
	 * name <code>target</code>.
	 */
	protected abstract IType calculateType(String name, String target, Node node, Node parentNode, IDocument document);

}
