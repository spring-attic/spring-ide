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
package org.springframework.ide.eclipse.config.ui.hyperlinks;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ExtendedNodeElementHyperlink extends NodeElementHyperlink {

	private final Node node;

	public ExtendedNodeElementHyperlink(Node node, IRegion region, IRegion targetRegion, ITextViewer viewer) {
		super(region, targetRegion, viewer);
		this.node = node;
	}

	@Override
	public void open() {
		super.open();
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof AbstractConfigEditor) {
			AbstractConfigEditor cEditor = (AbstractConfigEditor) editor;
			cEditor.revealElement(node);
		}
	}

}
