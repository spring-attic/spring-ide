/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.DummyLayout;

/**
 * 
 */
public class SubgraphFigure extends Figure {

    /**
     * 
     */
    IFigure contents;

    /**
     * 
     */
    IFigure footer;

    /**
     * 
     */
    IFigure header;

    /**
     * 
     * 
     * @param footer 
     * @param header 
     */
    public SubgraphFigure(IFigure header, IFigure footer) {
        contents = new Figure();
        contents.setLayoutManager(new DummyLayout());
        add(contents);
        add(this.header = header);
        add(this.footer = footer);
    }

    /**
     * 
     * 
     * @return 
     */
    public IFigure getContents() {
        return contents;
    }

    /**
     * 
     * 
     * @return 
     */
    public IFigure getFooter() {
        return footer;
    }

    /**
     * 
     * 
     * @return 
     */
    public IFigure getHeader() {
        return header;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    public Dimension getPreferredSize(int wHint, int hHint) {
        Dimension dim = new Dimension();
        dim.width = getHeader().getPreferredSize().width;
        dim.width += getInsets().getWidth() + 10;
        dim.height = 130;
        return dim;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.Figure#setBounds(org.eclipse.draw2d.geometry.Rectangle)
     */
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
        rect = Rectangle.SINGLETON;
        getClientArea(rect);
        contents.setBounds(rect);
        Dimension size = footer.getPreferredSize();
        footer.setLocation(rect.getBottomLeft().translate(0, -size.height));
        footer.setSize(size);

        size = header.getPreferredSize();
        header.setSize(size);
        header.setLocation(rect.getLocation());

    }

    /**
     * 
     * 
     * @param value 
     */
    public void setSelected(boolean value) {
    }

}