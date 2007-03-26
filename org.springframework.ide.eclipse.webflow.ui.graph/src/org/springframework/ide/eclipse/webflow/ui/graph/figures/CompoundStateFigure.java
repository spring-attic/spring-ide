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
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * 
 */
public class CompoundStateFigure extends SubgraphFigure {
	
    /**
     * 
     */
    boolean selected;

    /**
     * 
     */
    public CompoundStateFigure() {
        super(new Label(""), new Label(""));
        //setBorder(new MarginBorder(3, 5, 3, 0));
        setBorder(new ShadowedLineBorder());
        setOpaque(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    protected void paintFigure(Graphics g) {
        super.paintFigure(g);
        Rectangle r = super.getBounds();
        g.setAntialias(SWT.ON);
        g.setBackgroundColor(ColorConstants.button);
        if (selected) {
            g.setBackgroundColor(ColorConstants.menuBackgroundSelected);
            g.setForegroundColor(ColorConstants.menuForegroundSelected);
        }
        //g.fillRectangle(r);
        g.fillRectangle(r.x, r.y, 5, r.height - 5);
        g.fillRectangle(r.right() - 9, r.y, 5, r.height - 5);
        g.fillRectangle(r.x, r.bottom() - 9, r.width - 5, 5);
        if (getHeader().getPreferredSize().height < 20) {
            g.fillRectangle(r.x, r.y, r.width - 5, 20);
        }
        else {
            g.fillRectangle(r.x, r.y, r.width - 5, 29);
        }
        /*Rectangle tempRect = new Rectangle();
        tempRect.setBounds(this.getBounds());
        tempRect.width--;
        tempRect.height--;
        tempRect.shrink(1 / 2, 1 / 2);
        g.setLineWidth(1);
        g.drawRectangle(tempRect);
        
        
        r = getBounds();
        g.setBackgroundColor(ColorConstants.lightGray);
        g.fillRectangle(r.x, r.y + r.height + 4, r.width, 4);*/
    }

    /* (non-Javadoc)
     * @see org.springframework.ide.eclipse.webflow.ui.graph.figures.SubgraphFigure#setSelected(boolean)
     */
    public void setSelected(boolean selected) {
        if (this.selected == selected)
            return;
        this.selected = selected;
        if (!selected) {
            getHeader().setForegroundColor(null);
            getFooter().setForegroundColor(null);

        }
        else {
            getHeader().setForegroundColor(
                    ColorConstants.menuForegroundSelected);
            getFooter().setForegroundColor(
                    ColorConstants.menuForegroundSelected);

        }
        repaint();
    }
}
