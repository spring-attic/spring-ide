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

import org.eclipse.jface.action.Action;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class DeleteNodeAction extends Action {

	private final StructuredTextViewer textView;

	private final IDOMNode node;

	private final ShallowFormatProcessorXML formatter;

	public DeleteNodeAction(StructuredTextViewer textView, IDOMNode node) {
		super();
		this.textView = textView;
		this.node = node;
		formatter = new ShallowFormatProcessorXML();
		setText(Messages.getString("DeleteNodeAction.DELETE_ELEMENT_PREFIX") + node.getNodeName() + Messages.getString("DeleteNodeAction.DELETE_ELEMENT_SUFFIX")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean isEnabled() {
		if (node == null) {
			return false;
		}
		Element root = node.getOwnerDocument().getDocumentElement();
		if (node.equals(root)) {
			return false;
		}
		return super.isEnabled();
	}

	@Override
	public void run() {
		IDOMModel model = node.getModel();
		Node parent = node.getParentNode();
		if (textView != null && model != null && parent != null) {
			model.beginRecording(textView);
			parent.removeChild(node);
			formatter.formatNode(parent);
			model.endRecording(textView);
		}
	}

}
