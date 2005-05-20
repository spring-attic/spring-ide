/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Rectangle;

public class CompoundStateFigure extends SubgraphFigure {

    boolean selected;

    public CompoundStateFigure() {
        super(new Label(""), new Label(""));
        setBorder(new MarginBorder(3, 5, 3, 0));
        //setBorder(new ShadowedLineBorder());
        setOpaque(true);
    }

    protected void paintFigure(Graphics g) {
        super.paintFigure(g);
        Rectangle r = getBounds();
        g.setBackgroundColor(ColorConstants.button);
        if (selected) {
            g.setBackgroundColor(ColorConstants.menuBackgroundSelected);
            g.setForegroundColor(ColorConstants.menuForegroundSelected);
        }
        //g.fillRectangle(r);
        g.fillRectangle(r.x, r.y, 5, r.height);
        g.fillRectangle(r.right() - 5, r.y, 5, r.height);
        g.fillRectangle(r.x, r.bottom() - 5, r.width, 5);
        g.fillRectangle(r.x, r.y, r.width, 20);
        Rectangle tempRect = new Rectangle();
        tempRect.setBounds(this.getBounds());
        tempRect.width--;
        tempRect.height--;
        tempRect.shrink(1 / 2, 1 / 2);
        g.setLineWidth(1);
        g.drawRectangle(tempRect);
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