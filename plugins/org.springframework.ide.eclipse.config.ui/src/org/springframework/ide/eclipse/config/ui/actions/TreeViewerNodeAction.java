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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class TreeViewerNodeAction extends Action {

	protected TreeViewer treeViewer;

	private TreeItem selection;

	private TreeItem prevItem;

	private TreeItem nextItem;

	protected ShallowFormatProcessorXML formatter;

	protected SpringConfigContentAssistProcessor processor;

	public TreeViewerNodeAction(TreeViewer treeViewer, SpringConfigContentAssistProcessor processor) {
		this.treeViewer = treeViewer;
		this.processor = processor;
		formatter = new ShallowFormatProcessorXML();
	}

	protected IDOMElement getElementFromTreeItem(TreeItem item) {
		if (item != null) {
			Object data = item.getData();
			if (data != null && data instanceof IDOMElement) {
				return (IDOMElement) data;
			}
		}
		return null;
	}

	public TreeItem getNextTreeItem() {
		return nextItem;
	}

	public TreeItem getPreviousTreeItem() {
		return prevItem;
	}

	public TreeItem getSelectedTreeItem() {
		return selection;
	}

	protected void resolveSelection() {
		selection = null;
		prevItem = null;
		nextItem = null;

		if (treeViewer != null) {
			TreeItem[] items = treeViewer.getTree().getSelection();
			if (items.length > 0) {
				selection = items[0];
				TreeItem parent = selection.getParentItem();
				if (parent != null) {
					int index = parent.indexOf(selection);
					if (index > 0) {
						prevItem = parent.getItem(index - 1);
					}
					if (index + 1 < parent.getItemCount()) {
						nextItem = parent.getItem(index + 1);
					}
				}
			}
		}
	}

	@Override
	public void run() {
		resolveSelection();
	}

}
