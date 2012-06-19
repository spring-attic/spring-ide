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
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class CollapseNodeAction extends TreeViewerNodeAction {

	public CollapseNodeAction(TreeViewer treeViewer, SpringConfigContentAssistProcessor processor) {
		super(treeViewer, processor);
		setImageDescriptor(CommonImages.COLLAPSE_ALL);
	}

	@Override
	public void run() {
		super.run();
		TreeItem selection = getSelectedTreeItem();
		IDOMElement element = getElementFromTreeItem(selection);
		if (treeViewer != null && element != null) {
			treeViewer.collapseToLevel(element, TreeViewer.ALL_LEVELS);
		}
	}

}
