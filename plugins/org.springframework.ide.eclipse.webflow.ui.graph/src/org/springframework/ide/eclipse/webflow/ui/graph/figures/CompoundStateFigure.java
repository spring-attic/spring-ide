/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.preferences.WebflowGraphPreferences;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class CompoundStateFigure extends SubgraphFigure {
	
    private boolean selected;
    
    private IWebflowModelElement model;

    public CompoundStateFigure(IWebflowModelElement model) {
        super(new Label(""), new Label(""));
        setBorder(new ShadowedLineBorder());
        setOpaque(true);
        this.model = model;
    }

    protected void paintFigure(Graphics g) {
        super.paintFigure(g);
        Rectangle r = super.getBounds();
        g.setAntialias(SWT.ON);
        g.setBackgroundColor(WebflowGraphPreferences.getColorForModelElement(model));
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
