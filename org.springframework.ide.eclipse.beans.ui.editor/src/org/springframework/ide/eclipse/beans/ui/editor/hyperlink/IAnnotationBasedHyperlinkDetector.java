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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.w3c.dom.Node;

/**
 * Similar interface to {@link IHyperlinkDetector}. The
 * {@link #detectHyperlinks} takes an additional argument for the annotation
 * node.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public interface IAnnotationBasedHyperlinkDetector {
	
	/**
	 * Detect hyperlinks based on given annotation
	 * @param textViewer the text viewer
	 * @param region the region 
	 * @param canShowMultipleHyperlinks
	 * @param annotation the resolved annotation for the hovered attribute
	 */
	IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region,
			boolean canShowMultipleHyperlinks, Node annotation);
}
