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
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * 
 */
public class StateLabel extends Label {

    /**
     * 
     */
    public static final Color COLOR = new Color(null, 0, 0, 0);

    /**
     * 
     */
    private boolean hasFocus;

    /**
     * 
     */
    private boolean selected;
    
    /**
     * 
     * 
     * @return 
     */
    private Rectangle getSelectionRectangle() {
        Rectangle bounds = getTextBounds();
        bounds.expand(new Insets(1, 1, 1, 1));
        translateToParent(bounds);
        bounds.intersect(getBounds());
        return bounds;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Label#paintFigure(org.eclipse.draw2d.Graphics)
     */
    protected void paintFigure(Graphics graphics) {
        graphics.fillRectangle(getBounds());
        if (selected) {
            graphics.pushState();
            graphics.setBackgroundColor(ColorConstants.menuBackgroundSelected);
            graphics.fillRectangle(getSelectionRectangle());
            graphics.popState();
            graphics.setForegroundColor(ColorConstants.white);
        }
        if (hasFocus) {
            graphics.pushState();
            graphics.setXORMode(true);
            graphics.setForegroundColor(ColorConstants.menuBackgroundSelected);
            graphics.setBackgroundColor(ColorConstants.white);
            graphics.drawFocus(getSelectionRectangle().resize(-1, -1));
            graphics.popState();
        }
        super.paintFigure(graphics);
    }

    /**
     * 
     * 
     * @param b 
     */
    public void setFocus(boolean b) {
        hasFocus = b;
        repaint();
    }

    /**
     * 
     * 
     * @param b 
     */
    public void setSelected(boolean b) {
        selected = b;
        repaint();
    }
}
