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
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GroupBoxBorder;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * @author Leo Dos Santos
 */
public class ConnectorBorder extends GroupBoxBorder {

	// private final Insets insets = new Insets(8, 6, 8, 6);

	protected final int direction;

	protected PointList connector;

	protected PointList bottomConnector;

	protected final int inputCapacity;

	protected final int outputCapacity;

	public ConnectorBorder(int direction, int inputs, int outputs) {
		super(""); //$NON-NLS-1$
		this.direction = direction;
		this.inputCapacity = inputs;
		this.outputCapacity = outputs;
		createPointLists();
		if (direction == PositionConstants.EAST) {
			// insets.transpose();
			connector.transpose();
			bottomConnector.transpose();
		}
	}

	private void createPointLists() {
		connector = new PointList();
		bottomConnector = new PointList();

		connector.addPoint(-3, 0);
		connector.addPoint(-3, 6);
		connector.addPoint(3, 6);
		connector.addPoint(3, 0);

		bottomConnector.addPoint(3, 0);
		bottomConnector.addPoint(3, -6);
		bottomConnector.addPoint(-3, -6);
		bottomConnector.addPoint(-3, 0);
	}

	protected void drawAnchors(Graphics graphics, Rectangle rect) {
		graphics.pushState();
		graphics.setBackgroundColor(ColorConstants.gray);
		graphics.setForegroundColor(ColorConstants.gray);
		if (direction == PositionConstants.EAST) {
			int y1;
			int x1 = rect.x;
			int end = rect.width + x1;
			int height = rect.height;
			for (int i = 0; i < inputCapacity; i++) {
				y1 = rect.y + (2 * i + 1) * height / (inputCapacity * 2);
				connector.translate(x1, y1);
				graphics.fillPolygon(connector);
				graphics.drawPolygon(connector);
				connector.translate(-x1, -y1);
			}
			for (int i = 0; i < outputCapacity; i++) {
				y1 = rect.y + (2 * i + 1) * height / (outputCapacity * 2);
				bottomConnector.translate(end, y1);
				graphics.fillPolygon(bottomConnector);
				graphics.drawPolygon(bottomConnector);
				bottomConnector.translate(-end, -y1);
			}
		}
		else {
			int x1;
			int y1 = rect.y;
			int width = rect.width;
			int bottom = y1 + rect.height;
			for (int i = 0; i < inputCapacity; i++) {
				x1 = rect.x + (2 * i + 1) * width / (inputCapacity * 2);
				connector.translate(x1, y1);
				graphics.fillPolygon(connector);
				graphics.drawPolygon(connector);
				connector.translate(-x1, -y1);
			}
			for (int i = 0; i < outputCapacity; i++) {
				x1 = rect.x + (2 * i + 1) * width / (outputCapacity * 2);
				bottomConnector.translate(x1, bottom);
				graphics.fillPolygon(bottomConnector);
				graphics.drawPolygon(bottomConnector);
				bottomConnector.translate(-x1, -bottom);
			}
		}
		graphics.popState();
	}

	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		drawAnchors(graphics, figure.getBounds().getCropped(insets));
		superPaint(figure, graphics, insets);
	}

	/**
	 * Copied from
	 * {@link FigureUtilities#paintEtchedBorder(Graphics, Rectangle)} and
	 * modified to dispose the generated colors.
	 */
	private void paintEtchedBorder(Graphics g, Rectangle r) {
		Color rgb = ColorConstants.gray, shadow = FigureUtilities.darker(rgb), highlight = FigureUtilities.lighter(rgb);
		paintEtchedBorder(g, r, shadow, highlight);
		shadow.dispose();
		highlight.dispose();
	}

	/**
	 * Copied from
	 * {@link FigureUtilities#paintEtchedBorder(Graphics, Rectangle, Color, Color)}
	 * and modified to draw round rectangles.
	 */
	private void paintEtchedBorder(Graphics g, Rectangle r, Color shadow, Color highlight) {
		int x = r.x, y = r.y, w = r.width, h = r.height, arc = 10;

		g.setLineStyle(Graphics.LINE_SOLID);
		g.setLineWidth(1);
		g.setXORMode(false);

		w -= 2;
		h -= 2;

		g.setForegroundColor(shadow);
		g.drawRoundRectangle(new Rectangle(x, y, w, h), arc, arc);

		x++;
		y++;
		g.setForegroundColor(highlight);
		g.drawRoundRectangle(new Rectangle(x, y, w, h), arc, arc);
	}

	/**
	 * Copied from {@link GroupBoxBorder#paint(IFigure, Graphics, Insets)} and
	 * modified to use a different
	 * {@link #paintEtchedBorder(Graphics, Rectangle)} method.
	 */
	private void superPaint(IFigure figure, Graphics g, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, insets));
		Rectangle r = tempRect;
		if (r.isEmpty()) {
			return;
		}

		Rectangle textLoc = new Rectangle(r.getTopLeft(), getTextExtents(figure));
		r.crop(new Insets(getTextExtents(figure).height / 2));
		paintEtchedBorder(g, r);

		textLoc.x += getInsets(figure).left;
		g.setFont(getFont(figure));
		g.setForegroundColor(getTextColor());
		g.clipRect(textLoc);
		g.fillText(getLabel(), textLoc.getTopLeft());
	}

}
