/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Implementations of this interface are able to calculate {@link IHyperlink} instances for a given attribute.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public interface INamespaceHyperlinkDetector {

	/**
	 * Init this {@link INamespaceHyperlinkDetector}.
	 */
	void init();

	/**
	 * Calculate {@link IHyperlink} instance for the given context.
	 * <p>
	 * Note: this method will only be called if {@link #isLinkableAttr(Attr)} returns true.
	 */
	IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor);

	/**
	 * Calculate multiple {@link IHyperlink} instances for the given context.
	 * <p>
	 * Note: this method will only be called if {@link #isLinkableAttr(Attr)} returns true.
	 */
	IHyperlink[] createHyperlinks(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor);

	/**
	 * Checks if this {@link INamespaceHyperlinkDetector} supports the hyperlink calculation for the given attribute.
	 */
	boolean isLinkableAttr(Attr attr);
}
