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
