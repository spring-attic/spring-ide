/*******************************************************************************
 * Copyright (c) 2005, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.w3c.dom.Node;

/**
 * This implementation of <code>IHyperlink</code> represents a link to a node within the same file.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Terry Denney
 */
public class NodeElementHyperlink implements IHyperlink {

	private final IRegion region;

	private final IRegion targetRegion;

	private final ITextViewer viewer;

	private final IFile file;

	private final Node bean;

	public NodeElementHyperlink(Node bean, IFile file, IRegion region, IRegion targetRegion, ITextViewer viewer) {
		this.bean = bean;
		this.file = file;
		this.region = region;
		this.targetRegion = targetRegion;
		this.viewer = viewer;
	}
	
	public NodeElementHyperlink(IRegion region, IRegion targetRegion, ITextViewer viewer) {
		this(null, null, region, targetRegion, viewer);
	}

	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		if (bean != null && file != null) {
			try {
				StringBuilder str = new StringBuilder();
				str.append("Navigate to ");
				str.append(viewer.getDocument().get(region.getOffset(), region.getLength()));
				Node parentNode = bean.getParentNode();
				if (parentNode != null) {
					Node profileNode = parentNode.getAttributes().getNamedItem("profile");
					
					if (profileNode != null) {
						str.append(" in profile ");
						str.append(profileNode.getNodeValue());
					}
				}
				str.append(" - ");
				str.append(file.getName());
				return str.toString();
			} catch (BadLocationException e) {
				
			}
		}
		return "Navigate";
	}

	public void open() {
		viewer.setSelectedRange(targetRegion.getOffset(), 0);
		viewer.revealRange(targetRegion.getOffset(), targetRegion.getLength());
	}
}
