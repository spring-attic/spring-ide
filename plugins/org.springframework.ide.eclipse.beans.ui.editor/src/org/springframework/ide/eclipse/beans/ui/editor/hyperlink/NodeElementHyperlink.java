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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * This implementation of <code>IHyperlink</code> represents a link to a node within the same file.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class NodeElementHyperlink implements IHyperlink {

	private final IRegion region;

	private final IRegion targetRegion;

	private final ITextViewer viewer;

	public NodeElementHyperlink(IRegion region, IRegion targetRegion, ITextViewer viewer) {
		this.region = region;
		this.targetRegion = targetRegion;
		this.viewer = viewer;
	}

	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return "Navigate";
	}

	public void open() {
		viewer.setSelectedRange(targetRegion.getOffset(), 0);
		viewer.revealRange(targetRegion.getOffset(), targetRegion.getLength());
	}
}
