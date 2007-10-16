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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.w3c.dom.Node;

/**
 * Implementations of this interface are fine-grained objects responsible for
 * calculating hyperlinks.
 * <p>
 * Normally a hyperlink detector would extend the
 * {@link NamespaceHyperlinkDetectorSupport} class and register these
 * {@link IHyperlinkCalculator} instances in the
 * {@link NamespaceHyperlinkDetectorSupport#init()} method.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public interface IHyperlinkCalculator {
	
	/**
	 * Detect a hyperlink under the current context.
	 * @param name the name of the attribute
	 * @param target the value of the attribute
	 * @param node the node
	 * @param parentNode the parent node
	 * @param document the containing document
	 * @param textViewer the current {@link ITextViewer}
	 * @param hyperlinkRegion the region the hyperlink request was triggered
	 * @param cursor the current cursor region
	 */
	IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor);
}
