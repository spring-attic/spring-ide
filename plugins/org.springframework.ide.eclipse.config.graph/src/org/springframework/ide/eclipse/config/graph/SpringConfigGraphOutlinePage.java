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
package org.springframework.ide.eclipse.config.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Leo Dos Santos
 */
public class SpringConfigGraphOutlinePage extends Page implements IContentOutlinePage {

	private Canvas overview;

	private final ScalableFreeformRootEditPart rootEditPart;

	private Thumbnail thumbnail;

	public SpringConfigGraphOutlinePage(ScalableFreeformRootEditPart rootEditPart) {
		this.rootEditPart = rootEditPart;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public void createControl(Composite parent) {
		overview = new Canvas(parent, SWT.NONE);
		overview.setBackground(ColorConstants.listBackground);
		LightweightSystem lws = new LightweightSystem(overview);
		thumbnail = new ScrollableThumbnail((Viewport) rootEditPart.getFigure());
		thumbnail.setBorder(new MarginBorder(3));
		thumbnail.setSource(rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS));
		lws.setContents(thumbnail);
	}

	@Override
	public void dispose() {
		if (null != thumbnail) {
			thumbnail.deactivate();
		}
		super.dispose();
	}

	@Override
	public Control getControl() {
		return overview;
	}

	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public void setFocus() {
		if (getControl() != null) {
			getControl().setFocus();
		}
	}

	public void setSelection(ISelection selection) {
	}

}
