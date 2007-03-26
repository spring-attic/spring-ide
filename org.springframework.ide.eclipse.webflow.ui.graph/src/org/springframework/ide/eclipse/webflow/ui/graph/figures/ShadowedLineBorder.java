/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;

/**
 * 
 */
public class ShadowedLineBorder extends LineBorder {

    /**
     * 
     */
    private int shadowWidth;

    /**
     * 
     */
    public ShadowedLineBorder() {
        this(ColorConstants.black, 1, 4);
    }

    /**
     * 
     * 
     * @param lineWidth 
     * @param color 
     * @param shadowWidth 
     */
    public ShadowedLineBorder(Color color, int lineWidth, int shadowWidth) {
        super(color, lineWidth);
        this.shadowWidth = shadowWidth;
    }

    /**
     * 
     * 
     * @param shadowWidth 
     */
    public void setShadowWidth(int shadowWidth) {
        this.shadowWidth = shadowWidth;
    }

    /**
     * 
     * 
     * @return 
     */
    public int getShadowWidth() {
        return this.shadowWidth;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.LineBorder#getInsets(org.eclipse.draw2d.IFigure)
     */
    public Insets getInsets(IFigure figure) {
        return new Insets(getWidth(), getWidth(),
                getWidth() + getShadowWidth(), getWidth() + getShadowWidth());
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.LineBorder#paint(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
     */
    public void paint(IFigure figure, Graphics graphics, Insets insets) {

        // Paint line border [copied from super.paint()]
        tempRect.setBounds(getPaintRectangle(figure, insets));
        tempRect.width -= getShadowWidth();
        tempRect.height -= getShadowWidth();
        if (getWidth() % 2 == 1) {
            tempRect.width--;
            tempRect.height--;
        }
        tempRect.shrink(getWidth() / 2, getWidth() / 2);
        graphics.setLineWidth(getWidth());
        if (getColor() != null) {
            graphics.setForegroundColor(getColor());
        }
        graphics.drawRectangle(tempRect);

        // Paint the shadow by reusing the temporary rectangle already
        // initialized by super.paint()
        PointList plt = new PointList();
        plt.addPoint(tempRect.x + 1 + tempRect.width, tempRect.y
                + getShadowWidth());
        plt.addPoint(tempRect.x + tempRect.width + 1, tempRect.y
                + tempRect.height + 1);
        plt.addPoint(tempRect.x + getShadowWidth(), tempRect.y
                + tempRect.height + 1);
        plt.addPoint(tempRect.x + getShadowWidth(), tempRect.y
                + tempRect.height + getShadowWidth());
        plt.addPoint(tempRect.x + tempRect.width + getShadowWidth(), tempRect.y
                + tempRect.height + getShadowWidth());
        plt.addPoint(tempRect.x + tempRect.width + getShadowWidth(), tempRect.y
                + getShadowWidth());
        graphics.setBackgroundColor(ColorConstants.lightGray);
        graphics.fillPolygon(plt);
    }
}
