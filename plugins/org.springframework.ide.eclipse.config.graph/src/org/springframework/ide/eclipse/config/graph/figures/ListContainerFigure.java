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
package org.springframework.ide.eclipse.config.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.beans.ui.graph.figures.ShadowedLineBorder;

/**
 * @author Leo Dos Santos
 */
public class ListContainerFigure extends SubgraphFigure {

	protected boolean selected;

	private final Color containerBlue = new Color(null, 198, 220, 235);

	public ListContainerFigure(int direction) {
		super(new Label(""), new Label(""), direction); //$NON-NLS-1$ //$NON-NLS-2$
		setBorder(new ShadowedLineBorder());
		setOpaque(true);
	}

	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
		Rectangle r = super.getBounds();
		g.setAntialias(SWT.ON);
		g.setBackgroundColor(containerBlue);
		if (selected) {
			g.setBackgroundColor(ColorConstants.menuBackgroundSelected);
			g.setForegroundColor(ColorConstants.menuForegroundSelected);
		}
		g.fillRectangle(r.x, r.y, 5, r.height - 5);
		g.fillRectangle(r.right() - 9, r.y, 5, r.height - 5);
		g.fillRectangle(r.x, r.bottom() - 9, r.width - 5, 5);
		if (getHeader().getPreferredSize().height < 20) {
			g.fillRectangle(r.x, r.y, r.width - 5, 20);
		}
		else {
			g.fillRectangle(r.x, r.y, r.width - 5, 29);
		}
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		footer.setLocation(new Point(0, 0));
		footer.setSize(0, 0);
	}

	@Override
	public void setSelected(boolean selected) {
		if (this.selected == selected) {
			return;
		}
		this.selected = selected;
		if (!selected) {
			getHeader().setForegroundColor(null);
			getFooter().setForegroundColor(null);
		}
		else {
			getHeader().setForegroundColor(ColorConstants.menuForegroundSelected);
			getFooter().setForegroundColor(ColorConstants.menuForegroundSelected);
		}
		repaint();
	}

}
