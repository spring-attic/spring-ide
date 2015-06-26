/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class DefaultPathEditorSupport extends EditingSupport {

	private CellEditor editor;
	private int col;

	public DefaultPathEditorSupport(TableViewer tableViewer) {
		super(tableViewer);
//		if (col==PROPERTY_NAME_COLUMN) {
//			IContentProposalProvider proposalProvider =
//				//	new SimpleContentProposalProvider(new String[] {"red", "green", "blue"});
//				 new PropertyNameContentProposalProvider(project);
//			this.editor = new TextCellEditorWithContentProposal(tableViewer.getTable(),
//					proposalProvider, CTRL_SPACE,
//					SpringPropertiesProposalProcessor.AUTO_ACTIVATION_CHARS
//			).setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
//		} else {
			this.editor = new TextCellEditor(tableViewer.getTable());
//		}
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return element instanceof BootDashElement;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof BootDashElement) {
			String path = ((BootDashElement) element).getDefaultRequestMappingPath();
			return path==null?"":path;
		}
		return "?huh?";
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (value instanceof String && element instanceof BootDashElement) {
			((BootDashElement)element).setDefaultRequestMapingPath((String) value);
		}
	}
}
