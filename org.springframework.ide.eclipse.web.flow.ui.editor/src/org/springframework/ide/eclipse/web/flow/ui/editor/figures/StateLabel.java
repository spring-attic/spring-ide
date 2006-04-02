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
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class StateLabel extends Label {

    public static final Color COLOR = new Color(null, 0, 0, 0);

    private boolean hasFocus;

    private boolean selected;
    
    private Rectangle getSelectionRectangle() {
        Rectangle bounds = getTextBounds();
        bounds.expand(new Insets(1, 1, 1, 1));
        translateToParent(bounds);
        bounds.intersect(getBounds());
        return bounds;
    }

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

    public void setFocus(boolean b) {
        hasFocus = b;
        repaint();
    }

    public void setSelected(boolean b) {
        selected = b;
        repaint();
    }
}