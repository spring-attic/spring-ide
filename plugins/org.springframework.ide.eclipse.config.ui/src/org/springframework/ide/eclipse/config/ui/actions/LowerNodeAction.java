/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.actions;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class LowerNodeAction extends TreeViewerNodeAction {

	private final StructuredTextViewer textView;

	public LowerNodeAction(TreeViewer treeViewer, SpringConfigContentAssistProcessor processor,
			StructuredTextViewer textView) {
		super(treeViewer, processor);
		this.textView = textView;
	}

	@Override
	public void run() {
		super.run();
		IDOMElement selection = getElementFromTreeItem(getSelectedTreeItem());
		IDOMElement nextSib = getElementFromTreeItem(getNextTreeItem());
		if (textView != null && selection != null) {
			IDOMModel model = selection.getModel();
			Node parent = selection.getParentNode();
			if (model != null && nextSib != null && parent != null) {
				model.beginRecording(textView);
				parent.insertBefore(nextSib, selection);
				formatter.formatNode(nextSib);
				formatter.formatNode(nextSib.getParentNode());
				model.endRecording(textView);
			}
		}
	}

}
