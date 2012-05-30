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
package org.springframework.ide.eclipse.config.ui.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.wst.sse.ui.views.properties.PropertySheetConfiguration;
import org.eclipse.wst.xml.ui.views.properties.XMLPropertySheetConfiguration;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SpringConfigPropertySheetPage extends TabbedPropertySheetPage {

	private final AbstractConfigEditor editor;

	private final PropertySheetConfiguration config;

	public SpringConfigPropertySheetPage(AbstractConfigEditor editor) {
		super(editor);
		this.editor = editor;
		config = new XMLPropertySheetConfiguration();
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	protected AbstractConfigEditor getConfigEditor() {
		return editor;
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != null && part instanceof PageBookView) {
			if (((PageBookView) part).getCurrentPage() == this) {
				return;
			}
		}

		if (getControl() != null && getControl().isVisible()) {
			ISelection preferredSelection = config.getInputSelection(part, selection);
			if (preferredSelection instanceof IStructuredSelection) {
				// don't support more than one selected node
				if (((IStructuredSelection) preferredSelection).size() > 1) {
					preferredSelection = StructuredSelection.EMPTY;
				}
			}
			super.selectionChanged(part, preferredSelection);
		}
	}

}
