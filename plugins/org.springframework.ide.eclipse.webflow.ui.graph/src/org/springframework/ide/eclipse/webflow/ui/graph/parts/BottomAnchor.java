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
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Christian Dupuis
 */
class BottomAnchor extends AbstractConnectionAnchor {

    /**
     * 
     */
    private int offset;

    /**
     * 
     * 
     * @param source 
     * @param offset 
     */
    BottomAnchor(IFigure source, int offset) {
        super(source);
        this.offset = offset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.draw2d.ConnectionAnchor#getLocation(org.eclipse.draw2d.geometry.Point)
     */
    public Point getLocation(Point reference) {
        Rectangle r = getOwner().getBounds().getCopy();
        getOwner().translateToAbsolute(r);
        int off = offset;
        if (off == -1)
            off = r.width / 2;
        if (r.contains(reference) || r.bottom() > reference.y)
            return r.getTop().translate(off, 0);
        else
            return r.getBottom().translate(off, -1);
    }

}
